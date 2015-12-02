/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package koh.game.fights;

import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.fighters.MonsterFighter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
import koh.protocol.client.Message;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.protocol.client.enums.FightOptionsEnum;
import koh.protocol.client.enums.FighterRefusedReasonEnum;
import koh.protocol.client.enums.TeamTypeEnum;
import koh.protocol.types.game.context.fight.FightOptionsInformations;
import koh.protocol.types.game.context.fight.FightTeamInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;

/**
 *
 * @author Neo-Craft
 */
public class FightTeam {

    private HashMap<FightOptionsEnum, Boolean> myToggleLocks = new HashMap<FightOptionsEnum, Boolean>() {
        {
            put(FightOptionsEnum.FIGHT_OPTION_SET_SECRET, false);
            put(FightOptionsEnum.FIGHT_OPTION_SET_CLOSED, false);
            put(FightOptionsEnum.FIGHT_OPTION_SET_TO_PARTY_ONLY, false);
            put(FightOptionsEnum.FIGHT_OPTION_ASK_FOR_HELP, false);
        }
    };

    public AlignmentSideEnum AlignmentSide = AlignmentSideEnum.ALIGNMENT_WITHOUT;

    private ArrayList<Fighter> myFighters = new ArrayList<>(8);
    public byte Id;
    public int LeaderId;
    public Fighter Leader;
    public Fight Fight;
    public ArrayList<SwapPositionRequest> SwapRequests = new ArrayList<>();

    public FightTeam(byte Id, Fight f) {
        this.Id = Id;
        this.Fight = f;
    }

    public Stream<Fighter> GetFighters() {
        return this.myFighters.stream();
    }

    public Stream<Fighter> GetAliveFighters() {
        return this.myFighters.stream().filter(x -> x.IsAlive());
    }

    public Stream<Fighter> GetDeadFighters() {
        return this.myFighters.stream().filter(x -> !x.IsAlive());
    }

    public FighterRefusedReasonEnum CanJoin(Player Character) {
        if (this.Leader instanceof MonsterFighter) {
            return FighterRefusedReasonEnum.WRONG_ALIGNMENT;
        }
        if (this.Fight.FightState != FightState.STATE_PLACE) {
            return FighterRefusedReasonEnum.TOO_LATE;
        }
        if (this.myFighters.size() >= 8) {
            return FighterRefusedReasonEnum.TEAM_FULL;
        }

        if (this.IsToggle(FightOptionsEnum.FIGHT_OPTION_SET_SECRET) || this.IsToggle(FightOptionsEnum.FIGHT_OPTION_SET_CLOSED)) {
            return FighterRefusedReasonEnum.TEAM_LIMITED_BY_MAINCHARACTER;
        }
        if (this.IsToggle(FightOptionsEnum.FIGHT_OPTION_SET_TO_PARTY_ONLY)) {
            if (!(((CharacterFighter) this.Leader).Character.client.getParty() != null && ((CharacterFighter) this.Leader).Character.client.getParty().containsPlayer(Character))) {
                return FighterRefusedReasonEnum.TEAM_LIMITED_BY_MAINCHARACTER;
            }
        }
        return this.AlignmentSide != AlignmentSideEnum.ALIGNMENT_WITHOUT && Character.alignmentSide != this.AlignmentSide ? FighterRefusedReasonEnum.WRONG_ALIGNMENT : FighterRefusedReasonEnum.FIGHTER_ACCEPTED;
    }

    public byte TeamType() {
        if (this.Leader instanceof CharacterFighter) {
            return TeamTypeEnum.TEAM_TYPE_PLAYER;
        }
        return this.Leader instanceof MonsterFighter ? TeamTypeEnum.TEAM_TYPE_MONSTER : TeamTypeEnum.TEAM_TYPE_BAD_PLAYER;
    }

    public void SetLeader(Fighter Fighter) {
        this.Leader = Fighter;
        this.LeaderId = Fighter.ID;
    }

    public FightTeamInformations GetFightTeamInformations() {
        return new FightTeamInformations(this.Id, this.Leader != null ? this.Leader.ID : 0, this.AlignmentSide.value, this.TeamType(), (byte) 0, this.GetFighters().map(x -> x.GetFightTeamMemberInformations()).toArray(FightTeamMemberInformations[]::new));
    }

    public FightOptionsInformations GetFightOptionsInformations() {
        return new FightOptionsInformations(IsToggle(FightOptionsEnum.FIGHT_OPTION_SET_SECRET), IsToggle(FightOptionsEnum.FIGHT_OPTION_SET_TO_PARTY_ONLY), IsToggle(FightOptionsEnum.FIGHT_OPTION_SET_CLOSED), IsToggle(FightOptionsEnum.FIGHT_OPTION_ASK_FOR_HELP));
    }

    public short BladePosition = -1;

    public void FighterJoin(Fighter Fighter) {
        Fighter.Team = this;

        this.myFighters.add(Fighter);
    }

    public void FighterLeave(Fighter Fighter) {
        this.myFighters.remove(Fighter);
    }

    public boolean HasFighterAlive() {
        return this.myFighters.stream().anyMatch(x -> x.IsAlive());
    }

    public void Toggle(FightOptionsEnum ToggleType, boolean Value) {
        synchronized (this.myToggleLocks) {
            this.myToggleLocks.put(ToggleType, Value);
        }
    }

    public boolean IsToggle(FightOptionsEnum ToggleType) {
        synchronized (this.myToggleLocks) {
            return this.myToggleLocks.get(ToggleType);
        }
    }

    public void EndFight() {
        this.myFighters.removeIf(x -> x.Summoner != null); // On delete les invocations
        //this.myFighters.RemoveAll(x =  > x is DoubleFighter);  // On delete les doubles
    }

    public void Dispose() {
        this.myFighters.clear();

        this.myFighters = null;
        this.Leader = null;
    }

    public boolean IsFriendly(Fighter fighter) {
        return fighter.Team.Id == this.Id;
    }

    public void sendToField(Message Message) { //TODO : clean this fucking code
        this.Fight.sendToField(new FieldNotification(Message) {
            @Override
            public boolean can(Player perso) {
                return perso.client != null && perso.getFighter() != null && perso.getFighter().Team.Id == Id;
            }
        });
    }

    public SwapPositionRequest GetRequest(int id) {
        Optional<SwapPositionRequest> toUse = this.SwapRequests.stream().filter(x -> x.requestId == id).findFirst();
        if (toUse.isPresent()) {
            return toUse.get();
        } else {
            return null;
        }
    }

    public synchronized int GetNextRequestId() {
        OptionalInt OptionalInt = SwapRequests.stream().mapToInt(x -> x.requestId).max();
        if (OptionalInt.isPresent()) {
            return OptionalInt.getAsInt() + 1;
        } else {
            return 0;
        }
    }

}

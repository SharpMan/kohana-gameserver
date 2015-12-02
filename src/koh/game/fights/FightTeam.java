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

    public AlignmentSideEnum alignmentSide = AlignmentSideEnum.ALIGNMENT_WITHOUT;

    private ArrayList<Fighter> myFighters = new ArrayList<>(8);
    public byte Id;
    public int LeaderId;
    public Fighter Leader;
    public Fight Fight;
    public ArrayList<SwapPositionRequest> swapRequests = new ArrayList<>();

    public FightTeam(byte Id, Fight f) {
        this.Id = Id;
        this.Fight = f;
    }

    public Stream<Fighter> getFighters() {
        return this.myFighters.stream();
    }

    public Stream<Fighter> getAliveFighters() {
        return this.myFighters.stream().filter(x -> x.isAlive());
    }

    public Stream<Fighter> getDeadFighters() {
        return this.myFighters.stream().filter(x -> !x.isAlive());
    }

    public FighterRefusedReasonEnum canJoin(Player Character) {
        if (this.Leader instanceof MonsterFighter) {
            return FighterRefusedReasonEnum.WRONG_ALIGNMENT;
        }
        if (this.Fight.fightState != FightState.STATE_PLACE) {
            return FighterRefusedReasonEnum.TOO_LATE;
        }
        if (this.myFighters.size() >= 8) {
            return FighterRefusedReasonEnum.TEAM_FULL;
        }

        if (this.isToggled(FightOptionsEnum.FIGHT_OPTION_SET_SECRET) || this.isToggled(FightOptionsEnum.FIGHT_OPTION_SET_CLOSED)) {
            return FighterRefusedReasonEnum.TEAM_LIMITED_BY_MAINCHARACTER;
        }
        if (this.isToggled(FightOptionsEnum.FIGHT_OPTION_SET_TO_PARTY_ONLY)) {
            if (!(((CharacterFighter) this.Leader).Character.client.getParty() != null && ((CharacterFighter) this.Leader).Character.client.getParty().containsPlayer(Character))) {
                return FighterRefusedReasonEnum.TEAM_LIMITED_BY_MAINCHARACTER;
            }
        }
        return this.alignmentSide != AlignmentSideEnum.ALIGNMENT_WITHOUT && Character.alignmentSide != this.alignmentSide ? FighterRefusedReasonEnum.WRONG_ALIGNMENT : FighterRefusedReasonEnum.FIGHTER_ACCEPTED;
    }

    public byte getTeamType() {
        if (this.Leader instanceof CharacterFighter) {
            return TeamTypeEnum.TEAM_TYPE_PLAYER;
        }
        return this.Leader instanceof MonsterFighter ? TeamTypeEnum.TEAM_TYPE_MONSTER : TeamTypeEnum.TEAM_TYPE_BAD_PLAYER;
    }

    public void setLeader(Fighter Fighter) {
        this.Leader = Fighter;
        this.LeaderId = Fighter.ID;
    }

    public FightTeamInformations getFightTeamInformations() {
        return new FightTeamInformations(this.Id, this.Leader != null ? this.Leader.ID : 0, this.alignmentSide.value, this.getTeamType(), (byte) 0, this.getFighters().map(x -> x.getFightTeamMemberInformations()).toArray(FightTeamMemberInformations[]::new));
    }

    public FightOptionsInformations getFightOptionsInformations() {
        return new FightOptionsInformations(isToggled(FightOptionsEnum.FIGHT_OPTION_SET_SECRET), isToggled(FightOptionsEnum.FIGHT_OPTION_SET_TO_PARTY_ONLY), isToggled(FightOptionsEnum.FIGHT_OPTION_SET_CLOSED), isToggled(FightOptionsEnum.FIGHT_OPTION_ASK_FOR_HELP));
    }

    public short bladePosition = -1;

    public void fighterJoin(Fighter Fighter) {
        Fighter.team = this;

        this.myFighters.add(Fighter);
    }

    public void fighterLeave(Fighter Fighter) {
        this.myFighters.remove(Fighter);
    }

    public boolean hasFighterAlive() {
        return this.myFighters.stream().anyMatch(x -> x.isAlive());
    }

    public void toggle(FightOptionsEnum ToggleType, boolean Value) {
        synchronized (this.myToggleLocks) {
            this.myToggleLocks.put(ToggleType, Value);
        }
    }

    public boolean isToggled(FightOptionsEnum ToggleType) {
        synchronized (this.myToggleLocks) {
            return this.myToggleLocks.get(ToggleType);
        }
    }

    public void endFight() {
        this.myFighters.removeIf(x -> x.summoner != null); // On delete les invocations
        //this.myFighters.RemoveAll(x =  > x is DoubleFighter);  // On delete les doubles
    }

    public void dispose() {
        this.myFighters.clear();

        this.myFighters = null;
        this.Leader = null;
    }

    public boolean isFriendly(Fighter fighter) {
        return fighter.team.Id == this.Id;
    }

    public void sendToField(Message Message) { //TODO : clean this fucking code
        this.Fight.sendToField(new FieldNotification(Message) {
            @Override
            public boolean can(Player perso) {
                return perso.client != null && perso.getFighter() != null && perso.getFighter().team.Id == Id;
            }
        });
    }

    public SwapPositionRequest getRequest(int id) {
        return this.swapRequests.stream().filter(x -> x.requestId == id).findFirst().orElse(null);
    }

    public synchronized int getNextRequestId() {
        return swapRequests.stream().mapToInt(x -> x.requestId).max().orElse(0);
    }

}

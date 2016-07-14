/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package koh.game.fights;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.fighters.DoubleFighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.fights.fighters.StaticFighter;
import koh.game.fights.utils.SwapPositionRequest;
import koh.protocol.client.Message;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.protocol.client.enums.FightOptionsEnum;
import koh.protocol.client.enums.FighterRefusedReasonEnum;
import koh.protocol.client.enums.TeamTypeEnum;
import koh.protocol.types.game.context.fight.FightOptionsInformations;
import koh.protocol.types.game.context.fight.FightTeamInformations;
import koh.protocol.types.game.context.fight.FightTeamLightInformations;
import koh.protocol.types.game.context.fight.FightTeamMemberInformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
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

    private List<Fighter> myFighters = Fight.POSIBLE ? new CopyOnWriteArrayList<>() : new ArrayList<>(8);
    public byte id;
    public int leaderId;
    public Fighter leader;
    public Fight fight;
    public ArrayList<SwapPositionRequest> swapRequests = new ArrayList<>(2);
    //private TeamTypeEnum teamType; //TODO

    public FightTeam(byte Id, Fight f) {
        this.id = Id;
        this.fight = f;
    }

    public Stream<Fighter> getFighters() {
        return this.myFighters.stream();
    }

    public Stream<Fighter> getFightersNotSummoned() {
        return this.getFighters().filter(fr -> !fr.hasSummoner());
    }

    public Stream<Fighter> getAliveFighters() {
        return this.myFighters.stream().filter(x -> x.isAlive());
    }

    public Stream<Fighter> getDeadFighters() {
        return this.myFighters.stream().filter(x -> !x.isAlive());
    }

    public Stream<MonsterFighter> getAsMonster() {
        return this.getFighters()
                .filter(fr -> fr instanceof MonsterFighter)
                .map(fr -> ((MonsterFighter) fr));
    }

    public FighterRefusedReasonEnum canJoin(Player Character) {
        if (this.leader instanceof MonsterFighter) {
            return FighterRefusedReasonEnum.WRONG_ALIGNMENT;
        }
        if (this.fight.getFightState() != FightState.STATE_PLACE) {
            return FighterRefusedReasonEnum.TOO_LATE;
        }
        if (this.myFighters.size() >= 8) {
            return FighterRefusedReasonEnum.TEAM_FULL;
        }

        if (this.isToggled(FightOptionsEnum.FIGHT_OPTION_SET_SECRET) || this.isToggled(FightOptionsEnum.FIGHT_OPTION_SET_CLOSED)) {
            return FighterRefusedReasonEnum.TEAM_LIMITED_BY_MAINCHARACTER;
        }
        if (this.isToggled(FightOptionsEnum.FIGHT_OPTION_SET_TO_PARTY_ONLY)) {
            if (!(((CharacterFighter) this.leader).getCharacter().getClient().getParty() != null && ((CharacterFighter) this.leader).getCharacter().getClient().getParty().containsPlayer(Character))) {
                return FighterRefusedReasonEnum.TEAM_LIMITED_BY_MAINCHARACTER;
            }
        }
        return this.alignmentSide != AlignmentSideEnum.ALIGNMENT_WITHOUT && Character.getAlignmentSide() != this.alignmentSide ? FighterRefusedReasonEnum.WRONG_ALIGNMENT : FighterRefusedReasonEnum.FIGHTER_ACCEPTED;
    }

    public byte getTeamType() {
        if (this.leader instanceof CharacterFighter) {
            return TeamTypeEnum.TEAM_TYPE_PLAYER;
        }
        return this.leader instanceof MonsterFighter ? TeamTypeEnum.TEAM_TYPE_MONSTER : TeamTypeEnum.TEAM_TYPE_BAD_PLAYER;
    }

    public void setLeader(Fighter fighter) {
        this.leader = fighter;
        this.leaderId = fighter.getID();
    }

    public FightTeamInformations getFightTeamInformations() {
        return new FightTeamInformations(this.id, this.leader != null ? this.leader.getID() : 0, this.alignmentSide.value, this.getTeamType(), (byte) 0, this.getFighters().map(x -> x.getFightTeamMemberInformations()).toArray(FightTeamMemberInformations[]::new));
    }

    public FightOptionsInformations getFightOptionsInformations() {
        return new FightOptionsInformations(isToggled(FightOptionsEnum.FIGHT_OPTION_SET_SECRET), isToggled(FightOptionsEnum.FIGHT_OPTION_SET_TO_PARTY_ONLY), isToggled(FightOptionsEnum.FIGHT_OPTION_SET_CLOSED), isToggled(FightOptionsEnum.FIGHT_OPTION_ASK_FOR_HELP));
    }

    public short bladePosition = -1;

    public void fighterJoin(Fighter fighter) {
        fighter.setTeam(this);

        this.myFighters.add(fighter);
    }

    public void fighterLeave(Fighter Fighter) {
        this.myFighters.remove(Fighter);
    }

    public boolean hasFighterAlive() {
        return this.myFighters.stream().anyMatch(x -> x.isAlive());
    }

    public int getLevel() {
        return this.myFighters.stream()
                .mapToInt(fr -> fr.getLevel())
                .sum();
    }

    public void toggle(FightOptionsEnum toggleType, boolean value) {
        synchronized (this.myToggleLocks) {
            this.myToggleLocks.put(toggleType, value);
        }
    }

    public boolean isToggled(FightOptionsEnum toggleType) {
        synchronized (this.myToggleLocks) {
            return this.myToggleLocks.get(toggleType);
        }
    }

    public void endFight() {
        this.myFighters.removeIf(fr -> fr.hasSummoner() || fr instanceof DoubleFighter || fr instanceof StaticFighter); // On delete les invocations
    }

    public void dispose() {
        this.myFighters.clear();

        this.myFighters = null;
        this.leader = null;
    }

    public boolean isFriendly(Fighter fighter) {
        return fighter.getTeam().id == this.id;
    }

    public void sendToField(Message Message) { //TODO : clean this fucking code
        this.fight.sendToField(new FieldNotification(Message) {
            @Override
            public boolean can(Player perso) {
                return perso.getClient() != null && perso.getFighter() != null && perso.getFighter().getTeam().id == id;
            }
        });
    }

    public SwapPositionRequest getRequest(int id) {
        return this.swapRequests.stream().filter(x -> x.requestId == id).findFirst().orElse(null);
    }

    public synchronized int getNextRequestId() {
        return swapRequests.stream().mapToInt(x -> x.requestId).max().orElse(0);
    }

    public FightTeamLightInformations getFightTeamLightInformations(Player visitor) {
        //byte teamId, int leaderId, byte teamSide, byte teamTypeId, byte nbWaves, byte teamMembersCount, int meanLevel, boolean hasFriend, boolean hasGuildMember, boolean hasAllianceMember, boolean hasGroupMember, boolean hasMyTaxCollector
        return new FightTeamLightInformations(this.id,
                this.leaderId,
                this.alignmentSide.value,
                getTeamType(),
                leader.wave,
                (byte) this.myFighters.size(),
                (int) this.getFighters().mapToInt(Fighter::getLevel).average().orElse(0),
                this.getFighters().filter(fr -> fr instanceof CharacterFighter).map(Fighter::getPlayer).anyMatch(pl -> pl.getAccount() != null && pl.getAccount().accountData != null && pl.getAccount().accountData.hasFriend(visitor.getOwner())),
                this.getFighters().filter(fr -> fr instanceof CharacterFighter).map(Fighter::getPlayer).anyMatch(pl -> pl.getGuild() == visitor.getGuild()),
                false,
                this.getFighters().filter(fr -> fr instanceof CharacterFighter).map(Fighter::getPlayer).anyMatch(pl -> visitor.getClient().getParty() != null && visitor.getClient().getParty().containsPlayer(pl)),
                false
        );
    }


}

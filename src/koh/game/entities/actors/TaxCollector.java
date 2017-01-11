package koh.game.entities.actors;

import koh.game.dao.DAO;
import koh.game.dao.api.SpellDAO;
import koh.game.entities.guilds.Guild;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.types.TaxCollectorFight;
import koh.look.EntityLookParser;
import koh.protocol.client.enums.TaxCollectorStateEnum;
import koh.protocol.types.game.character.CharacterMinimalPlusLookInformation;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.fight.ProtectedEntityWaitingForHelpInfo;
import koh.protocol.types.game.context.roleplay.GameRolePlayTaxCollectorInformations;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.context.roleplay.TaxCollectorStaticInformation;
import koh.protocol.types.game.guild.tax.*;
import koh.protocol.types.game.look.EntityLook;
import koh.utils.Enumerable;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Neo-Craft
 */
public class TaxCollector extends IGameActor {

    final static private EntityLook PERCEPTEUR = EntityLookParser.fromString("{714|||110}");
    @Getter
    private short cellID;
    private final GuildInformations guildInformations;
    @Getter
    private final Guild guild;
    @Getter
    private final int firstName,lastName;
    @Setter @Getter
    private int iden, honor;
    @Getter
    private long experience;
    @Getter
    private int kamas;
    @Getter
    private int attacksCount;
    @Getter
    private String callerName;
    @Getter @Setter
    private TaxCollectorFight current_fight;
    @Getter @Setter
    private TaxCollectorStateEnum state;
    @Getter @Setter
    private Map<Integer,Short> gatheredItem = new ConcurrentHashMap<Integer,Short>();
    @Getter @Setter
    private Object $mutex = new Object();


    public TaxCollector(short cellID, Guild guild, int firstName, int lastName, int mapid, int iden, long experience, int kamas, int attacksCount, String callerName,int honor,String items) {
        this.cellID = cellID;
        this.guild = guild;
        this.firstName = firstName;
        this.lastName = lastName;
        this.attacksCount = attacksCount;
        this.callerName = callerName;
        this.mapid = mapid;
        this.iden = iden;
        this.experience = experience;
        this.kamas = kamas;
        this.guildInformations = new GuildInformations(guild.getEntity().guildID, guild.getEntity().name, guild.getGuildEmblem());
        this.guild.getTaxCollectors().add(this);
        this.state = TaxCollectorStateEnum.STATE_COLLECTING;
        this.honor = honor;
        this.gatheredItem = Enumerable.stringToShortHashMap(items,10);
    }


    public int getItemValues() {
        return 0;
    }

    public int getBagSize() {
        return 0;
    }

    public byte currentState(){
        return (byte) state.getValue();
    }

    public int getLevel() {
        return guild.getEntity().level;
    }

    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        if(cache == null)
           cache = new GameRolePlayTaxCollectorInformations(this.ID, PERCEPTEUR, new EntityDispositionInformations(this.cellID,this.direction), new TaxCollectorStaticInformation(firstName, lastName, guildInformations), (byte) guild.getEntity().level, 0);
        return cache;
    }


    @Override
    public EntityLook getEntityLook() {
        return PERCEPTEUR;
    }

    public int getTaxCollectorHealth(){
        return BASE_HEALTH + (int)(20 * this.getLevel());
    }

    private GameRolePlayTaxCollectorInformations cache;


    public void incrementVictory(){
        this.attacksCount++;
    }

    public List<SpellLevel> getSpells(){
        ArrayList<SpellLevel> spells = new ArrayList<>(12);
        for(int i =0 ; i < guild.spellLevel.length; i++){
            spells.add(DAO.getSpells().findSpell(Guild.TAX_COLLECTOR_SPELLS[i]).getLevelOrNear(guild.spellLevel[i]));
        }
        return spells;
    }


    public void addExperience(int xp){
        this.experience += xp;
    }

    public void addKamas(int xp){
        this.kamas += xp;
    }


    public static final int BASE_HEALTH = 3000;

    public TaxCollectorFightersInformation toTaxCollectorFightersInformation(){
        return  new TaxCollectorFightersInformation(iden,
                current_fight.getDefenders()
                        .stream()
                        .map(fighter1 -> fighter1.toCharacterMinimalPlusLookInformation())
                        .toArray(CharacterMinimalPlusLookInformation[]::new),
                current_fight.getTeam1().getFighters()
                        .filter(fighter -> fighter instanceof CharacterFighter)
                        .map(fighter1 -> fighter1.getPlayer().toCharacterMinimalPlusLookInformation())
                        .toArray(CharacterMinimalPlusLookInformation[]::new)
        );
    }


    public TaxCollectorBasicInformations toTaxCollectorBasicInformations(){
        return new TaxCollectorBasicInformations(firstName,
                lastName, getDofusMap().coordinates().worldX,
                getDofusMap().coordinates().worldY,
                getDofusMap().getId(),
                getDofusMap().getSubAreaId()
        );
    }

    public TaxCollectorInformations toTaxCollectorInformations() {
        return new TaxCollectorInformations(iden,
                firstName,
                lastName,
                new AdditionalTaxCollectorInformations(callerName, (int) System.currentTimeMillis()),
                getDofusMap().coordinates().worldX,
                getDofusMap().coordinates().worldY,
                getDofusMap().getSubAreaId(),
                currentState(),
                PERCEPTEUR,
                new TaxCollectorComplementaryInformations[] {
                        new TaxCollectorWaitingForHelpInformations(new ProtectedEntityWaitingForHelpInfo(30000, 20000, (byte)3)),

                        //new TaxCollectorGuildInformations(guildInformations),
                        /*state == TaxCollectorStateEnum.STATE_WAITING_FOR_HELP ?
                        new TaxCollectorWaitingForHelpInformations(new ProtectedEntityWaitingForHelpInfo(int timeLeftBeforeFight, int waitTimeForPlacement, byte nbPositionForDefensors)) :
                        */new TaxCollectorLootInformations(kamas,experience,getBagSize(),getItemValues())
                }
                );
    }
}

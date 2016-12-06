package koh.game.entities.actors;

import koh.game.entities.guilds.Guild;
import koh.look.EntityLookParser;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.GameContextActorInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayTaxCollectorInformations;
import koh.protocol.types.game.context.roleplay.GuildInformations;
import koh.protocol.types.game.context.roleplay.TaxCollectorStaticInformation;
import koh.protocol.types.game.look.EntityLook;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class TaxCollector extends IGameActor {

    final static private EntityLook PERCEPTEUR = EntityLookParser.fromString("{714|||110}");
    @Getter
    private short cellID;
    private final GuildInformations guildInformations;
    private final Guild guild;
    private final int firstName,lastName, mapid, iden;
    private long experience;
    private int kamas,level;


    public TaxCollector(short cellID, Guild guild, int firstName, int lastName, int mapid, int iden, long experience, int kamas, int level) {
        this.cellID = cellID;
        this.guild = guild;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mapid = mapid;
        this.iden = iden;
        this.experience = experience;
        this.kamas = kamas;
        this.level = level;
        this.guildInformations = new GuildInformations(guild.getEntity().guildID, guild.getEntity().name, guild.getGuildEmblem());
    }


    @Override
    public GameContextActorInformations getGameContextActorInformations(Player character) {
        if(cache == null)
           cache = new GameRolePlayTaxCollectorInformations(this.ID, PERCEPTEUR, new EntityDispositionInformations(this.cellID,this.direction), new TaxCollectorStaticInformation(firstName, lastName, guildInformations), (byte) guild.getEntity().level, 0);
        return cache;
    }


    @Override
    protected EntityLook getEntityLook() {
        return PERCEPTEUR;
    }

    private GameRolePlayTaxCollectorInformations cache;



    public static final int BASE_HEALTH = 3000;
}

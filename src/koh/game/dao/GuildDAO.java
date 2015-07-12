package koh.game.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.table.TableUtils;
import java.util.HashMap;
import java.util.List;
import koh.game.Main;
import static koh.game.dao.PetsDAO.doOpenConnectionSource;
import koh.game.entities.guild.*;
import koh.protocol.types.game.guild.GuildEmblem;

/**
 *
 * @author Neo-Craft
 */
public class GuildDAO {

    public static Dao<GuildEntity, Integer> guildsDao;
    public static Dao<GuildMember, Integer> guildsMembersDao;
    public static volatile int NextGuildID = 1;

    public static final HashMap<Integer, Guild> Cache = new HashMap<>();

    public static int FindAll() {
        try {
            doOpenConnectionSource();
            guildsDao.queryForAll().forEach(x -> {
                {
                    Cache.put(x.GuildID, new Guild(x));
                }
            });
            return Cache.size();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void InitNextKey() {
        try {
            GenericRawResults<String[]> rawResults = guildsDao.queryRaw("select MAX(id) from guilds");
            List<String[]> results = rawResults.getResults();
            String[] resultArray = results.get(0);
            if (resultArray == null || resultArray[0] == null) {
                NextGuildID = 0;
            } else {
                NextGuildID = Integer.parseInt(resultArray[0]) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Guild HasGuild(int PlayerId) {
        try {
            GenericRawResults<String[]> rawResults = guildsDao.queryRaw("select guild_id from guilds_members WHERE char_id =" + PlayerId);
            return Cache.get(Integer.parseInt(rawResults.getResults().get(0)[0]));
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static boolean EmblemExist(GuildEmblem e){
        return GuildDAO.Cache.values().stream().map(x -> x.GetGuildEmblem()).anyMatch(x -> x.equals(e));
    }
    
    public static boolean NameExist(String name){
        return GuildDAO.Cache.values().stream().map(x -> x.Entity.Name.toLowerCase().trim()).anyMatch(x -> x.equalsIgnoreCase(name.toLowerCase().trim()));
    }

    public static Long FindMembers() {
        try {
            guildsMembersDao.queryForAll().forEach(x -> {
                {
                    try {
                        Cache.get(x.GuildID).Members.put(x.CharacterID, x);
                    } catch (NullPointerException e) {
                        Main.Logs().writeError(x.GuildID + " nulled for guildmember " + x.CharacterID);
                        e.printStackTrace();
                    }
                }
            });
            return guildsMembersDao.countOf();
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }
    
     public static void Remove(GuildMember Item) {
        try {
            guildsMembersDao.delete(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void Remove(GuildEntity Item) {
        try {
            Cache.remove(Item.GuildID);
            guildsDao.delete(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Insert(GuildEntity Item) {
        try {
            //TableUtils.createTable(PetsDAO.connectionSource, GuildMember.class);
            guildsDao.create(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void Insert(GuildMember Item) {
        try {
            guildsMembersDao.create(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
     public static void Update(GuildMember Item) {
        try {
            guildsMembersDao.update(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     
     public static void Update(GuildEntity Item) {
        try {
            guildsDao.update(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

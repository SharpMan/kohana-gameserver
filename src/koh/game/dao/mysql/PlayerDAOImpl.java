package koh.game.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.inject.Inject;
import koh.game.dao.DAO;
import koh.game.dao.DatabaseSource;
import koh.game.utils.sql.ConnectionStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import koh.d2o.Couple;
import koh.game.Main;
import koh.game.MySQL;
import static koh.game.MySQL.executeQuery;

import koh.game.dao.api.PlayerDAO;
import koh.game.entities.Account;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.JobBook;
import koh.game.entities.actors.character.MountInformations;
import koh.game.entities.actors.character.ScoreType;
import koh.game.entities.actors.character.ShortcutBook;
import koh.game.entities.actors.character.SpellBook;
import koh.game.utils.sql.ConnectionResult;
import koh.look.EntityLookParser;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.utils.Enumerable;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Neo-Craft
 */
public class PlayerDAOImpl extends PlayerDAO {

    private static final Logger logger = LogManager.getLogger(PlayerDAO.class);

    private final Map<Integer, Player> myCharacterById = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Player> myCharacterByName = Collections.synchronizedMap(new HashMap<>());
    public final List<Integer> accountInUnload = Collections.synchronizedList(new ArrayList<Integer>()); //TO : CopyOnWriteArrayList ?
    private final ConcurrentLinkedQueue<Couple<Long, Player>> myCharacterByTime = new ConcurrentLinkedQueue();

    @Inject
    private DatabaseSource dbSource;

    static { //Je le supprime pas juste pour te montrer a quoi sa refert
        Timer thread = new Timer();
        thread.schedule(new TimerTask() {

            @Override
            public void run() {
                List<Couple<Long, Player>> copy = new ArrayList<>();
                synchronized (myCharacterByTime) {
                    copy.addAll(myCharacterByTime);
                }
                for (Couple<Long, Player> ref : copy) {
                    if (System.currentTimeMillis() > ref.first && !(ref.second.account.characters != null && ref.second.account.characters.stream().anyMatch(Character -> Character.getFighter() != null))) { //On décharge pas les combattants.
                        synchronized (accountInUnload) {
                            delCharacter(ref.second); // We sould rethink of this if their commit clear this field and they wasn't finishied clearing ..
                            accountInUnload.add(ref.second.account.id);
                            cleanMap(ref.second);
                            logger.debug("player " + ref.second.nickName + " is going to be cleared" + DateFormat.getInstance().format(ref.first));
                            ref.second.save(true);
                            MySQL.needCommit = true;
                        }
                    }
                }
                copy.clear();
                copy = null;
            }
        }, 5 * 1000 * 60, 5 * 1000 * 60);
    }

    public static final int MaxCharacterSlot = 5;

    private void cleanMap(Player p) {
        for (Couple<Long, Player> cp : myCharacterByTime) {
            if (cp.second == p) {
                myCharacterByTime.remove(cp);
            }
        }
    }

    @Override
    public void getByAccount(Account account) throws Exception {
        synchronized (myCharacterByTime) {
            account.characters = new ArrayList<>(5);
            Player p = null;
            try (ConnectionResult conn = dbSource.executeQuery("SELECT * FROM `character` WHERE owner = '" + account.id + "';", 0)) {
                ResultSet result = conn.getResult();

                while (result.next()) {
                    if (accountInUnload.contains(account.id)) {
                        throw new AccountOccupedException("player id " + account.id + " are unload handled ");
                    }
                    if (myCharacterById.containsKey(result.getInt("id"))) {
                        p = myCharacterById.get(result.getInt("id"));
                        cleanMap(p);
                        if (p.account != null && p.account.accountData != null) {
                            account.accountData = p.account.accountData;
                        }
                        account.characters.add(p);
                        continue;
                    }
                    p = new Player() {
                        {
                            ID = result.getInt("id");
                            nickName = result.getString("nickname");
                            breed = (byte) result.getInt("breed");
                            sexe = result.getInt("sex");
                            skins = new ArrayList<Short>() {
                                {
                                    for (String c : result.getString("skins").split(",")) {
                                        add(Short.parseShort(c));
                                    }
                                    /* add(result.getShort("morph"));
                                     add(result.getShort("cosmetic"));*/
                                }
                            };
                            for (String c : result.getString("colors").split(",")) {
                                indexedColors.add(Integer.parseInt(c));
                            }
                            scales = new ArrayList<Short>() {
                                {
                                    for (String c : result.getString("scales").split(",")) {
                                        add(Short.parseShort(c));
                                    }
                                }

                            };
                            level = result.getInt("level");
                            account = account;
                            mapid = result.getInt("map");
                            this.currentMap = DAO.getMaps().findTemplate(mapid);

                            if (currentMap != null) {
                                currentMap.Init();
                            }
                            cell = currentMap.getCell(result.getShort("cell"));
                            for (String s : result.getString("chat_channels").split(",")) {
                                ennabledChannels.add(Byte.parseByte(s));
                            }
                            statPoints = result.getInt("stat_points");
                            spellPoints = result.getInt("spell_points");
                            vitality = Integer.parseInt(result.getString("stats").split(",")[0]);
                            wisdom = Integer.parseInt(result.getString("stats").split(",")[1]);
                            strength = Integer.parseInt(result.getString("stats").split(",")[2]);
                            intell = Integer.parseInt(result.getString("stats").split(",")[3]);
                            agility = Integer.parseInt(result.getString("stats").split(",")[4]);
                            chance = Integer.parseInt(result.getString("stats").split(",")[5]);
                            life = Integer.parseInt(result.getString("stats").split(",")[6]);
                            experience = Long.parseLong(result.getString("stats").split(",")[7]);
                            activableTitle = Short.parseShort(result.getString("stats").split(",")[8]);
                            activableOrnament = Short.parseShort(result.getString("stats").split(",")[9]);

                            this.setHonor(result.getInt("honor_points"), false);
                            alignmentValue = Byte.parseByte(result.getString("alignment_informations").split(",")[0]);
                            PvPEnabled = Byte.parseByte(result.getString("alignment_informations").split(",")[1]);
                            alignmentSide = AlignmentSideEnum.valueOf(Byte.parseByte(result.getString("alignment_informations").split(",")[2]));
                            dishonor = Integer.parseInt(result.getString("alignment_informations").split(",")[3]);

                            mySpells = new SpellBook() {
                                {
                                    this.deserializeEffects(result.getBytes("spells"));
                                }
                            };
                            myJobs = new JobBook() {
                                {
                                    this.deserializeEffects(result.getBytes("job_informations"));
                                }
                            };
                            shortcuts = ShortcutBook.deserialize(result.getBytes("shortcuts"));
                            kamas = result.getInt("kamas");
                            savedMap = Integer.parseInt(result.getString("savedpos").split(",")[0]);
                            savedCell = Short.parseShort(result.getString("savedpos").split(",")[1]);
                            if (!result.getString("entity_look").isEmpty()) {
                                this.entityLook = EntityLookParser.fromString(result.getString("entity_look"));
                            }
                            emotes = Enumerable.StringToByteArray(result.getString("emotes"));
                            if (result.getString("tinsel").split(";").length == 1) {
                                ornaments = Enumerable.StringToIntArray(result.getString("tinsel").split(";")[0]);
                                titles = new int[0];
                            } else if (result.getString("tinsel").split(";").length >= 2) {
                                ornaments = Enumerable.StringToIntArray(result.getString("tinsel").split(";")[0]);
                                titles = Enumerable.StringToIntArray(result.getString("tinsel").split(";")[1]);
                            } else {
                                titles = ornaments = new int[0];
                            }
                            this.mountInfo = new MountInformations(this);
                            if (result.getBytes("mount_informations") != null) {
                                this.mountInfo.deserialize(result.getBytes("mount_informations"));
                            }
                            this.scores.put(ScoreType.PVP_WIN, Integer.parseInt(result.getString("scores").split(",")[0]));
                            this.scores.put(ScoreType.PVP_LOOSE, Integer.parseInt(result.getString("scores").split(",")[1]));
                            this.scores.put(ScoreType.ARENA_WIN, Integer.parseInt(result.getString("scores").split(",")[2]));
                            this.scores.put(ScoreType.ARENA_LOOSE, Integer.parseInt(result.getString("scores").split(",")[3]));
                            this.scores.put(ScoreType.PVM_WIN, Integer.parseInt(result.getString("scores").split(",")[4]));
                            this.scores.put(ScoreType.PVM_LOOSE, Integer.parseInt(result.getString("scores").split(",")[5]));
                            this.scores.put(ScoreType.PVP_TOURNAMENT, Integer.parseInt(result.getString("scores").split(",")[6]));

                        }
                    };
                    account.characters.add(p);
                    addCharacter(p);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
                throw new Exception(); //FIXME: something refeer
            }
        }
    }

    @Override
    public boolean update(Player character, boolean clear) {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `character` set breed = ?,skins = ?,scales = ?,sex = ?,level = ?,colors = ?,map = ?,cell = ?,chat_channels = ?,stat_points = ?,spell_points = ?,stats = ?,spells = ?,kamas = ?,shortcuts = ?,savedpos = ? ,entity_look = ?,emotes = ?,tinsel = ?,mount_informations = ?,job_informations = ?,honor_points = ?, alignment_informations = ?, scores = ? WHERE id = ?;")) {
            PreparedStatement pStatement = conn.getStatement();
            pStatement.setInt(1, character.breed);
            pStatement.setString(2, StringUtils.join(character.skins, ','));
            pStatement.setString(3, StringUtils.join(character.scales, ','));
            pStatement.setInt(4, character.sexe);
            pStatement.setInt(5, character.level);
            pStatement.setString(6, StringUtils.join(character.indexedColors, ','));
            pStatement.setInt(7, character.currentMap.id);
            pStatement.setInt(8, character.cell.id);
            pStatement.setString(9, StringUtils.join(character.ennabledChannels, ','));
            pStatement.setInt(10, character.statPoints);
            pStatement.setInt(11, character.spellPoints);
            pStatement.setString(12, character.vitality + "," + character.wisdom + "," + character.strength + "," + character.intell + "," + character.agility + "," + character.chance + "," + character.life + "," + character.experience + "," + character.activableTitle + "," + character.activableOrnament);
            pStatement.setBytes(13, character.mySpells.serialize());
            pStatement.setLong(14, character.kamas);
            pStatement.setBytes(15, character.shortcuts.serialize());
            pStatement.setString(16, character.savedMap + "," + character.savedCell);
            pStatement.setString(17, EntityLookParser.ConvertToString(character.getEntityLook()));
            pStatement.setString(18, Enumerable.Join(character.emotes, ','));
            pStatement.setString(19, Enumerable.Join(character.ornaments, ',') + ";" + Enumerable.Join(character.titles, ','));
            pStatement.setBytes(20, character.mountInfo.serialize());
            pStatement.setBytes(21, character.myJobs.serialize());
            pStatement.setInt(22, character.honor);
            pStatement.setString(23, character.alignmentValue + "," + character.PvPEnabled + "," + character.alignmentSide.value + "," + character.dishonor);
            pStatement.setString(24, StringUtils.join(character.scores.values(), ','));
            pStatement.setInt(25, character.ID);

            pStatement.executeUpdate();
            if (clear) {
                character.totalClear();
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean add(Player character) {

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `character` (`id`,`owner`,`nickname`,`breed`,`skins`,`scales`,`sex`,`level`,`colors`,`map`,`cell`,`chat_channels`,`stat_points`,`spell_points`,`stats`,`spells`,`shortcuts`,`savedpos`,`entity_look`,`emotes`,`tinsel`,`job_informations`,`honor_points`,`alignment_informations`,`scores`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);",true)) {
            PreparedStatement pStatement = conn.getStatement();

            pStatement.setInt(1, character.ID);
            pStatement.setInt(2, character.owner);
            pStatement.setString(3, character.nickName);
            pStatement.setInt(4, character.breed);
            pStatement.setString(5, StringUtils.join(character.skins, ','));
            pStatement.setString(6, StringUtils.join(character.scales, ','));
            pStatement.setInt(7, character.sexe);
            pStatement.setInt(8, character.level);
            pStatement.setString(9, StringUtils.join(character.indexedColors, ','));
            pStatement.setInt(10, character.mapid);
            pStatement.setInt(11, character.cell.id);
            pStatement.setString(12, StringUtils.join(character.ennabledChannels, ','));
            pStatement.setInt(13, character.statPoints);
            pStatement.setInt(14, character.spellPoints);
            pStatement.setString(15, character.vitality + "," + character.wisdom + "," + character.strength + "," + character.intell + "," + character.agility + "," + character.chance + "," + character.life + "," + character.experience + "," + character.activableTitle + "," + character.activableOrnament);
            pStatement.setBytes(16, character.mySpells.serialize());
            pStatement.setBytes(17, character.shortcuts.serialize());
            pStatement.setString(18, character.savedMap + "," + character.savedCell);
            pStatement.setString(19, EntityLookParser.ConvertToString(character.getEntityLook()));
            pStatement.setString(20, Enumerable.Join(character.emotes, ','));
            pStatement.setString(21, Enumerable.Join(character.ornaments, ',') + ";" + Enumerable.Join(character.titles, ','));
            pStatement.setBytes(22, character.myJobs.serialize());
            pStatement.setInt(23, character.honor);
            pStatement.setString(24, character.alignmentValue + "," + character.PvPEnabled + "," + character.alignmentSide.value + "," + character.dishonor);
            pStatement.setString(25, StringUtils.join(character.scores.values(), ','));
            pStatement.execute();
            ResultSet resultSet = pStatement.getGeneratedKeys();
            if(!resultSet.first())//character not created ?
                return false;
            character.ID = resultSet.getInt(1);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean containsName(String name) {
        if(myCharacterByName.get(name.toLowerCase()) != null)
            return true;//if already loaded

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("SELECT 1 FROM `character` WHERE LOWER(nickname) = LOWER(?);")) {
            PreparedStatement pStatement = conn.getStatement();

            //FIXME SELECT 1 FROM characters WHERE name=@0
            pStatement.setString(1, name);

            return pStatement.executeQuery().first();
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return false;
    }

    @Override
    public void addCharacter(Player character) {
        myCharacterById.put(character.ID, character);
        myCharacterByName.put(character.nickName.toLowerCase(), character);
    }

    @Override
    public Player getCharacterByAccount(int id) {
       return myCharacterById.values().stream().filter(x -> x.client != null && x.account != null && x.account.id == id).findFirst().orElse(null);

    }

    @Override
    public void delCharacter(Player character) {
        myCharacterById.remove(character.ID);
        myCharacterByName.remove(character.nickName.toLowerCase());
    }

    @Override
    public Player getCharacter(Integer characterId) {
        return myCharacterById.get(characterId);
    }

    @Override
    public Player getCharacter(String characterName) {
        return myCharacterByName.get(characterName.toLowerCase());
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }
}

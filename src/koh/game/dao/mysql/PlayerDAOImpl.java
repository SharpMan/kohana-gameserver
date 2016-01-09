package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.d2o.Couple;
import koh.game.MySQL;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.MapDAO;
import koh.game.dao.api.PlayerDAO;
import koh.game.entities.Account;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.JobBook;
import koh.game.entities.actors.character.MountInformations;
import koh.game.entities.actors.character.ShortcutBook;
import koh.game.entities.actors.character.SpellBook;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.look.EntityLookParser;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.utils.Enumerable;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * @author Neo-Craft
 */
public class PlayerDAOImpl extends PlayerDAO {

    public static final int MAX_CHARACTER_SLOT = 5;

    private static final Logger logger = LogManager.getLogger(PlayerDAO.class);

    @Inject
    private MapDAO maps;


    private final void scheduleLoader(){
        Timer thread = new Timer();
        thread.schedule(new TimerTask() {

            @Override
            public void run() {
                List<Couple<Long, Player>> copy = new ArrayList<>();
                synchronized (characterUnloadQueue) {
                    copy.addAll(characterUnloadQueue);
                }
                for (Couple<Long, Player> ref : copy) {
                    if (System.currentTimeMillis() > ref.first && !(ref.second.getAccount().characters != null && ref.second.getAccount().characters.stream().anyMatch(Character -> Character.getFighter() != null))) { //On décharge pas les combattants.
                        synchronized (accountInUnload) {
                            delCharacter(ref.second); // We sould rethink of this if their commit clear this field and they wasn't finishied clearing ..
                            accountInUnload.add(ref.second.getAccount().id);
                            cleanMap(ref.second);
                            logger.debug("player " + ref.second.getNickName() + " is going to be cleared" + DateFormat.getInstance().format(ref.first));
                            ref.second.save(true);
                            MySQL.needCommit = true;
                        }
                    }
                }
                accountInUnload.clear(); //Should be done after commit
                copy.clear();
                copy = null;
            }
        }, 5 * 1000 * 60, 5 * 1000 * 60);
    }

    public final List<Integer> accountInUnload = Collections.synchronizedList(new ArrayList<Integer>()); //TO : CopyOnWriteArrayList ?
    private final Map<Integer, Player> myCharacterById = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Player> myCharacterByName = Collections.synchronizedMap(new HashMap<>());
    private final ConcurrentLinkedQueue<Couple<Long, Player>> characterUnloadQueue = new ConcurrentLinkedQueue();



    @Inject
    private DatabaseSource dbSource;

    @Override
    public Stream<Couple<Long, Player>> getQueueAsSteam(){
        return this.characterUnloadQueue.stream();
    }

    @Override
    public void addCharacterInQueue(Couple<Long, Player> charr){
        this.characterUnloadQueue.add(charr);
    }

    @Override
    public boolean isCurrentlyOnProcess(int accountId){
        return accountInUnload.contains(accountId);
    }


    private void cleanMap(Player p) {
        for (Couple<Long, Player> cp : characterUnloadQueue) {
            if (cp.second == p) {
                characterUnloadQueue.remove(cp);
            }
        }
    }

    private ArrayList<Short> stringToShortArray(String tr) {
        return new ArrayList<Short>() {
            {
                for (String c : tr.split(",")) {
                    add(Short.parseShort(c));
                }
            }
        };
    }

    @Override
    public void getByAccount(Account account) throws Exception {
        synchronized (characterUnloadQueue) {
            account.characters = new ArrayList<>(5);
            try (ConnectionResult conn = dbSource.executeQuery("SELECT * FROM `character` WHERE owner = '" + account.id + "';", 0)) {
                ResultSet result = conn.getResult();

                while (result.next()) {
                    final Player p;
                    if (accountInUnload.contains(account.id)) {
                        throw new AccountOccupedException("player id " + account.id + " are unload handled ");
                    }
                    if (myCharacterById.containsKey(result.getInt("id"))) {
                        p = myCharacterById.get(result.getInt("id"));
                        cleanMap(p);
                        if (p.getAccount() != null && p.getAccount().accountData != null) {
                            account.accountData = p.getAccount().accountData;
                        }
                        account.characters.add(p);
                        continue;
                    }
                    p = Player.builder()
                            .nickName(result.getString("nickname"))
                            .breed((byte) result.getInt("breed"))
                            .regenRate((byte)10)
                            .sexe(result.getInt("sex"))
                            .skins(stringToShortArray(result.getString("skins")))
                            .scales(stringToShortArray(result.getString("scales")))
                            .level(result.getInt("level"))
                            .account(account)
                            .currentMap(maps.findTemplate(result.getInt("map")).init$Return())
                            .spellPoints(result.getInt("spell_points"))
                            .statPoints(result.getInt("stat_points"))
                            .vitality(Integer.parseInt(result.getString("stats").split(",")[0]))
                            .wisdom(Integer.parseInt(result.getString("stats").split(",")[1]))
                            .strength(Integer.parseInt(result.getString("stats").split(",")[2]))
                            .intell(Integer.parseInt(result.getString("stats").split(",")[3]))
                            .agility(Integer.parseInt(result.getString("stats").split(",")[4]))
                            .chance(Integer.parseInt(result.getString("stats").split(",")[5]))
                            .life(Integer.parseInt(result.getString("stats").split(",")[6]))
                            .experience(Long.parseLong(result.getString("stats").split(",")[7]))
                            .activableTitle(Short.parseShort(result.getString("stats").split(",")[8]))
                            .activableOrnament(Short.parseShort(result.getString("stats").split(",")[9]))
                            .regenStartTime(Long.parseLong(result.getString("stats").split(",")[10]))
                            .alignmentValue(Byte.parseByte(result.getString("alignment_informations").split(",")[0]))
                            .PvPEnabled(Byte.parseByte(result.getString("alignment_informations").split(",")[1]))
                            .alignmentSide(AlignmentSideEnum.valueOf(Byte.parseByte(result.getString("alignment_informations").split(",")[2])))
                            .dishonor(Integer.parseInt(result.getString("alignment_informations").split(",")[3]))
                            .mySpells(new SpellBook() {
                                {
                                    this.deserializeEffects(result.getBytes("spells"));
                                }
                            })
                            .myJobs(new JobBook() {
                                {
                                    this.deserializeEffects(result.getBytes("job_informations"));
                                }
                            })
                            .shortcuts(ShortcutBook.deserialize(result.getBytes("shortcuts")))
                            .kamas(result.getInt("kamas"))
                            .savedMap(Integer.parseInt(result.getString("savedpos").split(",")[0]))
                            .savedCell(Short.parseShort(result.getString("savedpos").split(",")[1]))
                            .emotes(Enumerable.StringToByteArray(result.getString("emotes")))
                            .mountInfo(new MountInformations().deserialize(result.getBytes("mount_informations")))
                            .moodSmiley((byte)-1)
                            .build();
                    Arrays.stream(result.getString("colors").split(",")).forEach(c -> p.getIndexedColors().add(Integer.parseInt(c)));
                    p.setMapid(result.getInt("map"));
                    p.setHonor(result.getInt("honor_points"), false);
                    p.initScore(result.getString("scores"));
                    p.setActorCell(p.getCurrentMap().getCell(result.getShort("cell")));
                    p.getMountInfo().setPlayer(p);
                    for (String s : result.getString("chat_channels").split(",")) {
                        p.getEnabledChannels().add(Byte.parseByte(s));
                    }
                    if (!result.getString("entity_look").isEmpty()) {
                        p.setEntityLook(EntityLookParser.fromString(result.getString("entity_look")));
                    }
                    if (result.getString("tinsel").split(";").length == 1) {
                        p.setOrnaments(Enumerable.StringToIntArray(result.getString("tinsel").split(";")[0]));
                        p.setTitles(new int[0]);
                    } else if (result.getString("tinsel").split(";").length >= 2) {
                        p.setOrnaments(Enumerable.StringToIntArray(result.getString("tinsel").split(";")[0]));
                        p.setTitles(Enumerable.StringToIntArray(result.getString("tinsel").split(";")[1]));
                    } else {
                        p.setTitles(new int[0]);
                        p.setOrnaments(new int[0]);
                    }
                    p.setID(result.getInt("id"));
                    account.characters.add(p);
                    addCharacter(p);
                }
            }
            catch(AccountOccupedException ex){
                throw new AccountOccupedException(ex.getMessage());
            }
            catch (Exception e) {
                e.printStackTrace();
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
            pStatement.setInt(1, character.getBreed());
            pStatement.setString(2, StringUtils.join(character.getSkins(), ','));
            pStatement.setString(3, StringUtils.join(character.getScales(), ','));
            pStatement.setInt(4, character.getSexe());
            pStatement.setInt(5, character.getLevel());
            pStatement.setString(6, StringUtils.join(character.getIndexedColors(), ','));
            pStatement.setInt(7, character.getCurrentMap().getId());
            pStatement.setInt(8, character.getCell().getId());
            pStatement.setString(9, StringUtils.join(character.getEnabledChannels(), ','));
            pStatement.setInt(10, character.getStatPoints());
            pStatement.setInt(11, character.getSpellPoints());
            pStatement.setString(12, character.serializeStats());
            pStatement.setBytes(13, character.getMySpells().serialize());
            pStatement.setLong(14, character.getKamas());
            pStatement.setBytes(15, character.getShortcuts().serialize());
            pStatement.setString(16, character.getSavedMap() + "," + character.getSavedCell());
            pStatement.setString(17, EntityLookParser.ConvertToString(character.getEntityLook()));
            pStatement.setString(18, Enumerable.Join(character.getEmotes(), ','));
            pStatement.setString(19, Enumerable.Join(character.getOrnaments(), ',') + ";" + Enumerable.Join(character.getTitles(), ','));
            pStatement.setBytes(20, character.getMountInfo().serialize());
            pStatement.setBytes(21, character.getMyJobs().serialize());
            pStatement.setInt(22, character.getHonor());
            pStatement.setString(23, character.getAlignmentValue() + "," + character.getPvPEnabled() + "," + character.getAlignmentSide().value + "," + character.getDishonor());
            pStatement.setString(24, StringUtils.join(character.getScores().values(), ','));
            pStatement.setInt(25, character.getID());

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

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `character` (`id`,`owner`,`nickname`,`breed`,`skins`,`scales`,`sex`,`level`,`colors`,`map`,`cell`,`chat_channels`,`stat_points`,`spell_points`,`stats`,`spells`,`shortcuts`,`savedpos`,`entity_look`,`emotes`,`tinsel`,`job_informations`,`honor_points`,`alignment_informations`,`scores`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);", true)) {
            PreparedStatement pStatement = conn.getStatement();

            pStatement.setInt(1, character.getID());
            pStatement.setInt(2, character.getOwner());
            pStatement.setString(3, character.getNickName());
            pStatement.setInt(4, character.getBreed());
            pStatement.setString(5, StringUtils.join(character.getSkins(), ','));
            pStatement.setString(6, StringUtils.join(character.getScales(), ','));
            pStatement.setInt(7, character.getSexe());
            pStatement.setInt(8, character.getLevel());
            pStatement.setString(9, StringUtils.join(character.getIndexedColors(), ','));
            pStatement.setInt(10, character.getMapid());
            pStatement.setInt(11, character.getCell().getId());
            pStatement.setString(12, StringUtils.join(character.getEnabledChannels(), ','));
            pStatement.setInt(13, character.getStatPoints());
            pStatement.setInt(14, character.getSpellPoints());
            pStatement.setString(15, character.serializeStats());
            pStatement.setBytes(16, character.getMySpells().serialize());
            pStatement.setBytes(17, character.getShortcuts().serialize());
            pStatement.setString(18, character.getSavedMap() + "," + character.getSavedCell());
            pStatement.setString(19, EntityLookParser.ConvertToString(character.getEntityLook()));
            pStatement.setString(20, Enumerable.Join(character.getEmotes(), ','));
            pStatement.setString(21, Enumerable.Join(character.getOrnaments(), ',') + ";" + Enumerable.Join(character.getTitles(), ','));
            pStatement.setBytes(22, character.getMyJobs().serialize());
            pStatement.setInt(23, character.getHonor());
            pStatement.setString(24, character.getAlignmentValue() + "," + character.getPvPEnabled() + "," + character.getAlignmentSide().value + "," + character.getDishonor());
            pStatement.setString(25, StringUtils.join(character.getScores().values(), ','));
            pStatement.execute();
            ResultSet resultSet = pStatement.getGeneratedKeys();
            if (!resultSet.first())//character not created ?
                return false;
            character.setID(resultSet.getInt(1));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean containsName(String name) {
        if (myCharacterByName.get(name.toLowerCase()) != null)
            return true;//if already loaded

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("SELECT 1 FROM `character` WHERE LOWER(nickname) = LOWER(?);")) {
            PreparedStatement pStatement = conn.getStatement();
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
        myCharacterById.put(character.getID(), character);
        myCharacterByName.put(character.getNickName().toLowerCase(), character);
    }

    @Override
    public Player getCharacterByAccount(int id) {
        return myCharacterById.values().stream().filter(x -> x.getClient() != null && x.getAccount() != null && x.getAccount().id == id).findFirst().orElse(null);

    }

    @Override
    public void delCharacter(Player character) {
        myCharacterById.remove(character.getID());
        myCharacterByName.remove(character.getNickName().toLowerCase());
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
        this.scheduleLoader();
    }

    @Override
    public void stop() {

    }
}

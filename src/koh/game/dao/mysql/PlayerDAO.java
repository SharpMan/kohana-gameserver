package koh.game.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import koh.d2o.Couple;
import koh.game.Main;
import koh.game.MySQL;
import static koh.game.MySQL.executeQuery;
import koh.game.entities.Account;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.JobBook;
import koh.game.entities.actors.character.MountInformations;
import koh.game.entities.actors.character.ScoreType;
import koh.game.entities.actors.character.ShortcutBook;
import koh.game.entities.actors.character.SpellBook;
import koh.game.utils.Settings;
import koh.look.EntityLookParser;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.utils.Enumerable;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Neo-Craft
 */
public class PlayerDAO {

    private static final Map<Integer, Player> myCharacterById = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Player> myCharacterByName = Collections.synchronizedMap(new HashMap<>());
    public static final List<Integer> AccountInUnload = Collections.synchronizedList(new ArrayList<Integer>()); //TO : CopyOnWriteArrayList ?
    public static final ConcurrentLinkedQueue<Couple<Long, Player>> myCharacterByTime = new ConcurrentLinkedQueue();

    static {
        Timer thread = new Timer();
        thread.schedule(new TimerTask() {

            @Override
            public void run() {
                List<Couple<Long, Player>> copy = new ArrayList<>();
                synchronized (myCharacterByTime) {
                    copy.addAll(myCharacterByTime);
                }
                for (Couple<Long, Player> ref : copy) {
                    if (System.currentTimeMillis() > ref.first && !(ref.second.Account.Characters != null && ref.second.Account.Characters.stream().anyMatch(Character -> Character.GetFighter() != null))) { //On décharge pas les combattants.
                        synchronized (AccountInUnload) {
                            DelCharacter(ref.second); // We sould rethink of this if their commit clear this field and they wasn't finishied clearing ..
                            AccountInUnload.add(ref.second.Account.ID);
                            cleanMap(ref.second);
                            Main.Logs().writeInfo("Player " + ref.second.NickName + " is going to be cleared" + DateFormat.getInstance().format(ref.first));
                            ref.second.Save(true);
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
    public static volatile int NextID;

    public static void cleanMap(Player p) {
        for (Couple<Long, Player> cp : myCharacterByTime) {
            if (cp.second == p) {
                myCharacterByTime.remove(cp);
            }
        }
    }

    public static void FindAll(Account account) throws Exception {
        try {
            synchronized (myCharacterByTime) {
                account.Characters = new ArrayList<>(10);
                ResultSet RS = MySQL.executeQuery("SELECT * FROM `character` WHERE owner = '" + account.ID + "';", Settings.GetStringElement("Database.Name"), 0);
                Player p = null;
                while (RS.next()) {
                    if (AccountInUnload.contains(account.ID)) {
                        throw new AccountOccupedException("Player id " + account.ID + " are unload handled ");
                    }
                    if (myCharacterById.containsKey(RS.getInt("ID"))) {
                        p = myCharacterById.get(RS.getInt("id"));
                        cleanMap(p);
                        if (p.Account != null && p.Account.Data != null) {
                            account.Data = p.Account.Data;
                        }
                        account.Characters.add(p);
                        continue;
                    }
                    p = new Player() {
                        {
                            ID = RS.getInt("id");
                            NickName = RS.getString("nickname");
                            Breed = (byte) RS.getInt("breed");
                            Sexe = RS.getInt("sex");
                            Skins = new ArrayList<Short>() {
                                {
                                    for (String c : RS.getString("skins").split(",")) {
                                        add(Short.parseShort(c));
                                    }
                                    /* add(RS.getShort("morph"));
                                     add(RS.getShort("cosmetic"));*/
                                }
                            };
                            for (String c : RS.getString("colors").split(",")) {
                                IndexedColors.add(Integer.parseInt(c));
                            }
                            Scales = new ArrayList<Short>() {
                                {
                                    for (String c : RS.getString("scales").split(",")) {
                                        add(Short.parseShort(c));
                                    }
                                }

                            };
                            Level = RS.getInt("level");
                            Account = account;
                            Mapid = RS.getInt("map");
                            this.CurrentMap = MapDAOImpl.dofusMaps.get(Mapid);

                            if (CurrentMap != null) {
                                CurrentMap.Init();
                            }
                            Cell = CurrentMap.getCell(RS.getShort("cell"));
                            for (String s : RS.getString("chat_channels").split(",")) {
                                EnnabledChannels.add(Byte.parseByte(s));
                            }
                            StatPoints = RS.getInt("stat_points");
                            SpellPoints = RS.getInt("spell_points");
                            Vitality = Integer.parseInt(RS.getString("stats").split(",")[0]);
                            Wisdom = Integer.parseInt(RS.getString("stats").split(",")[1]);
                            Strength = Integer.parseInt(RS.getString("stats").split(",")[2]);
                            Intell = Integer.parseInt(RS.getString("stats").split(",")[3]);
                            Agility = Integer.parseInt(RS.getString("stats").split(",")[4]);
                            Chance = Integer.parseInt(RS.getString("stats").split(",")[5]);
                            Life = Integer.parseInt(RS.getString("stats").split(",")[6]);
                            Experience = Long.parseLong(RS.getString("stats").split(",")[7]);
                            activableTitle = Short.parseShort(RS.getString("stats").split(",")[8]);
                            activableOrnament = Short.parseShort(RS.getString("stats").split(",")[9]);

                            this.setHonor(RS.getInt("honor_points"), false);
                            AlignmentValue = Byte.parseByte(RS.getString("alignment_informations").split(",")[0]);
                            PvPEnabled = Byte.parseByte(RS.getString("alignment_informations").split(",")[1]);
                            AlignmentSide = AlignmentSideEnum.valueOf(Byte.parseByte(RS.getString("alignment_informations").split(",")[2]));
                            Dishonor = Integer.parseInt(RS.getString("alignment_informations").split(",")[3]);

                            mySpells = new SpellBook() {
                                {
                                    this.DeserializeEffects(RS.getBytes("spells"));
                                }
                            };
                            myJobs = new JobBook() {
                                {
                                    this.DeserializeEffects(RS.getBytes("job_informations"));
                                }
                            };
                            Shortcuts = ShortcutBook.Deserialize(RS.getBytes("shortcuts"));
                            Kamas = RS.getInt("kamas");
                            SavedMap = Integer.parseInt(RS.getString("savedpos").split(",")[0]);
                            SavedCell = Short.parseShort(RS.getString("savedpos").split(",")[1]);
                            if (!RS.getString("entity_look").isEmpty()) {
                                this.entityLook = EntityLookParser.fromString(RS.getString("entity_look"));
                            }
                            Emotes = Enumerable.StringToByteArray(RS.getString("emotes"));
                            if (RS.getString("tinsel").split(";").length == 1) {
                                Ornaments = Enumerable.StringToIntArray(RS.getString("tinsel").split(";")[0]);
                                Titles = new int[0];
                            } else if (RS.getString("tinsel").split(";").length >= 2) {
                                Ornaments = Enumerable.StringToIntArray(RS.getString("tinsel").split(";")[0]);
                                Titles = Enumerable.StringToIntArray(RS.getString("tinsel").split(";")[1]);
                            } else {
                                Titles = Ornaments = new int[0];
                            }
                            this.MountInfo = new MountInformations(this);
                            if (RS.getBytes("mount_informations") != null) {
                                this.MountInfo.Deserialize(RS.getBytes("mount_informations"));
                            }
                            this.Scores.put(ScoreType.PVP_WIN, Integer.parseInt(RS.getString("scores").split(",")[0]));
                            this.Scores.put(ScoreType.PVP_LOOSE, Integer.parseInt(RS.getString("scores").split(",")[1]));
                            this.Scores.put(ScoreType.ARENA_WIN, Integer.parseInt(RS.getString("scores").split(",")[2]));
                            this.Scores.put(ScoreType.ARENA_LOOSE, Integer.parseInt(RS.getString("scores").split(",")[3]));
                            this.Scores.put(ScoreType.PVM_WIN, Integer.parseInt(RS.getString("scores").split(",")[4]));
                            this.Scores.put(ScoreType.PVM_LOOSE, Integer.parseInt(RS.getString("scores").split(",")[5]));
                            this.Scores.put(ScoreType.PVP_TOURNAMENT, Integer.parseInt(RS.getString("scores").split(",")[6]));

                        }
                    };
                    account.Characters.add(p);
                    AddCharacter(p);
                }
                MySQL.closeResultSet(RS);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Main.Logs().writeError("Connexion a la DB Perdue, deconnexion du compte en connexion en attendant la reconnexion de la DB...");
            throw new Exception();
        }
    }

    public static boolean Update(Player character, boolean Clear) {
        try {
            PreparedStatement p = MySQL.prepareQuery("UPDATE `character` set breed = ?,skins = ?,scales = ?,sex = ?,level = ?,colors = ?,map = ?,cell = ?,chat_channels = ?,stat_points = ?,spell_points = ?,stats = ?,spells = ?,kamas = ?,shortcuts = ?,savedpos = ? ,entity_look = ?,emotes = ?,tinsel = ?,mount_informations = ?,job_informations = ?,honor_points = ?, alignment_informations = ?, scores = ? WHERE id = ?;", MySQL.Connection()); //Define Row to be updated...

            p.setInt(1, character.Breed);
            p.setString(2, StringUtils.join(character.Skins, ','));
            p.setString(3, StringUtils.join(character.Scales, ','));
            p.setInt(4, character.Sexe);
            p.setInt(5, character.Level);
            p.setString(6, StringUtils.join(character.IndexedColors, ','));
            p.setInt(7, character.CurrentMap.Id);
            p.setInt(8, character.Cell.Id);
            p.setString(9, StringUtils.join(character.EnnabledChannels, ','));
            p.setInt(10, character.StatPoints);
            p.setInt(11, character.SpellPoints);
            p.setString(12, character.Vitality + "," + character.Wisdom + "," + character.Strength + "," + character.Intell + "," + character.Agility + "," + character.Chance + "," + character.Life + "," + character.Experience + "," + character.activableTitle + "," + character.activableOrnament);
            p.setBytes(13, character.mySpells.Serialize());
            p.setLong(14, character.Kamas);
            p.setBytes(15, character.Shortcuts.Serialize());
            p.setString(16, character.SavedMap + "," + character.SavedCell);
            p.setString(17, EntityLookParser.ConvertToString(character.GetEntityLook()));
            p.setString(18, Enumerable.Join(character.Emotes, ','));
            p.setString(19, Enumerable.Join(character.Ornaments, ',') + ";" + Enumerable.Join(character.Titles, ','));
            p.setBytes(20, character.MountInfo.Serialize());
            p.setBytes(21, character.myJobs.Serialize());
            p.setInt(22, character.Honor);
            p.setString(23, character.AlignmentValue + "," + character.PvPEnabled + "," + character.AlignmentSide.value + "," + character.Dishonor);
            p.setString(24, StringUtils.join(character.Scores.values(), ','));
            p.setInt(25, character.ID);

            p.executeUpdate();

            MySQL.closePreparedStatement(p);
            if (Clear) {
                character.totalClear();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean Insert(Player character) {

        try {
            PreparedStatement p = MySQL.prepareQuery("INSERT INTO `character` (`id`,`owner`,`nickname`,`breed`,`skins`,`scales`,`sex`,`level`,`colors`,`map`,`cell`,`chat_channels`,`stat_points`,`spell_points`,`stats`,`spells`,`shortcuts`,`savedpos`,`entity_look`,`emotes`,`tinsel`,`job_informations`,`honor_points`,`alignment_informations`,`scores`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);", MySQL.Connection());

            p.setInt(1, character.ID);
            p.setInt(2, character.Owner);
            p.setString(3, character.NickName);
            p.setInt(4, character.Breed);
            p.setString(5, StringUtils.join(character.Skins, ','));
            p.setString(6, StringUtils.join(character.Scales, ','));
            p.setInt(7, character.Sexe);
            p.setInt(8, character.Level);
            p.setString(9, StringUtils.join(character.IndexedColors, ','));
            p.setInt(10, character.Mapid);
            p.setInt(11, character.Cell.Id);
            p.setString(12, StringUtils.join(character.EnnabledChannels, ','));
            p.setInt(13, character.StatPoints);
            p.setInt(14, character.SpellPoints);
            p.setString(15, character.Vitality + "," + character.Wisdom + "," + character.Strength + "," + character.Intell + "," + character.Agility + "," + character.Chance + "," + character.Life + "," + character.Experience + "," + character.activableTitle + "," + character.activableOrnament);
            p.setBytes(16, character.mySpells.Serialize());
            p.setBytes(17, character.Shortcuts.Serialize());
            p.setString(18, character.SavedMap + "," + character.SavedCell);
            p.setString(19, EntityLookParser.ConvertToString(character.GetEntityLook()));
            p.setString(20, Enumerable.Join(character.Emotes, ','));
            p.setString(21, Enumerable.Join(character.Ornaments, ',') + ";" + Enumerable.Join(character.Titles, ','));
            p.setBytes(22, character.myJobs.Serialize());
            p.setInt(23, character.Honor);
            p.setString(24, character.AlignmentValue + "," + character.PvPEnabled + "," + character.AlignmentSide.value + "," + character.Dishonor);
            p.setString(25, StringUtils.join(character.Scores.values(), ','));
            p.execute();
            MySQL.closePreparedStatement(p);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean DoesNameExist(String name) {
        boolean exist = false;
        try {
            //FIXME SELECT 1 FROM characters WHERE Name=@0
            PreparedStatement p = MySQL.prepareQuery("SELECT nickname FROM `character` WHERE nickname LIKE ?;", MySQL.Connection());
            p.setString(1, name);
            ResultSet RS = p.executeQuery();

            while (RS.next()) {
                if (RS.getString("nickname").toLowerCase().equals(name.toLowerCase())) {
                    exist = true;
                }
            }
            MySQL.closeResultSet(RS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return exist;
    }

    public synchronized static void InitializeNextIdentifiant() {
        try {
            ResultSet RS = executeQuery("SELECT id FROM `character` ORDER BY id DESC LIMIT 1;", Settings.GetStringElement("Database.Name"));
            if (!RS.first()) {
                NextID = 0;
            }
            NextID = RS.getInt("id");
            NextID++;
            MySQL.closeResultSet(RS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void AddCharacter(Player character) {
        myCharacterById.put(character.ID, character);
        myCharacterByName.put(character.NickName.toLowerCase(), character);
    }

    public static Player GetCharacterByAccount(int id) {
        try {
            return myCharacterById.values().stream().filter(x -> x.Client != null && x.Account != null && x.Account.ID == id).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public static void DelCharacter(Player character) {
        myCharacterById.remove(character.ID);
        myCharacterByName.remove(character.NickName.toLowerCase());
    }

    public static Player GetCharacter(Integer CharacterId) {
        return myCharacterById.get(CharacterId);
    }

    public static Player GetCharacter(String CharacterName) {
        return myCharacterByName.get(CharacterName.toLowerCase());
    }
}

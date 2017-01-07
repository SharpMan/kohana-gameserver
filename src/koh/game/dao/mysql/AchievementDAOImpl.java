package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.AchievementDAO;
import koh.game.entities.achievement.AchievementGoal;
import koh.game.entities.achievement.AchievementReward;
import koh.game.entities.achievement.AchievementTemplate;
import koh.game.entities.actors.character.AchievementBook;
import koh.game.entities.actors.character.SpellBook;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.utils.Enumerable;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Melancholia on 12/27/16. kauai sunset
 */
@Log4j2
public class AchievementDAOImpl extends AchievementDAO {

    //If succes contain debloquer succes faire liste

    private final Map<Integer, AchievementReward> rewards = new HashMap<>(1600);
    private final Map<Integer, AchievementGoal> goals = new HashMap<>(3800);
    private final Map<Integer, HashMap<Integer, AchievementTemplate>> achievementsCategory = new HashMap<Integer, HashMap<Integer, AchievementTemplate>>() {{
        this.put(3, new HashMap<>()); //Donjons
        this.put(4, new HashMap<>()); //Dopeuls
        this.put(5, new HashMap<>()); //Élevage
        this.put(6, new HashMap<>()); //Exploration
        this.put(7, new HashMap<>()); //Métiers
        this.put(8, new HashMap<>()); //Quêtes
        this.put(9, new HashMap<>()); //Événements
        this.put(11, new HashMap<>()); //Niveaux 1 à 50
        this.put(12, new HashMap<>()); //Niveaux 51 à 100
        this.put(13, new HashMap<>()); //Niveaux 101 à 150
        this.put(14, new HashMap<>()); //Niveaux 151 à 190
        this.put(15, new HashMap<>()); //Général
        this.put(16, new HashMap<>()); //Frigost
        this.put(17, new HashMap<>()); //Amakna
        this.put(18, new HashMap<>()); //Ile d'Otomaï
        this.put(19, new HashMap<>()); //Ile des Wabbits
        this.put(20, new HashMap<>()); //Ile de Moon
        this.put(21, new HashMap<>()); //Ile de Sakaï
        this.put(22, new HashMap<>()); //Ile du Minotoror
        this.put(23, new HashMap<>()); //Île de Nowel
        this.put(24, new HashMap<>()); //Pandala
        this.put(25, new HashMap<>()); //Monstres
        this.put(26, new HashMap<>()); //Almanax
        this.put(28, new HashMap<>()); //Frigost
        this.put(29, new HashMap<>()); //Otomaï
        this.put(30, new HashMap<>()); //Ile de Moon
        this.put(31, new HashMap<>()); //Pandala
        this.put(32, new HashMap<>()); //Amakna
        this.put(33, new HashMap<>()); //Bonta
        this.put(34, new HashMap<>()); //Cania
        this.put(35, new HashMap<>()); //Brâkmar
        this.put(36, new HashMap<>()); //Sidimote
        this.put(37, new HashMap<>()); //Astrub
        this.put(38, new HashMap<>()); //Montagne des Koalaks
        this.put(39, new HashMap<>()); //Incarnam
        this.put(40, new HashMap<>()); //Quêtes Principales
        this.put(42, new HashMap<>()); //Avis de recherche
        this.put(43, new HashMap<>()); //Alignement
        this.put(44, new HashMap<>()); //Krosmoz
        this.put(45, new HashMap<>()); //Astrub
        this.put(47, new HashMap<>()); //Campement des Bworks
        this.put(48, new HashMap<>()); //Cania
        this.put(49, new HashMap<>()); //Château d'Amakna
        this.put(50, new HashMap<>()); //Île d'Otomaï
        this.put(51, new HashMap<>()); //Île de Frigost
        this.put(52, new HashMap<>()); //Île des Wabbits
        this.put(53, new HashMap<>()); //Incarnam
        this.put(54, new HashMap<>()); //Montagne des Koalaks
        this.put(56, new HashMap<>()); //Port de Madrestam
        this.put(57, new HashMap<>()); //Province d'Amakna
        this.put(58, new HashMap<>()); //Sufokia
        this.put(59, new HashMap<>()); //Niveaux 191 à 200
        this.put(60, new HashMap<>()); //Archipel de Vulkania
        this.put(61, new HashMap<>()); //Justiciers
        this.put(65, new HashMap<>()); //Dimensions Divines
        this.put(66, new HashMap<>()); //Dimensions
        this.put(67, new HashMap<>()); //Dimensions
        this.put(68, new HashMap<>()); //Halouine
    }};
    private final HashMap<Integer, AchievementTemplate> achievements = new HashMap<>(1800);

    @Inject
    private DatabaseSource dbSource;

    private static final String SAVE_PLAYER_DATA = "INSERT INTO `player_achievements` VALUES (?,?,?,?,?);";
    private  byte[] EMPTY_BYTES;

    @Override
    public void loadPlayerBook(AchievementBook book){
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * FROM `player_achievements` where player = '" + book.getPlayer().getID() + "';")) {
            final ResultSet result = conn.getResult();
            if (result.first()) {
                book.getPlayer().setAchievementPoints(result.getInt("score"));
                book.setRegisterBuffer(IoBuffer.wrap(result.getBytes("register")));
                int len = book.getRegisterBuffer().getInt();
                for (int i = 0; i < len; i++) {
                    book.addToRegister(new AchievementBook.AchievementFinishedInfo(book.getRegisterBuffer().getShort(),book.getRegisterBuffer().get()));
                }

                book.setAdvancementBuffer(IoBuffer.wrap(result.getBytes("advancements")));
                len = book.getAdvancementBuffer().getInt();
                for (int i = 0; i < len; i++) {
                    book.pushInfo(book.getAdvancementBuffer().getInt(), new AchievementBook.AchievementInfo(book.getAdvancementBuffer()));
                }

                book.setRewardBuffer(IoBuffer.wrap(result.getBytes("rewards")));
                len = book.getRewardBuffer().getInt();
                log.debug("{} rewards on hold",len);
                for (int i = 0; i < len; i++) {
                    book.award(book.getRewardBuffer().getShort());
                }
            }
            else{
                try(PreparedStatement pStatement = conn.getConnection().prepareStatement(SAVE_PLAYER_DATA)) {
                    pStatement.setInt(1, book.getPlayer().getID());
                    pStatement.setBytes(2, EMPTY_BYTES);
                    pStatement.setBytes(3, EMPTY_BYTES);
                    pStatement.setBytes(4, EMPTY_BYTES);
                    pStatement.setInt(5, 0);
                    pStatement.execute();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e);
            log.warn(e.getMessage());
        }
    }

    @Override
    public void saveBook(AchievementBook book){
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `player_achievements` SET `register` = ?, `advancements` = ?, `rewards` = ?, `score` = ? WHERE `player` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();

                pStatement.setBytes(1,  book.emptyRegister() ? EMPTY_BYTES : book.serializeRegister(book.getRegisterBuffer()).array());
                pStatement.setBytes(2, book.closeRange() ? EMPTY_BYTES : book.serializeAdvancements(book.getAdvancementBuffer()).array());
                pStatement.setInt(4,  book.getPlayer().getAchievementPoints());
                pStatement.setBytes(3, book.getRewardsOnHold().isEmpty() ? EMPTY_BYTES : book.serializeRewards(book.getRewardBuffer()).array());
                pStatement.setInt(5, book.getPlayer().getID());
                pStatement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e);
                log.warn(e.getMessage());
            }
    }


    @Override
    public int loadRewards() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from achievement_rewards", 0)) {
            ResultSet result = conn.getResult();
            AchievementReward klass;
            while (result.next()) {
                klass = AchievementReward.builder()
                        .id(result.getInt("id"))
                        .achievementId(result.getInt("achievement_id"))
                        .levelMin(result.getInt("level_min"))
                        .levelMax(result.getInt("level_max"))
                        .itemsReward(Enumerable.stringToIntArray(result.getString("items_reward")))
                        .itemsQuantityReward(Enumerable.stringToIntArray(result.getString("items_quantity_reward")))
                        .emotesReward(Enumerable.stringToIntArray(result.getString("emotes_reward")))
                        .spellsReward(Enumerable.stringToIntArray(result.getString("spells_reward")))
                        .titlesReward(Enumerable.stringToIntArray(result.getString("titles_reward")))
                        .ornamentsReward(Enumerable.stringToIntArray(result.getString("ornaments_reward")))
                        .build();
                rewards.put(result.getInt("id"), klass);

                ++i;
            }
        } catch (Exception e) {
            log.error(e);
            log.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public int loadGoals() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from achievement_objectives", 0)) {
            ResultSet result = conn.getResult();
            AchievementGoal klass;
            while (result.next()) {
                klass = AchievementGoal.builder()
                        .id(result.getInt("id"))
                        .achievementId(result.getInt("achievement_id"))
                        .name(result.getString("name"))
                        .criterion(result.getString("criterion"))
                        .build();
                goals.put(result.getInt("id"), klass);
                /*if(result.getString("criterion").length() > 2){
                Criterion cri = Criterion.createQuestCriterionByName(result.getString("criterion").substring(0,2));
*/


                ++i;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }



    @Override
    public AchievementTemplate find(int id) {
        return this.achievements.get(id);
    }

    private AchievementReward[] getAchievementRewards(String list) {
        if (list.isEmpty()) {
            return new AchievementReward[0];
        }
        AchievementReward[] d = new AchievementReward[list.split(",").length];
        for (int i = 0; i < list.split(",").length; i++) {
            d[i] = this.rewards.get(Integer.parseInt(list.split(",")[i]));
            if (d[i] == null) {
                System.out.println(list.split(",")[i] + " null");
            }
        }
        return d;
    }

    private final static AchievementGoal[] ENNANCIATED = new AchievementGoal[0];

    private AchievementGoal[] getAchievementGoals(String list) {
        if (list.isEmpty()) {
            return ENNANCIATED;
        }
        AchievementGoal[] d = new AchievementGoal[list.split(",").length];
        String dd = "";
        for (int i = 0; i < list.split(",").length; i++) {
            d[i] = this.goals.get(Integer.parseInt(list.split(",")[i]));
            if (d[i] == null) {
                dd += i + ",";
            }
        }
        if (!dd.isEmpty()) {
            dd = dd.substring(0, dd.length() - 1);
            if (dd.contains(",")) {
                d = ArrayUtils.removeAll(d, Enumerable.stringToIntArray(dd));
            } else {
                if (d.length == 1)
                    return ENNANCIATED;
                d = ArrayUtils.remove(d, Integer.parseInt(dd));
            }
        }
        return d;
    }

    @Override
    public int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from achievements", 0)) {
            ResultSet result = conn.getResult();
            AchievementTemplate klass;
            while (result.next()) {
                klass = AchievementTemplate.builder()
                        .id(result.getInt("id"))
                        .name(result.getString("name"))
                        .category(result.getInt("category"))
                        .iconId(result.getInt("icon"))
                        .points(result.getInt("points"))
                        .order(result.getInt("order"))
                        .level(result.getInt("level"))
                        .kamasRatio(result.getDouble("kamas_ratio"))
                        .experienceRatio(result.getDouble("experience_ratio"))
                        .kamasScaleWithPlayerLevel(result.getBoolean("kamas_scale"))
                        .objectives(this.getAchievementGoals(result.getString("objectives")))
                        .rewards(this.getAchievementRewards(result.getString("rewards")))
                        .build();
                achievementsCategory.get(result.getInt("category")).put(result.getInt("id"), klass);
                achievements.put(result.getInt("id"), klass);

                ++i;
            }
        } catch (Exception e) {
            log.error(e);
            log.warn(e.getMessage());
        }
        return i;
    }




    @Override
    public void start() {
        log.info("Loaded {} Achievement rewards", this.loadRewards());
        log.info("Loaded {} Achievement goals", this.loadGoals());
        log.info("Loaded {} Achievements", this.loadAll());
        final IoBuffer buffer = IoBuffer.allocate(16);
        buffer.putInt(0);
        this.EMPTY_BYTES = buffer.array();
    }

    @Override
    public void stop() {

    }


}

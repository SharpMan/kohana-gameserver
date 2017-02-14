package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.AreaDAO;
import koh.game.dao.api.MapDAO;
import koh.game.dao.api.MapMonsterDAO;
import koh.game.dao.api.MonsterDAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.DofusTrigger;
import koh.game.entities.environments.SubArea;
import koh.game.entities.mob.MonsterGrade;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayGroupMonsterInformations;
import koh.protocol.types.game.context.roleplay.GroupMonsterStaticInformations;
import koh.protocol.types.game.context.roleplay.MonsterInGroupInformations;
import koh.protocol.types.game.context.roleplay.MonsterInGroupLightInformations;
import koh.utils.Enumerable;
import lombok.extern.log4j.Log4j2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

/**
 * Created by Melancholia on 12/28/15.
 */
@Log4j2
public class MapMonsterDAOImpl extends MapMonsterDAO {

    @Inject
    private DatabaseSource dbSource;

    @Inject
    private MapDAO mapDAO;
    @Inject
    private MonsterDAO monsters;
    @Inject
    private AreaDAO areas;

    private final void initMonstersOnMap() {
        try {
            areas.getSubAreas()
                    .stream()
                    .filter(sub -> sub.getMonsters().length > 0)
                    .forEach(sub -> {
                        Arrays.stream(sub.getMapIds())
                                .mapToObj(id -> mapDAO.findTemplate(id))
                                .filter(map -> map != null && map.getFightActions() == null && !(map.getPosition() != null && map.getPosition().getWorldMap() == -1)) // On ne spawn pas dans les maisons
                                .forEach(map -> {
                                    final int monsterRemaining = MONSTER_GROUP_PER_MAP - map.getMonsters().size();
                                    if (monsterRemaining > 0) {
                                        for (int i = 0; i < monsterRemaining; i++) {
                                            map.addMonster(this.genMonsterGroup(sub,map));
                                        }
                                    }
                                });
                    });
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
            log.warn(e.getMessage());
        }
    }

    @Override
    public MonsterGroup genMonsterGroup(SubArea sub, DofusMap map){
        final int groupDifficulty = sub.getArea().getRANDOM().nextInt(2);
        final int groupCount = Math.min(this.MONSTER_COUNT_BY_DIFFICULTY[groupDifficulty][this.MONSTER_COUNT_BY_DIFFICULTY[groupDifficulty].length -1],map.getBlueCells().length != 0 ? map.getBlueCells().length -1 : 100);
        final MonsterGrade mainMonster = monsters.find(sub.getMonsters()[sub.getArea().getRANDOM().nextInt(sub.getMonsters().length)]).getRandomGrade(sub.getArea().getRANDOM());


        final MonsterGroup gr = MonsterGroup.builder()
                .fix(false)
                .gameRolePlayGroupMonsterInformations(
                        new GameRolePlayGroupMonsterInformations(map.getNextActorId(),
                                mainMonster.getMonster().getEntityLook(),
                                new EntityDispositionInformations((short) -1, (byte) sub.getArea().getRANDOM().nextInt(8)),
                                new GroupMonsterStaticInformations(
                                        new MonsterInGroupLightInformations(mainMonster.getMonsterId(), (byte) mainMonster.getGrade()),
                                        new MonsterInGroupInformations[groupCount]),
                                (short) -1,
                                (byte) -1,
                                (byte) -1,
                                false,
                                false,
                                false))
                .build();
        gr.getGameContextActorInformations(null).disposition.cellId = gr.getFixedCell();
        for (int ii = 0; ii  < groupCount; ii++) {
            final MonsterGrade randMonster = monsters.find(sub.getMonsters()[sub.getArea().getRANDOM().nextInt(sub.getMonsters().length)]).getRandomGrade(sub.getArea().getRANDOM());
            ((GameRolePlayGroupMonsterInformations) gr.getGameContextActorInformations(null)).staticInfos.underlings[ii] = new MonsterInGroupInformations(randMonster.getMonsterId(), randMonster.getGrade(), randMonster.getMonster().getEntityLook());
        }
        return gr;
    }

    @Override
    public void insert(int map, short cell,byte direction, String param1, String param2) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `map_monsters_fix` VALUES (?,?,?,?,?,?,?,?,?,?,?);")) {

                PreparedStatement pStatement = conn.getStatement();

                pStatement.setInt(1, map);
                pStatement.setInt(2, cell);
                pStatement.setByte(3, direction);
                pStatement.setInt(4, 0); //10
                pStatement.setByte(5, (byte) -1);
                pStatement.setByte(6, (byte)-1);
                pStatement.setInt(7, 0);
                pStatement.setInt(8, 0);
                pStatement.setInt(9, 0);
                pStatement.setString(10, param1);
                pStatement.setString(11, param2);
                pStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(int map, short cell) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `map_monsters_fix` WHERE `map` = ? AND `cell` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1,map);
                pStatement.setShort(2,cell);
                pStatement.execute();

            } catch (Exception e) {
                e.printStackTrace();
                log.error(e);
                log.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
            log.warn(e.getMessage());
        }
    }


    public final int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from map_monsters_fix")) {
            ResultSet result = conn.getResult();
            DofusMap map;
            while (result.next()) {
                map = mapDAO.findTemplate(result.getInt("map"));
                if (map == null) {
                    continue;
                }
                try {
                    final GameRolePlayGroupMonsterInformations gm = new GameRolePlayGroupMonsterInformations(map.getNextActorId(),
                            monsters.find(Integer.parseInt(result.getString("main_creature").split(",")[0])).getEntityLook(),
                            new EntityDispositionInformations(result.getShort("cell"), result.getByte("direction")),
                            new GroupMonsterStaticInformations(
                                    new MonsterInGroupLightInformations(Integer.parseInt(result.getString("main_creature").split(",")[0]), Byte.parseByte(result.getString("main_creature").split(",")[1])),
                                    Arrays.stream(result.getString("underlings").split(";"))
                                            .filter(text -> !text.isEmpty())
                                            .map(x -> new MonsterInGroupInformations(
                                                    Integer.parseInt(x.split(",")[0]),
                                                    Byte.parseByte(x.split(",")[1]),
                                                    monsters.find(Integer.parseInt(x.split(",")[0])).getEntityLook()))
                                            .toArray(MonsterInGroupInformations[]::new)
                            ),

                                        /*result.getShort("age_bonus")*/(short) -1,
                            result.getByte("lot_share"),
                            result.getByte("alignement_side"),
                            result.getBoolean("key_ring_bonus"),
                            result.getBoolean("has_hard_core_drop"),
                            result.getBoolean("has_ava_rewaard_token"));
                    map.addMonster(MonsterGroup.builder()
                            .fix(true)
                            .fixedCell(result.getShort("cell"))
                            .gameRolePlayGroupMonsterInformations(gm)
                            .build()
                    );

                }
                catch (Exception e){
                    log.error("Error at row  @{},{}",result.getInt("map"),result.getShort("cell"));
                    e.printStackTrace();
                }
                ++i;
            }
        } catch (Exception e) {
            log.error("Error at row  @{}",i);
            e.printStackTrace();

        }
        return i;
    }

    private int loadAllTriggers() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_triggers", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    mapDAO.findTemplate(result.getInt("old_map")).init$Return().getCell(result.getShort("old_cell")).myAction = new DofusTrigger(result);

                } catch (Exception e) {
                    log.debug("map {} trigger null", result.getInt("map"));
                }
                i++;
            }
        } catch (Exception e) {
            log.error(e);
            log.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public void start() {
        log.info("Loaded {} map monsters fix", this.loadAll());
        log.info("Loaded {} map triggers", this.loadAllTriggers());
        this.initMonstersOnMap();
    }

    @Override
    public void stop() {

    }
}

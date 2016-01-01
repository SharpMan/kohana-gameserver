package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.AreaDAO;
import koh.game.dao.api.MapDAO;
import koh.game.dao.api.MapMonsterDAO;
import koh.game.dao.api.MonsterDAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.SubArea;
import koh.game.entities.mob.MonsterGrade;
import koh.game.utils.sql.ConnectionResult;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayGroupMonsterInformations;
import koh.protocol.types.game.context.roleplay.GroupMonsterStaticInformations;
import koh.protocol.types.game.context.roleplay.MonsterInGroupInformations;
import koh.protocol.types.game.context.roleplay.MonsterInGroupLightInformations;
import lombok.extern.log4j.Log4j2;

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
                                .filter(x -> x != null)
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
            log.error(e);
            log.warn(e.getMessage());
        }
    }

    @Override
    public MonsterGroup genMonsterGroup(SubArea sub, DofusMap map){
        final int groupDifficulty = sub.getArea().getRANDOM().nextInt(2);
        final int groupCount = this.MONSTER_COUNT_BY_DIFFICULTY[groupDifficulty][this.MONSTER_COUNT_BY_DIFFICULTY[groupDifficulty].length -1];
        MonsterGrade mainMonster = monsters.find(sub.getMonsters()[sub.getArea().getRANDOM().nextInt(sub.getMonsters().length)]).getRandomGrade(sub.getArea().getRANDOM());


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
        for (int ii = 0; ii < groupCount; ii++) {
            MonsterGrade randMonster = monsters.find(sub.getMonsters()[sub.getArea().getRANDOM().nextInt(sub.getMonsters().length)]).getRandomGrade(sub.getArea().getRANDOM());
            ((GameRolePlayGroupMonsterInformations) gr.getGameContextActorInformations(null)).staticInfos.underlings[ii] = new MonsterInGroupInformations(randMonster.getMonsterId(), (byte) randMonster.getGrade(), randMonster.getMonster().getEntityLook());
        }
        return gr;
    }

    public final int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from map_monsters_fix")) {
            ResultSet result = conn.getResult();
            DofusMap map;
            while (result.next()) {
                map = mapDAO.findTemplate(result.getInt("map"));
                if (map == null || (map.getPosition() != null && map.getPosition().getWorldMap() == -1)) {
                    continue;
                }
                map.addMonster(MonsterGroup.builder()
                        .fix(true)
                        .fixedCell(result.getShort("cell"))
                        .gameRolePlayGroupMonsterInformations(
                                new GameRolePlayGroupMonsterInformations(map.getNextActorId(),
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
                                                        .toArray(MonsterInGroupInformations[]::new)),

                                        result.getShort("age_bonus"),
                                        result.getByte("lot_share"),
                                        result.getByte("alignement_side"),
                                        result.getBoolean("key_ring_bonus"),
                                        result.getBoolean("has_hard_core_drop"),
                                        result.getBoolean("has_ava_rewaard_token")))
                        .build());
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
        log.info("Loaded {} map monsters fix", this.loadAll());
        this.initMonstersOnMap();
    }

    @Override
    public void stop() {

    }
}

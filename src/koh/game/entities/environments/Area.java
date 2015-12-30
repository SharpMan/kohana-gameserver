package koh.game.entities.environments;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.dao.DAO;
import koh.protocol.messages.game.context.GameContextRefreshEntityLookMessage;
import koh.protocol.messages.game.context.roleplay.GameRolePlayShowActorMessage;
import koh.protocol.messages.game.interactive.StatedMapUpdateMessage;
import koh.protocol.types.game.interactive.StatedElement;
import lombok.Builder;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Neo-Craft
 */
@Builder
public class Area {

    @Getter
    private final SecureRandom RANDOM = new SecureRandom();

    @Getter
    private final ScheduledExecutorService backGroundWorker = Executors.newScheduledThreadPool(50);
    @Getter
    private int id;
    @Getter
    private final ArrayList<SubArea> subAreas = new ArrayList<>();
    @Getter
    private SuperArea superArea;
    @Getter
    private boolean containHouses, containPaddocks, hasWorldMap;
    @Getter
    private int worldmapId;

    public void onBuilt() {
        /*AUTO MOVE MONSTERS*/

        /* STARS ON MONSTERS */
        new CancellableScheduledRunnable(backGroundWorker, ((DAO.getSettings().getIntElement("Monster.AgeBonusTime") + this.id) * 60) * 1000, (DAO.getSettings().getIntElement("Monster.AgeBonusTime") * 60) * 1000) {
            @Override
            public void run() {
                subAreas.forEach(Sub -> Arrays.stream(Sub.getMapIds())
                        .mapToObj(id -> DAO.getMaps().findTemplate(id))
                        .filter(map -> map != null && !map.getMonsters().isEmpty())
                        .forEach(map -> {
                           map.getMonsters().forEach(mob -> {

                               if (mob.getGameRolePlayGroupMonsterInformations().ageBonus == -1) {
                                   mob.getGameRolePlayGroupMonsterInformations().ageBonus = 0;
                               }
                               else if (mob.getGameRolePlayGroupMonsterInformations().ageBonus != 200) {
                                   mob.getGameRolePlayGroupMonsterInformations().ageBonus += 4;

                               }
                               else{
                                   return;
                               }
                               if(map.isMyInitialized())
                                   map.sendToField(new GameRolePlayShowActorMessage(mob.getGameRolePlayGroupMonsterInformations()));
                            });
                        }));
            }
        };
        /* START ON INTERACTIF ELEMENTS */
        new CancellableScheduledRunnable(backGroundWorker, ((DAO.getSettings().getIntElement("Job.AgeBonusTime") + this.id) * 60) * 1000, (DAO.getSettings().getIntElement("Job.AgeBonusTime") * 60) * 1000) {
            @Override
            public void run() {
                subAreas.forEach(Sub -> Arrays.stream(Sub.getMapIds())
                        .forEach(Id -> DAO.getMaps().findTemplate(Id).getInteractiveElements().stream()
                                .filter(Element -> DAO.getMaps().findTemplate(Id).getStatedElementById(Element.elementId) != null && DAO.getMaps().findTemplate(Id).getStatedElementById(Element.elementId).elementState == 0)
                                .forEach(Interactive -> {
                                    {
                                        if (Interactive.ageBonus == -1) {
                                            Interactive.ageBonus = 0;
                                        }
                                        if (Interactive.ageBonus != 200) {
                                            Interactive.ageBonus += 4;
                                        }

                                    }
                                })));
            }
        };
        /* RESPAWN INTERFACTIF ELEMENTS*/
        new CancellableScheduledRunnable(backGroundWorker, (DAO.getSettings().getIntElement("Job.Spawn") + this.id) * 60 * 1000, DAO.getSettings().getIntElement("Job.Spawn") * 60 * 1000) {
            @Override
            public void run() {
                subAreas
                        .forEach(Sub -> Arrays.stream(Sub.getMapIds())
                                .filter(Id -> DAO.getMaps().findTemplate(Id).isMyInitialized())
                                .forEach(Id -> {
                                    {
                                        boolean Modified = false;
                                        for (StatedElement element : (Iterable<StatedElement>) Arrays.stream(DAO.getMaps().findTemplate(Id).getElementsStated())
                                                .filter(statedElement -> statedElement.deadAt != -1 && statedElement.elementState > 0 && (System.currentTimeMillis() - statedElement.deadAt) > DAO.getSettings().getIntElement("job.Spawn") * 60000)::iterator) {
                                            element.deadAt = -1;
                                            element.elementState = 0;
                                            Modified = true;
                                        }
                                        if (Modified) {
                                            DAO.getMaps().findTemplate(Id).sendToField(new StatedMapUpdateMessage(DAO.getMaps().findTemplate(Id).getElementsStated()));
                                        }

                                    }
                                }));
            }
        };
    }

    /*public IntStream Mapids() {
     if (Maps.length == 0) {
     for (subArea Sub : subAreas) {
     Maps = ArrayUtils.addAll(Maps, Sub.mapIds);
     }
     }
     return Arrays.stream(Maps).distinct();
     }*/
}

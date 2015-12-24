package koh.game.entities.environments;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.dao.DAO;
import koh.protocol.messages.game.interactive.StatedMapUpdateMessage;
import koh.protocol.types.game.interactive.StatedElement;
import lombok.Builder;
import lombok.Getter;

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
    public final ScheduledExecutorService backGroundWorker = Executors.newScheduledThreadPool(50);
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

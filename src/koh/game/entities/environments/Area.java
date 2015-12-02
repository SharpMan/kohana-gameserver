package koh.game.entities.environments;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import koh.concurrency.CancellableScheduledRunnable;
import koh.game.dao.DAO;
import koh.game.utils.Settings;
import koh.protocol.messages.game.interactive.StatedMapUpdateMessage;
import koh.protocol.types.game.interactive.StatedElement;

/**
 *
 * @author Neo-Craft
 */
public class Area {

    public final ScheduledExecutorService BackGroundWorker = Executors.newScheduledThreadPool(50);

    public Area() {
        new CancellableScheduledRunnable(BackGroundWorker, ((Settings.GetIntElement("job.AgeBonusTime") + this.id) * 60) * 1000, (Settings.GetIntElement("job.AgeBonusTime") * 60) * 1000) {
            @Override
            public void run() {
                Arrays.stream(subAreas).forEach(Sub -> Arrays.stream(Sub.mapIds)
                        .forEach(Id -> DAO.getMaps().findTemplate(Id).interactiveElements.stream()
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
        new CancellableScheduledRunnable(BackGroundWorker, (Settings.GetIntElement("job.Spawn") + this.id) * 60 * 1000, Settings.GetIntElement("job.Spawn") * 60 * 1000) {
            @Override
            public void run() {
                Arrays.stream(subAreas)
                        .forEach(Sub -> Arrays.stream(Sub.mapIds)
                                .filter(Id -> DAO.getMaps().findTemplate(Id).myInitialized)
                                .forEach(Id -> {
                                    {
                                        boolean Modified = false;
                                        for (StatedElement element : (Iterable<StatedElement>) Arrays.stream(DAO.getMaps().findTemplate(Id).elementsStated)
                                        .filter(statedElement -> statedElement.deadAt != -1 && statedElement.elementState > 0 && (System.currentTimeMillis() - statedElement.deadAt) > Settings.GetIntElement("job.Spawn") * 60000)::iterator) {
                                            element.deadAt = -1;
                                            element.elementState = 0;
                                            Modified = true;
                                        }
                                        if (Modified) {
                                            DAO.getMaps().findTemplate(Id).sendToField(new StatedMapUpdateMessage(DAO.getMaps().findTemplate(Id).elementsStated));
                                        }

                                    }
                                }));
            }
        };
    }

    public int id;
    public SubArea[] subAreas = new SubArea[0];
    public SuperArea superArea;
    public boolean containHouses;
    public boolean containPaddocks;
    public int worldmapId;
    public boolean hasWorldMap;

    /*public IntStream Mapids() {
     if (Maps.length == 0) {
     for (subArea Sub : subAreas) {
     Maps = ArrayUtils.addAll(Maps, Sub.mapIds);
     }
     }
     return Arrays.stream(Maps).distinct();
     }*/
}

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
        new CancellableScheduledRunnable(BackGroundWorker, ((Settings.GetIntElement("Job.AgeBonusTime") + this.id) * 60) * 1000, (Settings.GetIntElement("Job.AgeBonusTime") * 60) * 1000) {
            @Override
            public void run() {
                Arrays.stream(subAreas).forEach(Sub -> Arrays.stream(Sub.mapIds)
                        .forEach(Id -> DAO.getMaps().getMap(Id).interactiveElements.stream()
                                .filter(Element -> DAO.getMaps().getMap(Id).getStatedElementById(Element.elementId) != null && DAO.getMaps().getMap(Id).getStatedElementById(Element.elementId).elementState == 0)
                                .forEach(Interactive -> {
                                    {
                                        if (Interactive.AgeBonus == -1) {
                                            Interactive.AgeBonus = 0;
                                        }
                                        if (Interactive.AgeBonus != 200) {
                                            Interactive.AgeBonus += 4;
                                        }

                                    }
                                })));
            }
        };
        new CancellableScheduledRunnable(BackGroundWorker, (Settings.GetIntElement("Job.Spawn") + this.id) * 60 * 1000, Settings.GetIntElement("Job.Spawn") * 60 * 1000) {
            @Override
            public void run() {
                Arrays.stream(subAreas)
                        .forEach(Sub -> Arrays.stream(Sub.mapIds)
                                .filter(Id -> DAO.getMaps().getMap(Id).myInitialized)
                                .forEach(Id -> {
                                    {
                                        boolean Modified = false;
                                        for (StatedElement element : (Iterable<StatedElement>) Arrays.stream(DAO.getMaps().getMap(Id).elementsStated)
                                        .filter(statedElement -> statedElement.deadAt != -1 && statedElement.elementState > 0 && (System.currentTimeMillis() - statedElement.deadAt) > Settings.GetIntElement("Job.Spawn") * 60000)::iterator) {
                                            element.deadAt = -1;
                                            element.elementState = 0;
                                            Modified = true;
                                        }
                                        if (Modified) {
                                            DAO.getMaps().getMap(Id).sendToField(new StatedMapUpdateMessage(DAO.getMaps().getMap(Id).elementsStated));
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
     for (SubArea Sub : subAreas) {
     Maps = ArrayUtils.addAll(Maps, Sub.mapIds);
     }
     }
     return Arrays.stream(Maps).distinct();
     }*/
}

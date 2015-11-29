package koh.game.entities.environments;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import koh.concurrency.CancellableScheduledRunnable;
import koh.game.dao.mysql.MapDAO;
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
                Arrays.stream(SubAreas).forEach(Sub -> Arrays.stream(Sub.mapIds)
                        .forEach(Id -> MapDAO.Cache.get(Id).InteractiveElements.stream()
                                .filter(Element -> MapDAO.Cache.get(Id).GetStatedElementById(Element.elementId) != null && MapDAO.Cache.get(Id).GetStatedElementById(Element.elementId).elementState == 0)
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
                Arrays.stream(SubAreas)
                        .forEach(Sub -> Arrays.stream(Sub.mapIds)
                                .filter(Id -> MapDAO.Cache.get(Id).myInitialized)
                                .forEach(Id -> {
                                    {
                                        boolean Modified = false;
                                        for (StatedElement Element : (Iterable<StatedElement>) Arrays.stream(MapDAO.Cache.get(Id).ElementsStated)
                                        .filter(Element -> Element.deadAt != -1 && Element.elementState > 0 && (System.currentTimeMillis() - Element.deadAt) > Settings.GetIntElement("Job.Spawn") * 60000)::iterator) {
                                            Element.deadAt = -1;
                                            Element.elementState = 0;
                                            Modified = true;
                                        }
                                        if (Modified) {
                                            MapDAO.Cache.get(Id).sendToField(new StatedMapUpdateMessage(MapDAO.Cache.get(Id).ElementsStated));
                                        }

                                    }
                                }));
            }
        };
    }

    public int id;
    public SubArea[] SubAreas = new SubArea[0];
    public SuperArea superArea;
    public boolean containHouses;
    public boolean containPaddocks;
    public int worldmapId;
    public boolean hasWorldMap;

    /*public IntStream Mapids() {
     if (Maps.length == 0) {
     for (SubArea Sub : SubAreas) {
     Maps = ArrayUtils.addAll(Maps, Sub.mapIds);
     }
     }
     return Arrays.stream(Maps).distinct();
     }*/
}

package koh.game.entities.kolissium;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.PlayerInst;
import koh.game.entities.item.ItemTemplate;
import koh.game.network.WorldServer;
import koh.protocol.client.enums.PvpArenaStepEnum;
import koh.protocol.client.enums.PvpArenaTypeEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.roleplay.fight.arena.GameRolePlayArenaRegistrationStatusMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Melancholia on 6/9/16.
 */
public class ArenaBattle {

    private static final ScheduledExecutorService backGroundWorker = Executors.newScheduledThreadPool(50);

    @Getter
    private static final AtomicInteger idenGen = new AtomicInteger();
    @Getter
    private ArenaParty party1, party2;
    @Getter
    private final int id;

    private ArrayList<Integer> readyToGoes = new ArrayList<>(6);
    public static final int BAN_TIME_MINUTES = 15;
    public static final ItemTemplate KOLIZETON = DAO.getItemTemplates().getTemplate(12736);
    private ArrayList<Player> pussies = new ArrayList<>(6);

    public ArenaBattle(ArenaParty p1, ArenaParty p2) {
        this.id = idenGen.incrementAndGet();
        this.party1 = p1;
        this.party2 = p2;
        this.pussies.addAll(party1.getPlayers());
        this.pussies.addAll(party2.getPlayers());
        new CancellableScheduledRunnable(backGroundWorker, 1000 * (DAO.getSettings().getBoolElement("Logging.Debug") ? 17 : 25)) {
            @Override
            public void run() {
                try {
                    if ((KolizeumExecutor.getTEAM_SIZE() * 2) != readyToGoes.size()) {
                        pussies.forEach(pl -> {
                            if (!readyToGoes.contains(pl.getID())) {
                                party1.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 275, pl.getNickName()));
                                party2.sendToField(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 275, pl.getNickName()));
                                pl.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 343, String.valueOf(BAN_TIME_MINUTES)));
                                PlayerInst.getPlayerInst(pl.getID()).setBannedTime(System.currentTimeMillis() + BAN_TIME_MINUTES * 60000);
                            } else {
                                pl.send(new GameRolePlayArenaRegistrationStatusMessage(false, PvpArenaStepEnum.ARENA_STEP_UNREGISTER, PvpArenaTypeEnum.ARENA_TYPE_3VS3));

                            }
                            if (pl.getClient() != null) {
                                pl.getClient().endGameAction(GameActionTypeEnum.KOLI);
                                pl.getClient().endGameAction(GameActionTypeEnum.GROUP);
                            }

                        });

                        party1.setInKolizeum(false);
                        party2.setInKolizeum(false);
                        DAO.getArenas().remove(id);
                    } else {

                        //TODO: !canGameAction Fight ptete il se branle
                        final Player playerFighting = pussies.stream().filter(pl -> pl.getClient() == null || !pl.isInWorld() || pl.getClient().isGameAction(GameActionTypeEnum.FIGHT)).findFirst().orElse(null);
                        if (playerFighting != null || party1.memberCounts() == 0 || party2.memberCounts() == 0) {
                            pussies.forEach(p -> p.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 275, playerFighting.getNickName())));
                            playerFighting.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 343, String.valueOf(BAN_TIME_MINUTES)));
                            PlayerInst.getPlayerInst(playerFighting.getID()).setBannedTime(System.currentTimeMillis() + BAN_TIME_MINUTES * 60000);
                            pussies.forEach(p -> {
                                p.send(new GameRolePlayArenaRegistrationStatusMessage(false, PvpArenaStepEnum.ARENA_STEP_UNREGISTER, PvpArenaTypeEnum.ARENA_TYPE_3VS3));
                                if (p.getClient() != null) {
                                    p.getClient().endGameAction(GameActionTypeEnum.KOLI);
                                    p.getClient().endGameAction(GameActionTypeEnum.GROUP);
                                }
                            });
                            party1.setInKolizeum(false);
                            party2.setInKolizeum(false);
                            DAO.getArenas().remove(id);
                            return;
                        }
                        pussies.stream().filter(p -> p.getClient() != null).forEach(p -> p.getClient().endGameAction(GameActionTypeEnum.EXCHANGE));

                        party1.sendToField(new GameRolePlayArenaRegistrationStatusMessage(true, PvpArenaStepEnum.ARENA_STEP_STARTING_FIGHT, PvpArenaTypeEnum.ARENA_TYPE_3VS3));
                        party2.sendToField(new GameRolePlayArenaRegistrationStatusMessage(true, PvpArenaStepEnum.ARENA_STEP_STARTING_FIGHT, PvpArenaTypeEnum.ARENA_TYPE_3VS3));
                        WorldServer.getKoli().startFight(party1.getPlayers(), party2.getPlayers());
                    }
                } catch (NullPointerException e) { //Pary distroyed
                    pussies.forEach(p -> {
                        p.send(new GameRolePlayArenaRegistrationStatusMessage(false, PvpArenaStepEnum.ARENA_STEP_UNREGISTER, PvpArenaTypeEnum.ARENA_TYPE_3VS3));
                        if (p.getClient() != null) {
                            p.getClient().endGameAction(GameActionTypeEnum.KOLI);
                            p.getClient().endGameAction(GameActionTypeEnum.GROUP);
                        }
                    } );
                    party1.setInKolizeum(false);
                    party2.setInKolizeum(false);
                    DAO.getArenas().remove(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void accept(Player pl) {
        this.readyToGoes.add(pl.getID());
    }

    public void clear() {
        try {
            this.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


}

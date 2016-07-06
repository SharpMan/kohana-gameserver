package koh.game.entities.kolissium;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.kolissium.tasks.StartFightTask;
import koh.game.fights.FightTypeEnum;
import koh.game.network.ChatChannel;
import koh.game.utils.PeriodicContestExecutor;
import koh.protocol.client.enums.BreedEnum;
import koh.protocol.client.enums.PvpArenaStepEnum;
import koh.protocol.client.enums.PvpArenaTypeEnum;
import koh.protocol.messages.game.chat.ChatServerMessage;
import koh.protocol.messages.game.context.roleplay.fight.arena.GameRolePlayArenaRegistrationStatusMessage;
import koh.utils.Couple;
import lombok.Getter;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static koh.protocol.client.enums.ChatActivableChannelsEnum.CHANNEL_ADMIN;

/**
 * Created by Melancholia on 3/20/16.
 */
public class KolizeumExecutor extends PeriodicContestExecutor {

    protected static final int[] MAPS = new int[]{
            94634497, 94634499, 94634501,
            94634507, 94634509, 94634511,
            94634513, 94634515, 94634517,
            94634519, 94634505, 94634503,
            141033472, 141033474, 141034496,
            141034498, 141035520, 141035522,
            141036544, 141036546, 141037568,
            141037570, 141038592, 141038594,
            141295616, 141295618, 141296640,
            141296642, 141297664, 141297666,
            141298688, 141298690, 141299712,
            141299714, 141300736, 141300738
    };

    public KolizeumExecutor() {
        this.waiting = new CopyOnWriteArrayList<>();
        this.waitingGroups = new CopyOnWriteArrayList<>();
        super.initialize();
    }


    private CopyOnWriteArrayList<Player> waiting;
    private CopyOnWriteArrayList<ArenaParty> waitingGroups;
    //private Map<Integer,Player> waiting = new ConcurrentHashMap<>())
    private static Map<Integer, Couple<Integer, Short>> lastPositions = new HashMap<>();

    @Getter
    private static final int TEAM_SIZE = DAO.getSettings().getIntElement("Koliseo.Size");
    private static final int TEAM_LVL_DIFF_MAX = 40;

    private static Comparator<ArenaParty> partySorter;
    private static Comparator<Player> playerSorter;

    public static byte[] PILLAR = new byte[]{BreedEnum.Sacrieur, BreedEnum.Xelor, BreedEnum.Osamodas, BreedEnum.Eniripsa};

    public void clear() {
        try {
            ArrayList<Player> players = new ArrayList<>(waiting);
            for (Player player : players) {
                if (player.getClient() != null) {
                    player.getClient().abortGameAction(GameActionTypeEnum.KOLI, new Object[2]);
                    player.getClient().delGameAction(GameActionTypeEnum.KOLI);
                }
                player.send(new GameRolePlayArenaRegistrationStatusMessage(false, PvpArenaStepEnum.ARENA_STEP_UNREGISTER, PvpArenaTypeEnum.ARENA_TYPE_3VS3));

            }
            ArrayList<ArenaParty> parties = new ArrayList<>(waitingGroups);
            for (ArenaParty party : parties) {
                party.setInKolizeum(false);
                for (Player player : party.getPlayers()) {
                    if (player.getClient() != null) {
                        player.getClient().abortGameAction(GameActionTypeEnum.KOLI, new Object[2]);
                        player.getClient().delGameAction(GameActionTypeEnum.KOLI);
                    }
                    player.send(new GameRolePlayArenaRegistrationStatusMessage(false, PvpArenaStepEnum.ARENA_STEP_UNREGISTER, PvpArenaTypeEnum.ARENA_TYPE_3VS3));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        waiting.clear();
        waitingGroups.clear();
        waiting = new CopyOnWriteArrayList<>();
        waitingGroups = new CopyOnWriteArrayList<>();
    }

    static {
        partySorter = Comparator.comparing(party -> party.getMoyLevel());
        partySorter.thenComparing(Comparator.comparing(party -> party.getRating()));

        playerSorter = Comparator.comparing(pl -> pl.getLevel());
        playerSorter.thenComparing(Comparator.comparing(pl -> pl.getKolizeumRate().getRating()));
    }

    @Override
    public void registerPlayer(Player p) {
        this.executeTask(() -> waiting.addIfAbsent(p));
    }

    @Override
    public void unregisterPlayer(Player p) {
        this.executeTask(() -> waiting.remove(p));
    }

    public boolean groupIsRegistred(ArenaParty p) {
        return this.waitingGroups.contains(p);
    }

    @Override
    public boolean unregisterGroup(Player executor, ArenaParty group) {
        if (group == null || executor == null) {
            return false;
        }
        if (!group.isChief(executor)) {
            PlayerController.sendServerMessage(executor.getClient(), "Seul le chef <b>" + group.getChief().getNickName() + "</b> peut désinscrire le groupe du Kolizeum.", "CC0000");
            return false;
        }
        group.setInKolizeum(false);
        this.executeTask(() -> {
            final boolean success = waitingGroups.remove(group);
            if (success) {
                group.sendMessageToGroup("Votre groupe a été désinscrit du Kolizeum.");
            }
        });
        return true;
    }

    @Override
    public void unregisterGroupForced(ArenaParty group) {
        this.executeTask(() -> {
            waitingGroups.remove(group);
            group.setInKolizeum(false);
        });
    }

    @Override
    public boolean isRegistred(Player player) {
        return waiting.contains(player);
    }

    @Override
    public boolean registerGroup(final Player executor, final ArenaParty group) {
        if (group == null || executor == null) {
            return false;
        }
        if (group.inKolizeum()) {
            PlayerController.sendServerMessage(executor.getClient(), "Votre groupe est déjà inscrit à un Kolizeum.", "C16100");
            return false;
        }
        if (group.memberCounts() > TEAM_SIZE) {
            PlayerController.sendServerMessage(executor.getClient(), "Votre groupe ne peut pas être inscrit car il contient plus de " + TEAM_SIZE + " joueurs.", "C16100");
            return false;
        }
        group.setInKolizeum(true);
        this.executeTask(() -> {
            final boolean success = waitingGroups.addIfAbsent(group);
            if (success) {
                group.sendMessageToGroup("Votre groupe a été inscrit au Kolizeum.");
            }
        });
        return true;
    }

    @Override
    public void run() {
        try {
            int waitingSize = waiting.size();
            for (Party group : waitingGroups) {
                try {
                    waitingSize += group.memberCounts();
                } catch (NullPointerException e) {
                    waitingGroups.remove(group);
                }
            }
            if (!DAO.getSettings().getBoolElement("Logging.Debug")) {
                System.out.println("waitingSize= " + waitingSize);
                ChatChannel.CHANNELS.get(CHANNEL_ADMIN).sendToField(new ChatServerMessage(CHANNEL_ADMIN, "Koliseo scale " + waitingSize + "/" + TEAM_SIZE, (int) Instant.now().getEpochSecond(), "az", 1, "Neo-Craft", 1));
            }
            if (waitingSize >= TEAM_SIZE * 2) {
                tryToStartFight();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryToStartFight() {
        try {
            final ArrayList<Player> team1 = new ArrayList<Player>();
            final ArrayList<Player> team2 = new ArrayList<Player>();
            int lvlTeam1 = 0;
            int lvlTeam2 = 0;

            final List<ArenaParty> groupSortedList = new ArrayList<ArenaParty>();
            System.out.println("getnextWaiting");
            groupSortedList.addAll(this.nextWaitingGroups(2));
            groupSortedList.sort(partySorter);

            for (ArenaParty group : groupSortedList) {
                if (!group.inKolizeum()) {
                    waitingGroups.remove(group);
                    System.out.println("removing group cause not inkoli");
                    continue;
                }
                final int groupSize = group.getPlayers().size();
                final int grouplevel = group.getPlayers().stream().mapToInt(Player::getLevel).sum();
                if (lvlTeam1 > lvlTeam2 && team1.size() + groupSize <= 3) {
                    lvlTeam2 += grouplevel;
                    team2.addAll(group.observers);
                } else if (team1.size() + groupSize <= TEAM_SIZE) {
                    lvlTeam1 += grouplevel;
                    team1.addAll(group.getPlayers());
                } else if (team2.size() + groupSize <= TEAM_SIZE) {
                    lvlTeam2 += grouplevel;
                    team2.addAll(group.getPlayers());
                } else {
                    break;//all teams are ready
                }
                //group.sendMessageToGroup( "Votre groupe a été associé à une équipe de combat, en attente d'autres joueurs...");
            }
            groupSortedList.clear();

            if (!waiting.isEmpty()) {
                final List<Player> sortedList = new ArrayList<>();
                Player p;
                System.out.println("getnextPlayerWiaing");
                sortedList.addAll(this.nextWaitingPlayers(2 * TEAM_SIZE - (team1.size() + team2.size())));
                Collections.sort(sortedList, playerSorter);
                System.out.println("get first in list");
                p = waiting.get(0);

                while (team1.size() != TEAM_SIZE || team2.size() != TEAM_SIZE || p != null) {
                    if (lvlTeam1 > lvlTeam2 && team2.size() < 3) {
                        if (p == null) {
                            continue;
                        }
                        lvlTeam2 += p.getLevel();
                        team2.add(p);
                        sortedList.remove(p);
                    } else if (team1.size() < TEAM_SIZE) {
                        if (p == null) {
                            continue;
                        }
                        lvlTeam1 += p.getLevel();
                        team1.add(p);
                        sortedList.remove(p);
                    } else if (team2.size() < TEAM_SIZE) {
                        if (p == null) {
                            continue;
                        }
                        lvlTeam2 += p.getLevel();
                        team2.add(p);
                        sortedList.remove(p);
                    } else {
                        break;//all teams are ready
                    }

                    final Iterator<Player> it = sortedList.iterator();
                    if (it.hasNext()) {
                        p = it.next();
                    } else {
                        p = null;
                    }
                    //P is Initialized => recheck
                }
                p = null;
                sortedList.clear();
            }

            final int teamDiff = Math.abs(lvlTeam1 - lvlTeam2);
            if (teamDiff > TEAM_LVL_DIFF_MAX) { //TODO check 2 team1's ip vs team2's ip have seen
                return;
            } else if (team1.size() != TEAM_SIZE || team2.size() != TEAM_SIZE) {
                System.out.println("TEAMSIZE !" + team1.size() + " " + team2.size());
                return;
            } else if (team1.stream().anyMatch(team2::contains) || team2.stream().anyMatch(team1::contains)) {
                System.out.println("team depracated");
                return;
            } else if ((int) team1.stream().map(Player::getBreed).distinct().count() != team1.size()
                    || (int) team2.stream().map(Player::getBreed).distinct().count() != team2.size()) {
                System.out.println("too many breed");
                return;
            }
            final ArenaParty team1Group = team1.stream()
                    .filter(p -> p.getClient() != null && p.getClient().getParty() instanceof ArenaParty)
                    .map(p -> p.getClient().getParty().asArena())
                    .max(Comparator.comparing(ArenaParty::memberCounts))
                    .orElse(null);
            team1.stream().filter(p -> p.getClient() != null && p.getClient().getParty() != null && p.getClient().getParty()/*.asArena()*/ != team1Group)
                    .forEach(pl -> pl.getClient().endGameAction(GameActionTypeEnum.GROUP));

            final ArenaParty team2Group = team2.stream()
                    .filter(p -> p.getClient() != null && p.getClient().getParty() instanceof ArenaParty)
                    .map(p -> p.getClient().getParty().asArena())
                    .max(Comparator.comparing(ArenaParty::memberCounts))
                    .orElse(null);

            team2.stream().filter(p -> p.getClient().getParty() != null && p.getClient().getParty()/*.asArena()*/ != team2Group)
                    .forEach(pl -> pl.getClient().endGameAction(GameActionTypeEnum.GROUP));

            if (team1Group != null) {
                team1Group.setInKolizeum(false);
                team1.stream().filter(p -> !team1Group.containsPlayer(p)).forEach(team1Group::addPlayer);
                this.waitingGroups.remove(team1Group);
            }
            if (team2Group != null) {
                team2Group.setInKolizeum(false);
                team2.stream().filter(p -> !team2Group.containsPlayer(p)).forEach(team2Group::addPlayer);
                this.waitingGroups.remove(team2Group);
            }

            final ArenaParty party = team1Group != null ? team1Group : new ArenaParty(team1), party1 = team2Group != null ? team2Group : new ArenaParty(team2);


            team1.forEach(waiting::remove);
            team2.forEach(waiting::remove);


            party1.setInKolizeum(true);
            party.setInKolizeum(true);
            party.sendToField(new GameRolePlayArenaRegistrationStatusMessage(true, PvpArenaStepEnum.ARENA_STEP_WAITING_FIGHT, PvpArenaTypeEnum.ARENA_TYPE_3VS3));
            party1.sendToField(new GameRolePlayArenaRegistrationStatusMessage(true, PvpArenaStepEnum.ARENA_STEP_WAITING_FIGHT, PvpArenaTypeEnum.ARENA_TYPE_3VS3));


            final ArenaBattle battle = new ArenaBattle(party1, party);
            DAO.getArenas().add(battle);


            party.sendFightProposition(battle.getId());
            party1.sendFightProposition(battle.getId());


            //Invitation
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void startFight(List<Player> team1, List<Player> team2) {
        final DofusMap map = this.getRandomFightingMap();
        final Iterator<Player> it = team1.iterator();
        while (it.hasNext()) {
            final Player p = it.next();
            if (p.getClient().getParty() != null && p.getClient().getParty() instanceof ArenaParty && ((ArenaParty) p.getClient().getParty()).inKolizeum()) {
                this.unregisterGroupForced(p.getClient().getParty().asArena());
            }
            this.unregisterPlayer(p);
            p.getClient().abortGameAction(GameActionTypeEnum.KOLI, new Object[2]);
            p.getClient().delGameAction(GameActionTypeEnum.KOLI);
            lastPositions.put(p.getID(), new Couple<>(p.getCurrentMap().getId(), p.getCell().getId()));
            p.teleport(map.getId(), 0);
        }
        final Iterator<Player> it1 = team2.iterator();
        while (it1.hasNext()) {
            final Player p = it1.next();
            if (p.getClient().getParty() != null && p.getClient().getParty() instanceof ArenaParty && ((ArenaParty) p.getClient().getParty()).inKolizeum()) {
                ((ArenaParty) p.getClient().getParty()).setInKolizeum(false);
                this.unregisterGroupForced(p.getClient().getParty().asArena());
            }
            this.unregisterPlayer(p);
            p.getClient().abortGameAction(GameActionTypeEnum.KOLI, new Object[2]);
            p.getClient().delGameAction(GameActionTypeEnum.KOLI);
            lastPositions.put(p.getID(), new Couple<>(p.getCurrentMap().getId(), p.getCell().getId()));
            p.teleport(map.getId(), 0);
        }
        this.scheduleTask(new StartFightTask(this, FightTypeEnum.FIGHT_TYPE_PVP_ARENA, map, team1, team2), 3000);

    }

    private static final SecureRandom RAND = new SecureRandom();

    private List<ArenaParty> nextWaitingGroups(int bound) {
        final List<ArenaParty> parties = new ArrayList<>();
        int tests = 0;
        while (parties.size() < bound) {
            if (tests > 20 || this.waitingGroups.size() <= bound) {
                return waitingGroups;
            }
            int nextIndex = RAND.nextInt(this.waitingGroups.size() - 1);
            final ArenaParty next = waitingGroups.get(nextIndex);
            if (next != null && !parties.contains(next)) {
                parties.add(next);
            }
            tests++;
        }

        return parties;
    }

    private List<Player> nextWaitingPlayers(int bound) {
        final List<Player> players = new ArrayList<Player>();
        int tests = 0;
        while (players.size() < bound) {
            if (tests > 20 || this.waiting.size() <= bound) {
                return this.waiting;
            }
            int nextIndex = RAND.nextInt(this.waiting.size() - 1);
            final Player next = waiting.get(nextIndex);
            if (next != null && !players.contains(next)) {
                players.add(next);
            }
            tests++;
        }

        return players;
    }

    // protected static final SecureRandom random = new SecureRandom();
    protected static final Random random = new Random();

    protected static final DofusMap getRandomFightingMap() {
        final int nextIndex = random.nextInt(MAPS.length - 1);
        return DAO.getMaps().findTemplate(MAPS[nextIndex]);
    }

    @Override
    public void shutdown() {
        try {
            ArrayList<Player> players = new ArrayList<>(waiting);
            for (Player player : players) {
                if (player.getClient() != null) {
                    player.getClient().abortGameAction(GameActionTypeEnum.KOLI, new Object[2]);
                    player.getClient().delGameAction(GameActionTypeEnum.KOLI);
                }
                player.send(new GameRolePlayArenaRegistrationStatusMessage(false, PvpArenaStepEnum.ARENA_STEP_UNREGISTER, PvpArenaTypeEnum.ARENA_TYPE_3VS3));

            }
            ArrayList<ArenaParty> parties = new ArrayList<>(waitingGroups);
            for (ArenaParty party : parties) {
                party.setInKolizeum(false);
                for (Player player : party.getPlayers()) {
                    if (player.getClient() != null) {
                        player.getClient().abortGameAction(GameActionTypeEnum.KOLI, new Object[2]);
                        player.getClient().delGameAction(GameActionTypeEnum.KOLI);
                    }
                    player.send(new GameRolePlayArenaRegistrationStatusMessage(false, PvpArenaStepEnum.ARENA_STEP_UNREGISTER, PvpArenaTypeEnum.ARENA_TYPE_3VS3));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.shutdown();
    }

    public static void teleportLastPosition(Player p) {
        p.teleport(lastPositions.get(p.getID()).first, lastPositions.get(p.getID()).second);
    }

}

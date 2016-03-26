package koh.game.entities.kolissium;

import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.game.entities.environments.DofusMap;
import koh.game.utils.PeriodicContestExecutor;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Melancholia on 3/20/16.
 */
public class KolizeumExecutor extends PeriodicContestExecutor {

    protected static final int[] MAPS = new int[] {
            94634497,94634499,94634501,
            94634507,94634509,94634511,
            94634513, 94634515,94634517,
            94634519,94634505,94634503,
            141033472,141033474,141034496,
            141034498,141035520,141035522,
            141036544,141036546,141037568,
            141037570,141038592,141038594,
            141295616,141295618,141296640,
            141296642,141297664,141297666,
            141298688,141298690,141299712,
            141299714,141300736,141300738
    };

    public KolizeumExecutor(){
        this.waiting = new CopyOnWriteArrayList<Player>();
        this.waitingGroups = new CopyOnWriteArrayList<>();
        super.initialize();
    }

    private CopyOnWriteArrayList<Player> waiting;
    private CopyOnWriteArrayList<ArenaParty> waitingGroups;

    private static final int TEAM_SIZE = 2;
    private static final int TEAM_LVL_DIFF_MAX = 40;

    private static Comparator<ArenaParty> partySorter;
    private static Comparator<Player> playerSorter;

    static{
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
        waitingGroups.remove(group);
        group.setInKolizeum(false);
    }

    @Override
    public boolean isRegistred(Player player){
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
            boolean success = waitingGroups.addIfAbsent(group);
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
            waitingSize += group.memberCounts();
        }
        if (waitingSize >= TEAM_SIZE * 2) {
            tryToStartFight();
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryToStartFight() {
        final ArrayList<Player> team1 = new ArrayList<Player>();
        final ArrayList<Player> team2 = new ArrayList<Player>();
        int lvlTeam1 = 0;
        int lvlTeam2 = 0;

        final List<ArenaParty> groupSortedList = new ArrayList<ArenaParty>();
        groupSortedList.addAll(this.nextWaitingGroups(2));
        groupSortedList.sort(partySorter);

        for (ArenaParty group : groupSortedList) {
            if (!group.inKolizeum()) {
                waitingGroups.remove(group);
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
            sortedList.addAll(this.nextWaitingPlayers(2 * TEAM_SIZE - (team1.size() + team2.size())));
            Collections.sort(sortedList, playerSorter);
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

                Iterator<Player> it = sortedList.iterator();
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

        int teamDiff = Math.abs(lvlTeam1 - lvlTeam2);
        if (teamDiff > TEAM_LVL_DIFF_MAX) { //TODO check 2 team1's ip vs team2's ip have seen
            return;
        }

        if (team1.size() != TEAM_SIZE || team2.size() != TEAM_SIZE) {
            return;
        }

        //Invitation

    }

    private void startFight(ArrayList<Player> team1, ArrayList<Player> team2) {
        //DofusMap map = World.getRandomFightingMap();
        Iterator<Player> it = team1.iterator();
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

    protected static final SecureRandom random = new SecureRandom();

    protected static final DofusMap getRandomMap(){
        final int nextIndex = random.nextInt(MAPS.length - 1);
        return DAO.getMaps().findTemplate(MAPS[nextIndex]);
    }

}

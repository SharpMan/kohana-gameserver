package koh.game.entities.command;

import koh.game.entities.actors.MonsterGroup;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/12/16.
 */
public class SpawnsCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return "Show monster spawns";
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        StringBuilder sb = new StringBuilder("Monsters in ");
        client.getCharacter().getCurrentMap().getMonsters().forEach(e -> {
            try {
                client.getCharacter().getCurrentMap().spawnActor(e);
                System.out.println(e == null);
            }
            catch (Exception ee){
                ee.printStackTrace();
            }
                }
        );
        sb.append(client.getCharacter().getCurrentMap().getId()).append(" ").append(client.getCharacter().getCurrentMap().getMonsters().size()).append("\n");
        client.getCharacter().getCurrentMap().getMyGameActors()
                .entrySet()
                .stream()
                .filter(e -> e.getValue() instanceof MonsterGroup)
                .map(e -> ((MonsterGroup) e.getValue()))
                .forEach(e -> {
                    sb.append("ID ").append(e.getID()).append("\n");
                    sb.append(e.getMainCreature().getMonster().getNameId()).append(" level ");
                    sb.append(e.getMainCreature().getLevel());
                    sb.append(" isFix ").append(e.isFix()).append("\n");
                    e.getMonsters().forEach(mob -> {
                        sb.append(mob.getMonster().getNameId()).append(" level ");
                        sb.append(mob.getLevel()).append("\n");
                    });
                });
        client.send(new ConsoleMessage((byte)0,sb.toString()));

    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 0;
    }

    @Override
    public int argsNeeded() {
        return 1;
    }
}

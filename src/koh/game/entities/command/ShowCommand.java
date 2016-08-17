package koh.game.entities.command;

import koh.game.entities.mob.MonsterGrade;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

/**
 * Created by Melancholia on 8/8/16.
 */
public class ShowCommand implements PlayerCommand {


    @Override
    public String getDescription() {
        return "Show monsters in the fight";
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        System.out.println("****************************************************");
        final StringBuilder sb = new StringBuilder("Monsters=====================\n");
        if(client.getCharacter().getFight() != null) {
            client.getCharacter().getFight().fighters().filter(f -> f instanceof MonsterFighter).forEach(mb -> {
                final MonsterGrade mob = mb.asMonster().getGrade();
                sb.append(mob.getMonster().getNameId()).append("Id ").append(mob.getMonsterId()).append(mob.getGrade());
                sb.append("\n-> Force ");
                sb.append(mob.getStrenght()).append(" Intelligence");
                sb.append(mob.getIntelligence()).append(" Agility ");
                sb.append(mob.getAgility()).append(" Chance ");
                sb.append(mob.getChance()).append("\n");
            });
        }
        else{
            client.getCharacter().getCurrentMap().getMonsters().forEach(gr -> {
                sb.append("Group ").append(gr.getID()+" =====================\n");
                sb.append(gr.getMainCreature().getMonster().getNameId()).append("Id ").append(gr.getMainCreature().getMonsterId()).append(gr.getMainCreature().getGrade());
                sb.append("\n-> Force ");
                sb.append(gr.getMainCreature().getStrenght()).append(" Intelligence ");
                sb.append(gr.getMainCreature().getIntelligence()).append(" Agility ");
                sb.append(gr.getMainCreature().getAgility()).append("Chance ");
                sb.append(gr.getMainCreature().getChance()).append("\n");
                gr.getMonsters().forEach(mob -> {
                    sb.append(mob.getMonster().getNameId()).append("Id ").append(mob.getMonsterId()).append(mob.getGrade());
                    sb.append("\n-> Force ");
                    sb.append(mob.getStrenght()).append(" Intelligence ");
                    sb.append(mob.getIntelligence()).append(" Agility");
                    sb.append(mob.getAgility()).append(" Chance ");
                    sb.append(mob.getChance()).append("\n");
                });
            });
        }

        client.send(new ConsoleMessage((byte)0,sb.toString()));


    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 1;
    }

    @Override
    public int argsNeeded() {
        return 0;
    }
}

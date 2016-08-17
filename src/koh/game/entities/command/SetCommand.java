package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.MonsterFighter;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.authorized.ConsoleMessage;

/**
 * Created by Melancholia on 8/8/16.
 */
public class SetCommand implements PlayerCommand {


    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        final int monsterId = Integer.parseInt(args[0].substring(0, args[0].length() -1));
        final byte grade = Byte.parseByte(args[0].substring(args[0].length() -1));
        final int qua = Integer.parseInt(args[2]);
        final MonsterTemplate tm = DAO.getMonsters().find(monsterId);
        final Fighter mob = client.getCharacter().getFight().fighters()
                .filter(f -> f instanceof MonsterFighter && f.asMonster().getGrade().getMonsterId() == monsterId && f.asMonster().getGrade().getGrade() == grade)
                .findFirst().orElse(null);
        if(tm == null){
            client.send(new ConsoleMessage((byte)2, "Monstre inconnu veuillez verifiez la valeur"));
        }
        else if(mob == null){
            client.send(new ConsoleMessage((byte)2, "Monstre absent en combat"));
        }
        else{
            final MonsterGrade gr = tm.getGrade(grade);
            switch (args[1]){
                case "force":
                    gr.setStrenght(qua);
                    gr.getStats().getEffect(StatsEnum.STRENGTH).base = qua;
                    mob.getStats().getEffect(StatsEnum.STRENGTH).base = qua;
                    DAO.getMonsters().update(gr,"strength",qua);
                    break;
                case "intel":
                case "intelligence":
                    gr.setIntelligence(qua);
                    gr.getStats().getEffect(StatsEnum.INTELLIGENCE).base = qua;
                    mob.getStats().getEffect(StatsEnum.INTELLIGENCE).base = qua;
                    DAO.getMonsters().update(gr,"intelligence",qua);
                    break;
                case "agi":
                case "agilite":
                case "agility":
                    gr.setAgility(qua);
                    gr.getStats().getEffect(StatsEnum.AGILITY).base = qua;
                    mob.getStats().getEffect(StatsEnum.AGILITY).base = qua;
                    DAO.getMonsters().update(gr,"agility",qua);
                    break;
                case "chance":
                    gr.setChance(qua);
                    gr.getStats().getEffect(StatsEnum.CHANCE).base = qua;
                    mob.getStats().getEffect(StatsEnum.CHANCE).base = qua;
                    DAO.getMonsters().update(gr,"chance",qua);
                    break;
            }

            client.send(new ConsoleMessage((byte)1, "Stat du monstre reacfraichit !"));
        }
    }

    @Override
    public boolean can(WorldClient client) {
        if(client.getCharacter().getFight() == null){
            client.send(new ConsoleMessage((byte)2, "Vous n'etes pas en combat"));
        }
        return client.getCharacter().getFight() != null;
    }

    @Override
    public int roleRestrained() {
        return 1;
    }

    @Override
    public int argsNeeded() {
        return 3;
    }

}

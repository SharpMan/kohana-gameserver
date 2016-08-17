package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.mob.MonsterGrade;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayGroupMonsterInformations;
import koh.protocol.types.game.context.roleplay.GroupMonsterStaticInformations;
import koh.protocol.types.game.context.roleplay.MonsterInGroupInformations;
import koh.protocol.types.game.context.roleplay.MonsterInGroupLightInformations;
import koh.utils.Enumerable;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/11/16.
 */
public class SpawnFixCommand implements PlayerCommand {
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args)  {
        try {
            final String[] ar = args[0].split(";");
            final int groupCount = ar.length - 1;
            final int[] firstMonster = Enumerable.stringToIntArray(ar[0]);
            final MonsterGrade mainMonster = DAO.getMonsters().find(firstMonster[0]).getLevelOrNear(firstMonster[1]);

            final MonsterGroup gr = MonsterGroup.builder()
                    .fix(false)
                    .fixedCell(client.getCharacter().getCell().getId())
                    .gameRolePlayGroupMonsterInformations(
                            new GameRolePlayGroupMonsterInformations(client.getCharacter().getCurrentMap().getNextActorId(),
                                    mainMonster.getMonster().getEntityLook(),
                                    new EntityDispositionInformations(client.getCharacter().getCell().getId(), client.getCharacter().getDirection()),
                                    new GroupMonsterStaticInformations(
                                            new MonsterInGroupLightInformations(mainMonster.getMonsterId(), mainMonster.getGrade()),
                                            new MonsterInGroupInformations[groupCount]),
                                    (short) -1,
                                    (byte) -1,
                                    (byte) -1,
                                    false,
                                    false,
                                    false))
                    .build();
            gr.setActorCell(client.getCharacter().getCell());
            if (groupCount > 0) {
                for (int ii = 0; ii < groupCount; ii++) {
                    final int[] monster = Enumerable.stringToIntArray(ar[ii + 1]);
                    final MonsterGrade randMonster = DAO.getMonsters().find(monster[0]).getLevelOrNear(monster[1]);
                    gr.getGameRolePlayGroupMonsterInformations().staticInfos.underlings[ii] = new MonsterInGroupInformations(randMonster.getMonsterId(), randMonster.getGrade(), randMonster.getMonster().getEntityLook());
                }
            }
            client.getCharacter().getCurrentMap().addMonster(gr);
            client.getCharacter().getCurrentMap().spawnActor(gr);
            DAO.getMapMonsters().insert(client.getCharacter().getCurrentMap().getId(), client.getCharacter().getCell().getId(), client.getCharacter().getDirection(), mainMonster.getMonsterId() + "," + mainMonster.getGrade(), args[0].substring((mainMonster.getMonsterId() + "," + mainMonster.getGrade()).length() + 1));
            client.send(new ConsoleMessage((byte)1,"Monster spawned"));
        }
        catch (Exception e){
            e.printStackTrace();
            client.send(new ConsoleMessage((byte)2,"Monster notfound"));
        }
    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 3;
    }

    @Override
    public int argsNeeded() {
        return 1;
    }
}

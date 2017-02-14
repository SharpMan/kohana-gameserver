package koh.game.entities.item.actions;

import koh.game.dao.DAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemAction;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import koh.protocol.types.game.context.EntityDispositionInformations;
import koh.protocol.types.game.context.roleplay.GameRolePlayGroupMonsterInformations;
import koh.protocol.types.game.context.roleplay.GroupMonsterStaticInformations;
import koh.protocol.types.game.context.roleplay.MonsterInGroupInformations;
import koh.protocol.types.game.context.roleplay.MonsterInGroupLightInformations;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.ObjectEffectDice;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Melancholia on 1/16/17.
 */
public class SpawnArenaAction extends ItemAction  {

    public final static int[] MAPS = {4981250,4980738,4980737,4980736,4981248,4981760,4981761,4981249,11796994,11796482,11796481,11796480,11796992,11797504,11797505,11797506,11796993,4980738};

    public SpawnArenaAction(String[] args, String criteria, int template) {
        super(args, criteria, template);
    }


    @Override
    public boolean execute(Player possessor, Player p, int cell) {
        if(!super.execute(possessor,p, cell) || !ArrayUtils.contains(MAPS,possessor.getMapid()))
            return false;
        final InventoryItem item = p.getInventoryCache().find(cell);
        if(item == null){
            return false;
        }
        final int groupCount = (int) item.getEffects().stream().filter(e-> e.actionId == 623).count() -1;
        if(groupCount == -1){
            return false;
        }

        MonsterGroup gr = null;

        int ii = -1;

        for (ObjectEffect effect : item.getEffects()) {
            if(effect.actionId == 623){
                final ObjectEffectDice ef = (ObjectEffectDice) effect;
                if(ii == -1){
                    final MonsterGrade mainMonster = DAO.getMonsters().find(ef.diceConst).getLevelOrNear(ef.diceNum);
                    gr = MonsterGroup.builder()
                            .fix(false)
                            .fixedCell(possessor.getCell().getId())
                            .gameRolePlayGroupMonsterInformations(
                                    new GameRolePlayGroupMonsterInformations(possessor.getCurrentMap().getNextActorId(),
                                            mainMonster.getMonster().getEntityLook(),
                                            new EntityDispositionInformations(possessor.getCell().getId(), possessor.getDirection()),
                                            new GroupMonsterStaticInformations(
                                                    new MonsterInGroupLightInformations(mainMonster.getMonsterId(), mainMonster.getGrade()),
                                                    new MonsterInGroupInformations[groupCount]),
                                            (short) -1,
                                            (byte) -1,
                                            (byte) -1,
                                            false,
                                            false,
                                            false)
                            )
                            .build();
                }else{
                    final MonsterGrade randMonster = DAO.getMonsters().find(ef.diceConst).getLevelOrNear(ef.diceNum);
                    gr.getGameRolePlayGroupMonsterInformations().staticInfos.underlings[ii] = new MonsterInGroupInformations(randMonster.getMonsterId(), randMonster.getGrade(), randMonster.getMonster().getEntityLook());
                }

            }
            ii++;
        }

        gr.setActorCell(possessor.getCell());
        possessor.getCurrentMap().addMonster(gr);
        possessor.getCurrentMap().spawnActor(gr);
        gr.setArena(true);

        p.getInventoryCache().safeDelete(item,1);


        return true;
    }
}

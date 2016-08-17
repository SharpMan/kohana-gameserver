package koh.game.fights.effects;

import koh.game.fights.Fighter;

import java.util.Optional;

/**
 * Created by Melancholia on 8/15/16.
 */
public class EffectRestitue extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {

        final Fighter zomby = castInfos.getFight().getDeadFighters()
                .filter(f -> !f.hasSummoner())
                .findFirst()
                .orElse(castInfos.getFight().getDeadFighters()
                        .findFirst().orElse(null));

        if(zomby != null && !castInfos.getCell().hasFighter()){
            
        }


        return 0;
    }


}

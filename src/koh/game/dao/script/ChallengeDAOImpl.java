package koh.game.dao.script;

import com.google.common.collect.ImmutableMap;
import koh.game.dao.api.ChallengeDAO;
import koh.game.entities.fight.*;
import koh.game.entities.fight.Challenge;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Melancholia on 9/24/16.
 */
public class ChallengeDAOImpl extends ChallengeDAO {

    //private static ImmutableClassToInstanceMap<Challenge> challenges;
    private ImmutableMap<Integer, Class<? extends Challenge>> challenges;
    private Random rnd;


    @Override
    public void start() {
        this.challenges = ImmutableMap.<Integer, Class<? extends Challenge>>builder()
                .put(1, ZombyChallenge.class)
                .put(2, StatueChallenge.class)
                .put(3 , VolunterDesigner.class)
                .put(4, Sursis.class)
                .put(5 ,Econome.class)
                .put(6, Versatile.class)
                .put(7, Jardinier.class)
                .put(9, Barbare.class)
                .put(8, Nomade.class)
                .put(10, Cruel.class)
                .put(11, Mystique.class)
                .put(12, Fossoyeur.class)
                .put(14, RoyalCasino.class)
                .put(15, Araknophile.class)
                .put(17, Intouchable.class)
                .put(18, Incurable.class)
                .put(19, SweetHand.class)
                .put(20, Elemantaire.class)
                .put(21, Circulez.class)
                .put(22, TimeRunning.class)
                .put(23, SightLose.class)
                .put(25, Ordonated.class)
                .put(28, NoSoumise.class)
                .put(29, Soumis.class)
                .put(30, SmallFirst.class)
                .put(31, Focus.class)
                .put(32, Elitiste.class)
                .put(33, Survivant.class)
                .put(34, Imprevisible.class)
                .put(35, GageKiller.class)
                .put(36, Hardi.class)
                .put(37, Collant.class)
                .put(38, Blitzkrieg.class)
                .put(39, Anachorete.class)
                .put(40, Pusillanime.class)
                .put(41, Petulant.class)
                .put(42, TwoForOne.class)
                .put(43, Abnegation.class)
                .put(45, Duel.class)
                .put(46, EachMonster.class)
                .put(47, Contamination.class)
                .put(48, MuleFirst.class)
                .put(49, Protection.class)

        .build();
        this.rnd = new SecureRandom();
    }


    @Override
    public Class<? extends Challenge> find(int id){
        return this.challenges.get(id);
    }

    @Override
    public int pop(){
        final int key = this.challenges.keySet().asList().get(rnd.nextInt(challenges.size()));
        return key;
    }

    @Override
    public void stop() {

    }
}

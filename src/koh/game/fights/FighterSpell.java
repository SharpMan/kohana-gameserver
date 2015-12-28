package koh.game.fights;

import java.util.ArrayList;
import java.util.HashMap;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffSpellCoolDown;

/**
 *
 * @author Neo-Craft
 */
public class FighterSpell {

    private HashMap<Integer, ArrayList<SpellTarget>> myTargets = new HashMap<>();
    public HashMap<Integer, SpellinitialCooldown> myinitialCooldown = new HashMap<>();

    public FighterBuff Buffs;

    public FighterSpell(FighterBuff FighterBuffs) {
        this.Buffs = FighterBuffs;
    }

    public boolean canLaunchSpell(SpellLevel Spell, int TargetId) {
        if (Spell.getMinCastInterval() > 0) {
            if (this.myinitialCooldown.containsKey(Spell.getSpellId())) {
                if (this.myinitialCooldown.get(Spell.getSpellId()) != null) {
                    int newCoolDown = minCastInterval(Spell.getSpellId());
                    if ((newCoolDown == 0 ? this.myinitialCooldown.get(Spell.getSpellId()).initialCooldown : newCoolDown) > 0) {
                        return false;
                    }
                }
            }
        }
        if (Spell.getMaxCastPerTurn() == 0 && Spell.getMaxCastPerTarget() == 0) {
            return true;
        }

        if (Spell.getMaxCastPerTurn() > 0) {
            if (this.myTargets.containsKey(Spell.getSpellId())) {
                if (this.myTargets.get(Spell.getSpellId()).size() >= Spell.getMaxCastPerTurn()) {
                    return false;
                }
            }
        }

        if (Spell.getMaxCastPerTarget() > 0) {
            if (this.myTargets.containsKey(Spell.getSpellId())) {
                if (this.myTargets.get(Spell.getSpellId()).stream().filter(x -> x.targetId == TargetId).count() >= Spell.getMaxCastPerTarget()) {
                    //System.out.println("ici" + this.myTargets.get(Spell.getSpellId()).stream().filter(x -> x.targetId == targetId).count());
                    return false;
                }
            }
        }

        return true;
    }

    public int minCastInterval(int Spell) {
        BuffEffect Buff = this.Buffs.getAllBuffs().filter(x -> x instanceof BuffSpellCoolDown && ((BuffSpellCoolDown) x).Spell == Spell).findFirst().orElse(null);
        if (Buff == null) {
            return 0;
        } else {
            return ((BuffSpellCoolDown) Buff).Value - Buff.duration;
        }
    }

    public void actualize(SpellLevel spell, int targetId) {
        if (spell.getMinCastInterval() > 0) {
            if (!this.myinitialCooldown.containsKey(spell.getSpellId())) {
                this.myinitialCooldown.put(spell.getSpellId(), new SpellinitialCooldown(spell.getMinCastInterval()));
            } else {
                this.myinitialCooldown.get(spell.getSpellId()).initialCooldown = spell.getMinCastInterval();
            }
        }

        if (spell.getMaxCastPerTurn() == 0 && spell.getMaxCastPerTarget() == 0) {
            return;
        }

        if (!this.myTargets.containsKey(spell.getSpellId())) {
            this.myTargets.put(spell.getSpellId(), new ArrayList<>());
        }
        this.myTargets.get(spell.getSpellId()).add(new SpellTarget(targetId));
    }

    public void endTurn() {
        myTargets.values().stream().forEach((Targets) -> {
            Targets.clear();
        });

        this.myinitialCooldown.values().stream().forEach((initialCooldown) -> {
            initialCooldown.decrement();
        });

    }

    public class SpellinitialCooldown {

        public byte initialCooldown;

        public SpellinitialCooldown(byte initialCooldown) {
            this.initialCooldown = initialCooldown;
        }

        public void decrement() {
            this.initialCooldown--;
        }
    }

    public class SpellTarget {

        public int targetId;

        public SpellTarget(int TargetId) {
            this.targetId = TargetId;
        }
    }

}

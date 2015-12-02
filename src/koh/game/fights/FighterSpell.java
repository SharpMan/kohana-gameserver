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
        if (Spell.minCastInterval > 0) {
            if (this.myinitialCooldown.containsKey(Spell.spellId)) {
                if (this.myinitialCooldown.get(Spell.spellId) != null) {
                    int newCoolDown = minCastInterval(Spell.spellId);
                    if ((newCoolDown == 0 ? this.myinitialCooldown.get(Spell.spellId).initialCooldown : newCoolDown) > 0) {
                        return false;
                    }
                }
            }
        }
        if (Spell.maxCastPerTurn == 0 && Spell.maxCastPerTarget == 0) {
            return true;
        }

        if (Spell.maxCastPerTurn > 0) {
            if (this.myTargets.containsKey(Spell.spellId)) {
                if (this.myTargets.get(Spell.spellId).size() >= Spell.maxCastPerTurn) {
                    return false;
                }
            }
        }

        if (Spell.maxCastPerTarget > 0) {
            if (this.myTargets.containsKey(Spell.spellId)) {
                if (this.myTargets.get(Spell.spellId).stream().filter(x -> x.targetId == TargetId).count() >= Spell.maxCastPerTarget) {
                    //System.out.println("ici" + this.myTargets.get(spell.spellId).stream().filter(x -> x.targetId == targetId).count());
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
            return ((BuffSpellCoolDown) Buff).Value - Buff.Duration;
        }
    }

    public void actualize(SpellLevel spell, int targetId) {
        if (spell.minCastInterval > 0) {
            if (!this.myinitialCooldown.containsKey(spell.spellId)) {
                this.myinitialCooldown.put(spell.spellId, new SpellinitialCooldown(spell.minCastInterval));
            } else {
                this.myinitialCooldown.get(spell.spellId).initialCooldown = spell.minCastInterval;
            }
        }

        if (spell.maxCastPerTurn == 0 && spell.maxCastPerTarget == 0) {
            return;
        }

        if (!this.myTargets.containsKey(spell.spellId)) {
            this.myTargets.put(spell.spellId, new ArrayList<>());
        }
        this.myTargets.get(spell.spellId).add(new SpellTarget(targetId));
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

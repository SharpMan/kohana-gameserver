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

    public boolean CanLaunchSpell(SpellLevel Spell, int TargetId) {
        if (Spell.minCastInterval > 0) {
            if (this.myinitialCooldown.containsKey(Spell.spellId)) {
                if (this.myinitialCooldown.get(Spell.spellId) != null) {
                    int newCoolDown = MinCastInterval(Spell.spellId);
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
                if (this.myTargets.get(Spell.spellId).stream().filter(x -> x.TargetId == TargetId).count() >= Spell.maxCastPerTarget) {
                    //System.out.println("ici" + this.myTargets.get(Spell.spellId).stream().filter(x -> x.TargetId == TargetId).count());
                    return false;
                }
            }
        }

        return true;
    }

    public int MinCastInterval(int Spell) {
        BuffEffect Buff = this.Buffs.GetAllBuffs().filter(x -> x instanceof BuffSpellCoolDown && ((BuffSpellCoolDown) x).Spell == Spell).findFirst().orElse(null);
        if (Buff == null) {
            return 0;
        } else {
            return ((BuffSpellCoolDown) Buff).Value - Buff.Duration;
        }
    }

    public void Actualise(SpellLevel Spell, int TargetId) {
        if (Spell.minCastInterval > 0) {
            if (!this.myinitialCooldown.containsKey(Spell.spellId)) {
                this.myinitialCooldown.put(Spell.spellId, new SpellinitialCooldown(Spell.minCastInterval));
            } else {
                this.myinitialCooldown.get(Spell.spellId).initialCooldown = Spell.minCastInterval;
            }
        }

        if (Spell.maxCastPerTurn == 0 && Spell.maxCastPerTarget == 0) {
            return;
        }

        if (!this.myTargets.containsKey(Spell.spellId)) {
            this.myTargets.put(Spell.spellId, new ArrayList<>());
        }
        this.myTargets.get(Spell.spellId).add(new SpellTarget(TargetId));
    }

    public void EndTurn() {
        myTargets.values().stream().forEach((Targets) -> {
            Targets.clear();
        });

        this.myinitialCooldown.values().stream().forEach((initialCooldown) -> {
            initialCooldown.Decrement();
        });

    }

    public class SpellinitialCooldown {

        public byte initialCooldown;

        public SpellinitialCooldown(byte initialCooldown) {
            this.initialCooldown = initialCooldown;
        }

        public void Decrement() {
            this.initialCooldown--;
        }
    }

    public class SpellTarget {

        public int TargetId;

        public SpellTarget(int TargetId) {
            this.TargetId = TargetId;
        }
    }

}

package koh.game.fights;

import java.util.ArrayList;
import java.util.HashMap;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffSpellCoolDown;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class FighterSpell {

    private HashMap<Integer, ArrayList<SpellTarget>> myTargets = new HashMap<>();
    @Getter
    private HashMap<Integer, SpellinitialCooldown> initialCooldown = new HashMap<>();

    public FighterBuff Buffs;

    public FighterSpell(FighterBuff FighterBuffs) {
        this.Buffs = FighterBuffs;
    }

    public boolean canLaunchSpell(SpellLevel Spell, int TargetId) {
        if (Spell.getMinCastInterval() > 0) {
            if (this.initialCooldown.containsKey(Spell.getSpellId())) {
                if (this.initialCooldown.get(Spell.getSpellId()) != null) {
                    int newCoolDown = minCastInterval(Spell.getSpellId());
                    if ((newCoolDown == 0 ? this.initialCooldown.get(Spell.getSpellId()).initialCooldown : newCoolDown) > 0) {
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
                    return false;
                }
            }
        }

        return true;
    }

    public byte minCastInterval(int spell) {
        BuffEffect buff = this.Buffs.getAllBuffs().filter(x -> x instanceof BuffSpellCoolDown && ((BuffSpellCoolDown) x).spell == spell).findFirst().orElse(null);
        if (buff == null) {
            return 0;
        } else {
            return (byte) (((BuffSpellCoolDown) buff).value - buff.duration);
        }
    }

    public void actualize(SpellLevel spell, int targetId) {
        if (spell.getMinCastInterval() > 0) {
            if (!this.initialCooldown.containsKey(spell.getSpellId())) {
                this.initialCooldown.put(spell.getSpellId(), new SpellinitialCooldown(spell.getMinCastInterval()));
            } else {
                this.initialCooldown.get(spell.getSpellId()).initialCooldown = spell.getMinCastInterval();
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

        this.initialCooldown.values().stream().forEach((initialCooldown) -> {
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

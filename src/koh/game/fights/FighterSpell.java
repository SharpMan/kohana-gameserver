package koh.game.fights;

import java.util.ArrayList;
import java.util.HashMap;

import koh.game.entities.item.InventoryItem;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffSpellCoolDown;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class FighterSpell {

    private static final int WEAPON_ID = -2;

    private HashMap<Integer, ArrayList<SpellTarget>> myTargets = new HashMap<>();
    @Getter
    private HashMap<Integer, SpellInitialCooldown> initialCooldown = new HashMap<>();

    public FighterBuff Buffs;

    public FighterSpell(FighterBuff FighterBuffs) {
        this.Buffs = FighterBuffs;
    }

    public boolean canLaunchSpellId(int id){
        if (this.initialCooldown.containsKey(id)) {
            if (this.initialCooldown.get(id) != null) {
                int newCoolDown = minCastInterval(id);
                if ((newCoolDown == 0 ? this.initialCooldown.get(id).initialCooldown : newCoolDown) > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canLaunchSpell(SpellLevel spell, int targetId) {
        if (spell.getMinCastInterval() > 0) {
            if (this.initialCooldown.containsKey(spell.getSpellId())) {
                if (this.initialCooldown.get(spell.getSpellId()) != null) {
                    int newCoolDown = minCastInterval(spell.getSpellId());
                    if ((newCoolDown == 0 ? this.initialCooldown.get(spell.getSpellId()).initialCooldown : newCoolDown) > 0) {
                        return false;
                    }
                }
            }
        }
        if (spell.getMaxCastPerTurn() == 0 && spell.getMaxCastPerTarget() == 0) {
            return true;
        }

        if (spell.getMaxCastPerTurn() > 0) {
            if (this.myTargets.containsKey(spell.getSpellId())) {
                if (this.myTargets.get(spell.getSpellId()).size() >= spell.getMaxCastPerTurn()) {
                    return false;
                }
            }
        }

        if (spell.getMaxCastPerTarget() > 0) {
            if (this.myTargets.containsKey(spell.getSpellId())) {
                if (this.myTargets.get(spell.getSpellId()).stream().filter(x -> x.targetId == targetId).count() >= spell.getMaxCastPerTarget()) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean canLaunchWeapon(InventoryItem item){
        if (!this.myTargets.containsKey(WEAPON_ID)) {
            return true;
        }
        return item.getWeaponTemplate().getMaxCastPerTurn() > this.myTargets.get(WEAPON_ID).size();
    }

    public void actualize(InventoryItem item , int targetId){
        if (!this.myTargets.containsKey(WEAPON_ID)) {
            this.myTargets.put(WEAPON_ID, new ArrayList<>());
        }
        this.myTargets.get(WEAPON_ID).add(new SpellTarget(targetId));
    }

    public byte minCastInterval(int spell) {
        final BuffEffect buff = this.Buffs.getAllBuffs().filter(x -> x instanceof BuffSpellCoolDown && ((BuffSpellCoolDown) x).spell == spell).findFirst().orElse(null);
        if (buff == null) {
            return 0;
        } else {
            return (byte) (((BuffSpellCoolDown) buff).value - buff.duration);
        }
    }

    public void actualize(SpellLevel spell, int targetId) {
        if (spell.getMinCastInterval() > 0) {
            if (!this.initialCooldown.containsKey(spell.getSpellId())) {
                this.initialCooldown.put(spell.getSpellId(), new SpellInitialCooldown(spell.getMinCastInterval()));
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

    public void setCooldown(int spellid,byte relance){
        this.initialCooldown.put(spellid, new SpellInitialCooldown(relance));
    }

    public void endTurn() {
        this.myTargets.values().stream().forEach(ArrayList::clear);
        this.initialCooldown.values().stream().forEach(SpellInitialCooldown::decrement);
    }

    public class SpellInitialCooldown {

        public byte initialCooldown;

        public SpellInitialCooldown(byte initialCooldown) {
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

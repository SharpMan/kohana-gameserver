package koh.game.entities.achievement;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemTemplate;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.messages.game.context.roleplay.emote.EmoteListMessage;
import koh.protocol.messages.messages.game.tinsel.OrnamentGainedMessage;
import koh.protocol.messages.messages.game.tinsel.TitleGainedMessage;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Melancholia on 12/27/16.
 */
@Builder
public class AchievementTemplate {

    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private int category;
    @Getter
    private int iconId, points, order, level;
    @Getter
    private double kamasRatio, experienceRatio;
    @Getter
    private boolean kamasScaleWithPlayerLevel;
    @Getter
    private AchievementGoal[] objectives;
    @Getter
    private AchievementReward[] rewards;

    private static final double REWARD_SCALE_CAP = 1.5;
    private static final double REWARD_REDUCED_SCALE = 0.7;

    //duration 1
    public int getKamasReward(double duration, int pPlayerLevel)
    {
        final int lvl = ((kamasScaleWithPlayerLevel) ? pPlayerLevel : this.level);
        return (int) Math.floor(((((Math.pow(lvl, 2) + (20 * lvl)) - 20) * kamasRatio) * duration));
    }

    public int getExperienceReward(int pPlayerLevel, int pXpBonus, double duration)
    {
        int rewLevel;
        double xpBonus = (1 + (pXpBonus / 100));
        if (pPlayerLevel > this.level)
        {
            rewLevel = Math.min(pPlayerLevel, (int) Math.floor(this.level * REWARD_SCALE_CAP));
            return (int) Math.floor(((((1 - REWARD_REDUCED_SCALE) * this.getFixeExperienceReward(this.level, duration, experienceRatio)) + (REWARD_REDUCED_SCALE * this.getFixeExperienceReward(rewLevel, duration, this.experienceRatio))) * xpBonus));
        };
        return (int) Math.floor((this.getFixeExperienceReward(pPlayerLevel, duration, experienceRatio) * xpBonus));
    }

    private double getFixeExperienceReward(int level, double duration, double xpRatio)
    {
        return ((((level * Math.pow((100 + (2 * level)), 2)) / 20) * duration) * xpRatio);
    }
    
    public void award(Player p){
        for (AchievementReward reward : rewards) {
            if(reward.pass(p.getLevel())){
                for (int i = 0; i < reward.getItemsReward().length; i++) {
                    final ItemTemplate template = DAO.getItemTemplates().getTemplate(reward.getItemsReward()[i]);
                    final InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), template.getId(), 63, p.getID(), reward.getItemsQuantityReward()[i], EffectHelper.generateIntegerEffect(template.getPossibleEffects(), EffectGenerationType.NORMAL, template.isWeapon()));
                    if (p.getInventoryCache().add(item, true))
                        item.setNeedInsert(true);

                }
                for (int i : reward.getEmotesReward()) {
                    p.setEmotes(ArrayUtils.add(p.getEmotes(),(byte)i));
                    p.send(new EmoteListMessage(p.getEmotes()));
                }
                for (int i : reward.getOrnamentsReward()) {
                    p.setOrnaments(ArrayUtils.add(p.getOrnaments(), i));
                    p.send(new OrnamentGainedMessage((short)i));
                }
                for (int i : reward.getSpellsReward()) {
                    p.getMySpells().addSpell(i, p.getMySpells().firstLevel, p.getMySpells().getFreeSlot(), p.getClient());
                }
                for (int i : reward.getTitlesReward()) {
                    p.setTitles(ArrayUtils.add(p.getTitles(), i));
                    p.send(new TitleGainedMessage(i));
                }
            }
        }
        
        
    }


}

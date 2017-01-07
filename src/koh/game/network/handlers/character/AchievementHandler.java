package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
import koh.game.entities.achievement.AchievementTemplate;
import koh.game.entities.actors.character.AchievementBook;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.achievement.*;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.startup.StartupActionFinishedMessage;
import koh.protocol.messages.game.startup.StartupActionsAllAttributionMessage;
import koh.protocol.types.game.achievement.Achievement;
import koh.protocol.types.game.achievement.AchievementObjective;
import koh.protocol.types.game.achievement.AchievementStartedObjective;

import java.util.Arrays;

/**
 * Created by Melancholia on 12/27/16.
 */
public class AchievementHandler {

    @HandlerAttribute(ID = 6537)
    public static void handleStartupActionsAllAttributionMessage(WorldClient client,StartupActionsAllAttributionMessage msg){

    }

    @HandlerAttribute(ID= AchievementDetailedListRequestMessage.M_ID)
    public static void handleAchievementDetailedListRequestMessage(WorldClient client, AchievementDetailedListRequestMessage msg){
        client.send(new AchievementDetailedListMessage(client.getCharacter().getAchievements().getStartedAchievement(msg.categoryId),
                client.getCharacter().getAchievements().getFinishedAchievement(msg.categoryId)
        ));
    }

    @HandlerAttribute(ID = AchievementDetailsRequestMessage.M_ID)
    public static void handleAchievementDetailsRequestMessage(WorldClient client, AchievementDetailsRequestMessage msg){
        final AchievementTemplate template = DAO.getAchievements().find(msg.achievementId);
        if(template == null){
            client.send(new BasicNoOperationMessage());
            return;
        }
        Arrays.stream(template.getObjectives())
                .map(g-> g.getCriterion())
                .forEach(System.out::println);
        final AchievementBook.AchievementInfo info = client.getCharacter().getAchievements().getAchievement((short) msg.achievementId);
        if(info != null){
            client.send(new AchievementDetailsMessage(info));
        }else{
            client.send(new AchievementDetailsMessage(new Achievement(msg.achievementId,
                    new AchievementObjective[0],
                    Arrays.stream(template.getObjectives())
                            .map(g-> new AchievementStartedObjective(g.getId(), 1,0))
                            .toArray(AchievementStartedObjective[]::new)
            )));
        }
    }

    @HandlerAttribute(ID = AchievementRewardRequestMessage.M_ID)
    public static void handleAchievementRewardRequestMessage(WorldClient client, AchievementRewardRequestMessage msg){
        if(client.isGameAction(GameActionTypeEnum.FIGHT)){
            client.send(new AchievementRewardErrorMessage(msg.achievementId));
            return;
        }
        if(msg.achievementId == -1){
            int xpSum = 0;
            for (Short val : client.getCharacter().getAchievements().getRewardsOnHold()) {
                final AchievementTemplate achievementTemplate = DAO.getAchievements().find(val);
                if(achievementTemplate == null){
                    client.send(new AchievementRewardErrorMessage(val));
                }
                xpSum += achievementTemplate.getExperienceReward(client.getCharacter().getLevel(),
                        DAO.getSettings().getIntElement("Rate.Achievement"),
                        1
                );
                achievementTemplate.award(client.getCharacter());
                client.getCharacter().getInventoryCache().addKamas(achievementTemplate.getKamasReward(1,client.getCharacter().getLevel()),true);
                client.send(new AchievementRewardSuccessMessage(val));
            }
            if(xpSum > 0){
                client.getCharacter().addExperience(xpSum);
                client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE,8,String.valueOf(xpSum)));
            }
            client.getCharacter().getAchievements().getRewardsOnHold().clear();
            return;
        }
        if(!client.getCharacter().getAchievements().isRewarded(msg.achievementId)){
            client.send(new AchievementRewardErrorMessage(msg.achievementId));
            return;
        }
        client.getCharacter().getAchievements().unAward(msg.achievementId);
        final AchievementTemplate achievementTemplate = DAO.getAchievements().find(msg.achievementId);
        if(achievementTemplate == null){
            client.send(new AchievementRewardErrorMessage(msg.achievementId));
            return;
        }
        final int exp = achievementTemplate.getExperienceReward(client.getCharacter().getLevel(),
                DAO.getSettings().getIntElement("Rate.Achievement"),
                1
        );
        if(exp > 0) {
            client.getCharacter().addExperience(exp);
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 8, String.valueOf(exp)));
        }
        achievementTemplate.award(client.getCharacter());
        client.getCharacter().getInventoryCache().addKamas(achievementTemplate.getKamasReward(1,client.getCharacter().getLevel()),true);

        client.send(new AchievementRewardSuccessMessage(msg.achievementId));
        client.send(new AchievementListMessage(client.getCharacter().getAchievements().getFinishedAchievementsIds(),
                client.getCharacter().getAchievements().getAchievementRewardable()
        ));

    }

}

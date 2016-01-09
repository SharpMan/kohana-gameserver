package koh.game.entities;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import koh.inter.messages.PlayerComingMessage;
import koh.protocol.types.game.choice.CharacterBaseInformations;
import lombok.Getter;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class Account {

    public Account() {

    }

    public Account(PlayerComingMessage message) {
        id = message.accountId;
        nickName = message.nickname;
        right = message.rights;
        secretQuestion = message.secretQuestion;
        secretAnswer = message.secretAnswer;
        lastIP = message.lastAddress;
        last_login = message.lastLogin;
    }

    @Getter
    public int id;
    public String nickName;
    public byte right;
    public String secretQuestion, secretAnswer, lastIP, currentIP;
    public Timestamp last_login;
    public ArrayList<Player> characters;
    public AccountData accountData;
    public Player currentCharacter = null;

    public List<CharacterBaseInformations> toBaseInformations() {
        return characters.stream().map(Player::toBaseInformations).collect(Collectors.toList());
    }
    
    public Player getPlayerInFight(){
        return characters.stream().filter(x -> x.getFighter() != null).findAny().orElse(null);
    }

    public Player getPlayer(int id) {
        return characters.stream().filter(x -> x.getID() == id).findFirst().orElse(null);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void continueClear() {
        try {
            accountData = null;
            id = 0;
            nickName = null;
            right = 0;
            secretQuestion = null;
            secretAnswer = null;
            lastIP = null;
            last_login = null;
            characters.clear();
            characters = null;
            currentCharacter = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

    public void totalClear() {
        if (this.accountData != null && accountData.columsToUpdate != null) {
            if (accountData.columsToUpdate != null) {
                DAO.getAccountDatas().save(this.accountData,this);
            } else {
                accountData.totalClear(this);
            }
        } else {
            this.continueClear();
        }
    }

}

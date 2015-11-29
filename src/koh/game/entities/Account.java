package koh.game.entities;

import koh.game.entities.actors.Player;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import koh.game.dao.mysql.AccountDataDAOImpl;
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

    @Getter
    public int ID;
    public String NickName;
    public byte Right;
    public String SecretQuestion, SecretAnswer, LastIP,CurrentIP;
    public Timestamp last_login;
    public ArrayList<Player> Characters;
    public AccountData Data;
    public Player currentCharacter = null;

    public List<CharacterBaseInformations> ToBaseInformations() {
        return Characters.stream().map(x -> x.toBaseInformations()).collect(Collectors.toList());
    }
    
    public Player GetPlayerInFight(){
        return Characters.stream().filter(x -> x.GetFighter() != null).findAny().orElse(null);
    }

    public Player getPlayer(int id) {
        return Characters.stream().filter(x -> x.ID == id).findFirst().get();
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void continueClear() {
        try {
            Data = null;
            ID = 0;
            NickName = null;
            Right = 0;
            SecretQuestion = null;
            SecretAnswer = null;
            LastIP = null;
            last_login = null;
            Characters.clear();
            Characters = null;
            currentCharacter = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

    public void totalClear() {
        if (this.Data != null && Data.ColumsToUpdate != null) {
            if (Data.ColumsToUpdate != null) {
                AccountDataDAOImpl.Update(this.Data,this);
            } else {
                Data.totalClear(this);
            }
        } else {
            this.continueClear();
        }
    }

}

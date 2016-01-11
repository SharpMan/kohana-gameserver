from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from java.lang import StringBuilder
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage

class GetPlayerInfoCommand(PlayerCommand):

    def getDescription(self):
        return "Get player arg1 informations";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacter(args[0]);
        if target is None:
            client.send(ConsoleMessage(0, "The target is missing"));
        else :
            sb = StringBuilder();
            for player in DAO.getPlayers().getByIp(target.getClient().getIP()):
                sb.append("Player ").append(player.getNickName());
                sb.append(" Level ").append(player.getLevel());
                sb.append(" PH ").append(player.getHonor());
                sb.append(" MapPos ").append(player.getCurrentMap().posToString()).append("\n");
            sb.append("IP ").append(target.getClient().getIP());
            sb.append(" Nickname ").append(target.getAccount().nickName);
            client.send(ConsoleMessage(0, sb.toString()));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 1;

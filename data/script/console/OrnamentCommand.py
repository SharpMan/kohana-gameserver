from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage
from org.apache.commons.lang3 import ArrayUtils
from koh.protocol.messages.messages.game.tinsel import OrnamentGainedMessage

class OrnamentCommand(PlayerCommand):

    def getDescription(self):
        return "Set the ornament arg1 to the player arg2";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacter(args[1]);
        if target is None or target.getClient() is None :
            client.send(ConsoleMessage(0, "The target is missing"));
        elif ArrayUtils.contains(client.getCharacter().getOrnaments(), int(args[0])):
            client.send(ConsoleMessage(0, "The target possesses already the ornament"));
        else:
            client.getCharacter().setOrnaments(ArrayUtils.add(client.getCharacter().getOrnaments(), int(args[0])));
            client.send(OrnamentGainedMessage(int(args[0])));
            client.send(ConsoleMessage(0, "The ornament has been added"));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 2;

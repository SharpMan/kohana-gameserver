from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage


class LevelCommand(PlayerCommand):

    def getDescription(self):
        return "Set the level arg1 to the player arg2";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacter(args[1]);
        if target is None or target.getClient() is None :
            client.send(ConsoleMessage(0, "The target is missing"));
        else:
            target.addExperience((DAO.getExps().getPlayerMinExp(int(args[0])) + 1) - target.getExperience());
            client.send(ConsoleMessage(0, "The level has been upped"));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 2;

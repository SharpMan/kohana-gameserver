from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.protocol.messages.authorized import ConsoleMessage
from koh.game import Main
from koh.inter.messages import AccountTookAwayMessage
from koh.game.dao import DAO


class UnBanCommand(PlayerCommand):

    def getDescription(self):
        return "Unban the player arg1";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacterOwner(args[0]);

        if target == -1 :
            client.send(ConsoleMessage(0, "The target is missing"));
        else:
            Main.getInterClient().send(AccountTookAwayMessage(target));
            client.send(ConsoleMessage(0, "The target has been unsuspended"));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 4;

    def argsNeeded(self):
        return 1;

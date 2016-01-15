from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage
from java.lang import System
from java.time import Instant
from java.time.temporal import ChronoUnit
from koh.game import Main
from koh.inter.messages import PlayerAddressSuspendedMessage


class IPBanCommand(PlayerCommand):

    def getDescription(self):
        return "Ban the player's ip arg1 from arg2 hours";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacter(args[0]);
        if target is None or target.getClient() is None :
            client.send(ConsoleMessage(0, "The target is missing"));
        else:
            time = Instant.now().plus(int(args[1]), ChronoUnit.HOURS).toEpochMilli();
            Main.interClient().send(PlayerAddressSuspendedMessage(time,target.getAccount().getId(),target.getClient().getIP()));

            for player in DAO.getPlayers().getByIp(target.getClient().getIP()):
                player.getClient().timeOut();

            client.send(ConsoleMessage(0, "The target's ip has been banned for "+ args[1] +" hours"));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 3;

    def argsNeeded(self):
        return 2;

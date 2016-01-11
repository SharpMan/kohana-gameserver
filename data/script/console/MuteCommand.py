from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.protocol.messages.authorized import ConsoleMessage
from java.time import Instant
from java.time.temporal import ChronoUnit
from koh.game.dao import DAO
from koh.game.entities.actors.character import PlayerInst
from koh.protocol.messages.game.basic import TextInformationMessage


class MuteCommand(PlayerCommand):

    def getDescription(self):
        return "Mute the player arg1 for arg2 minutes //for ummute tape 0 minutes";

    def apply(self,client,args):
        target = DAO.getPlayers().getCharacter(args[0]);
        if target is None or target.getClient() is None :
            client.send(ConsoleMessage(0, "The target is missing"));
        else:
            time = Instant.now().plus(int(args[1]), ChronoUnit.MINUTES).toEpochMilli();
            PlayerInst.getPlayerInst(target.getID()).setMutedTime(time);
            target.send(TextInformationMessage(1, 123, str(int(args[1]) * 60 )));
            client.send(ConsoleMessage(0, "The target has been muted for "+ args[1] +" minutes"));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 2;

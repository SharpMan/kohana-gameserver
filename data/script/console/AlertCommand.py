from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game import Main
from koh.protocol.messages.game.moderation import PopupWarningMessage

class AlertCommand(PlayerCommand):

    def getDescription(self):
        return "Alert duration arg1 arg2 message to the Community duration, please don't set more than 0 seconds just in HARSH situations";

    def apply(self,client,args):
        Main.getWorldServer().sendPacket(PopupWarningMessage(int(args[0]), client.getCharacter().getNickName(), args[1]));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 5;

    def argsNeeded(self):
        return 2;

from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum
from koh.game.network import WorldClient
from koh.protocol.messages.authorized import ConsoleMessage
from koh.protocol.client.enums import StatsEnum

class AddStatCommand(PlayerCommand):

    def getDescription(self):
        return "AddStat arg1 count arg2 to me\n For arg1 join effectId her https://gist.github.com/SharpMan/6bf29671681d02860c1d";

    def apply(self,client,args):
        if(client.isGameAction(GameActionTypeEnum.FIGHT)):
             client.send(ConsoleMessage(0, "Action not permitted , you are in fight"));
        else:
             client.getCharacter().getStats().addBase(StatsEnum.valueOf(int(args[0])),int(args[1]));
             client.getCharacter().refreshStats();

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 2;

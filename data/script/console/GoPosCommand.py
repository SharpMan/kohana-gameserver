from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.protocol.messages.authorized import ConsoleMessage
from koh.game.entities.environments import MapPosition
from koh.game.entities.environments import DofusMap

class GoPosCommand(PlayerCommand):

    def getDescription(self):
        return "Telpeort to the player to the pos X arg1 pos Y arg2";

    def apply(self,client,args):
        X = int(args[0]);
        if " " in args[1]:
           Y = int(args[1].split()[0]);
           Z = int(args[1].split()[1]);
        else:
           Y = int(args[1]);
           Z = -1;

        subAreas = DAO.getMaps().getSubAreaOfPos(X, Y);
        if len(subAreas) > 1 and Z == -1:
            client.send(ConsoleMessage(0, "This position contains a lots of subArea so try one of this .."));
            for pos in subAreas:
                client.send(ConsoleMessage(0,  "gopos " + str(X) + " " + str(Y) + " " + str(pos.getSubAreaId()) + " (Or choose teleport " + str(pos.getId()) + " -1 )"));

        else :
            if Z == -1:
                Z = subAreas[0].getSubAreaId();

            map = DAO.getMaps().findMapByPos(X, Y, Z);
            if map is None :
                client.send(ConsoleMessage(0, "Undefined map .."));
                pass;

            client.getCharacter().teleport(map.getId(), map.getAnyCellWalakable().getId());

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 1;

    def argsNeeded(self):
        return 2;

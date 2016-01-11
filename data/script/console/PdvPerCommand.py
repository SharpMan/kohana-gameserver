from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum
from koh.game.controllers import PlayerController
from koh.game.network import WorldClient



class PdvPerCommand(PlayerCommand):

    def getDescription(self):
        return "Set the player Pdv percent to arg1";

    def apply(self,client,args):
        percent = int(args[0]);
        if client.getCharacter().getFight() != None or percent > 100 :
            pass;

        client.getCharacter().setLife(int((client.getCharacter().getMaxLife() * percent) / 100));
        client.getCharacter().refreshStats();

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 1;

    def argsNeeded(self):
        return 1;

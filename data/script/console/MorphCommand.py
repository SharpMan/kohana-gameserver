from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum
from koh.game.controllers import PlayerController
from koh.game.network import WorldClient



class MorphCommand(PlayerCommand):

    def getDescription(self):
        return "Set player bones to arg1";

    def apply(self,client,args):
        client.getCharacter().getEntityLook().bonesId = int(args[0]);
        client.getCharacter().refreshEntitie();

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 1;

    def argsNeeded(self):
        return 1;

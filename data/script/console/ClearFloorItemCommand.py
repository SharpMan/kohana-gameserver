from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient



class ClearFloorItemCommand(PlayerCommand):

    def getDescription(self):
        return "Clear all items deposed on the floor";

    def apply(self,client,args):
        client.getCharacter().getCurrentMap().clearDroppedItems();

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 0;

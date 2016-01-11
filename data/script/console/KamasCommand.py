from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient



class KamasCommand(PlayerCommand):

    def getDescription(self):
        return "Add arg1 kamas to you";

    def apply(self,client,args):
        client.getCharacter().getInventoryCache().addKamas(int(args[0]));

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 4;

    def argsNeeded(self):
        return 1;

from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.fights import Fight


class StartFightCommand(PlayerCommand):

    def getDescription(self):
        return "start the fucking fight right now";

    def apply(self,client,args):
        client.getCharacter().getFight().startFight()

    def can(self,client):
        return True

    def roleRestrained(self):
        return 2

    def argsNeeded(self):
        return 0

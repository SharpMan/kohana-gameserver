from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient



class SpellPointCommand(PlayerCommand):

    def getDescription(self):
        return "Add arg1 spellponints to me";

    def apply(self,client,args):
        client.getCharacter().setSpellPoints(client.getCharacter().getSpellPoints()  + int(args[0]));
        client.getCharacter().refreshStats()

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 1;

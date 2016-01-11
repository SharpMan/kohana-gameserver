from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient



class LearnSpellCommand(PlayerCommand):

    def getDescription(self):
        return "Add spell arg1 to you";

    def apply(self,client,args):
        client.getCharacter().getMySpells().addSpell(int(args[0]),  1, client.getCharacter().getMySpells().getFreeSlot(), client);

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 2;

    def argsNeeded(self):
        return 1;

from koh.game.entities.command import PlayerCommand
from koh.game.controllers import PlayerController;
from koh.game.network import WorldClient;
from koh.protocol.client.enums import AlignmentSideEnum;



class AngeCommand(PlayerCommand):

    def getDescription(self):
        return "Choisit l'alignement bontarien";

    def apply(self,client,args):
        client.getCharacter().changeAlignementSide(AlignmentSideEnum.ALIGNMENT_ANGEL);

    def can(self,client):
        return True;

    def roleRestrained(self):
        return 0;

    def argsNeeded(self):
        return 0;

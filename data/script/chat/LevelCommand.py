 # coding=UTF-8
from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum
from koh.game.network import WorldClient
from koh.game.dao import DAO
from koh.game.controllers import PlayerController
from koh.game.network import WorldClient
from koh.game.actions import GameAction

class LevelCommand(PlayerCommand):

    def getDescription(self):
        return None

    def apply(self,client,args):
        level = int(args[0]);
        if level < 2 or level > 200 :
            layerController.sendServerMessage(client, "Niveau invalide")
        else:
            client.getCharacter().addExperience((DAO.getExps().getPlayerMinExp(int(args[0])) + 1) - client.getCharacter().getExperience())
            action = client.getGameAction(GameActionTypeEnum.TUTORIAL)
            action.keepGoing()

    def can(self,client):
        if not client.isGameAction(GameActionTypeEnum.TUTORIAL):
            PlayerController.sendServerMessage(client, "Action impossible : Vous avez déjà choisis votre niveau")
            return False
        else:
            return True

    def argsNeeded(self):
        return 1

    def roleRestrained(self):
        return 0

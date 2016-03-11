from koh.game.entities.command import PlayerCommand
from koh.game.network import WorldClient
from koh.game.entities.actors.pnj import NpcTemplate
from koh.game.dao import DAO
from koh.protocol.messages.game.context.roleplay import GameRolePlayShowActorMessage

class ProjectNpcCommand(PlayerCommand):

    def getDescription(self):
        return "Project npc arg1 on your cell arg2 dir arg3"

    def apply(self,client,args):
        id = int(args[0])
        cell = int(args[1])
        dir = int(args[2])

        npc = DAO.getNpcs().findTemplate(id)

        client.send(GameRolePlayShowActorMessage(npc.getProjection(cell,dir)))

    def can(self,client):
        return True

    def roleRestrained(self):
        return 4

    def argsNeeded(self):
        return 3

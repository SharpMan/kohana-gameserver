#
#		IA Mind of "Tonneau"
#		@author: alleos13
#		@date: 08/06/2013
from koh.game.entities.mob import IAMind
from koh.game.fights.AI import AIProcessor
from koh.protocol.client.enums import FightStateEnum

class IAMind11(IAMind):

    def play(self,IA):
        hasState = IA.fighter.getStates().hasState(FightStateEnum.CARRIED)
        if IA.getUsedNeurons() == 1:
            IA.initCells()
            if hasState:
                IA.heal()
            else:
                IA.repels()
        elif IA.getUsedNeurons() == 2:
            IA.initCells()
            if hasState:
                IA.heal()
            else:
                IA.repels()
        else:
            IA.stop()

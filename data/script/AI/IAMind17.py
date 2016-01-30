#
#		Tofu IA
#		@author: Melancholia
#		@date: 24/01/2013
from koh.game.entities.mob import IAMind
from koh.game.fights.AI import AIProcessor

class IAMind17(IAMind):

    def play(self,IA):
        if IA.getUsedNeurons() == 1:
            IA.initCells()
            IA.moveToEnnemyByCell(1,1)
        elif IA.getUsedNeurons() == 2:
            IA.initCells()
            IA.selfAction()
        elif IA.getUsedNeurons() == 3:
            IA.initCells()
            IA.moveFar()
        else:
            IA.stop()

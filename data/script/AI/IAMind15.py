#
#		IA Mind of "Cadran"
#		@author: alleos13
#		@date: 08/06/2013
from koh.game.entities.mob import IAMind
from koh.game.fights.AI import AIProcessor

class IAMind15(IAMind):

    def play(self,IA):
        if IA.getUsedNeurons() == 1:
            IA.initCells()
            IA.buffMe()
        elif IA.getUsedNeurons() == 2:
            IA.initCells()
            IA.subbuff()
        elif IA.getUsedNeurons() == 3:
            IA.initCells()
            IA.subbuff()
        else:
            IA.stop()

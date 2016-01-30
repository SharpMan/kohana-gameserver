#
#		Top IA Mind
#		@author: alleos13
#		@date: 08/06/2013
from koh.game.entities.mob import IAMind
from koh.game.fights.AI import AIProcessor

class IAMind1(IAMind):

    def play(self,IA):
        if IA.getUsedNeurons() == 1:
            IA.initCells()
            IA.selfAction()
            if IA.getNeuron().isAttacked() is False :
                IA.moveToEnnemy()
        elif IA.getUsedNeurons() == 2:
            IA.initCells()
            IA.selfAction()
            if IA.getNeuron().isAttacked() is False : #in case if he is trapped by a trap the first time
                IA.moveToEnnemy()
        elif IA.getUsedNeurons() == 3:
            IA.initCells()
            IA.selfAction()
        elif IA.getUsedNeurons() == 4:
            IA.initCells()
            IA.selfAction()
        else:
            IA.stop()

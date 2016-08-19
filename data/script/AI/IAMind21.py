#
#		DRAGONET ROUGE IA Mind
#		@author: alleos13
#		@date: 08/06/2013
from koh.game.entities.mob import IAMind
from koh.game.fights.AI import AIProcessor

class IAMind21(IAMind):

    def play(self,IA):
        if IA.getUsedNeurons() == 1:
            IA.initCells()
            IA.debuffEnnemy()
        elif IA.getUsedNeurons() == 2:
            IA.initCells()
            IA.debuffEnnemy()
        elif IA.getUsedNeurons() == 3:
            IA.initCells()
            IA.attack()
            if IA.getNeuron().isAttacked() is False :
                IA.moveToEnnemy()
        elif IA.getUsedNeurons() == 4:
            IA.initCells()
            IA.attack()
            if IA.getNeuron().isAttacked() is False :
                IA.moveToEnnemy()

        else:
            IA.stop()

#
#		Passive IA Mind
#		@author: alleos13
#		@date: 08/06/2013
from koh.game.entities.mob import IAMind
from koh.game.fights.AI import AIProcessor

class IAMind0(IAMind):

    def play(self,IA):
            IA.Wait(250)
            IA.stop()

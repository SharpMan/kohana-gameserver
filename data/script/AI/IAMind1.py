from koh.game.entities.mob import IAMind
from koh.game.fights.AI import AIProcessor

class IAMind1(IAMind):

    def play(self,IA):
        if IA.getUsedNeurons() == 1:
            IA.initCells();
            IA.attack();
        elif IA.getUsedNeurons() == 2:
            IA.initCells();
            IA.attack();
        elif IA.getUsedNeurons() == 3:
            IA.initCells();
            IA.attack();
        else:
            IA.stop();

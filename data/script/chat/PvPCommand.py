#!/usr/bin/python
# -*- coding: iso-8859-1 -*-
from koh.game.entities.command import PlayerCommand
from koh.game.actions import GameActionTypeEnum;
from koh.game.controllers import PlayerController;
from koh.game.network import WorldClient;



class PvPCommand(PlayerCommand):

    def getDescription(self):
        return "Téleporte à la map PvP";

    def apply(self,client,args):
        client.getCharacter().teleport(88212759,293);

    def can(self,client):
        if not client.canGameAction(GameActionTypeEnum.MAP_MOVEMENT):
            PlayerController.sendServerMessage(client, "Action impossible : Vous êtes occupé");
            return False;
        else:
            return True;

    def roleRestrained(self):
        return 0;

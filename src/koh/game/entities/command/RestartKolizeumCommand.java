package koh.game.entities.command;

import koh.game.entities.kolissium.KolizeumExecutor;
import koh.game.fights.Fight;
import koh.game.network.WorldClient;
import koh.game.network.WorldServer;
import koh.protocol.messages.authorized.ConsoleMessage;

/**
 * Created by Melancholia on 6/16/16.
 */
public class RestartKolizeumCommand implements PlayerCommand {

    private int i = 0;

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        if(client.getCharacter().getNickName().startsWith("Melan")){
            //WorldServer.getKoli().clear();
            Fight.POSIBLE = !Fight.POSIBLE;
            return;
        }
        if(i == 3){
            client.send(new ConsoleMessage((byte) 0, "Koliseo can not longer restart"));
            return;
        }
        WorldServer.getKoli().shutdown();
        WorldServer.setKoli(new KolizeumExecutor());
        client.send(new ConsoleMessage((byte) 0, "Koliseo Task rescheduled"));
        i++;
    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 3;
    }

    @Override
    public int argsNeeded() {
        return 0;
    }
}

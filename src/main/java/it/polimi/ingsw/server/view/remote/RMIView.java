package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.messages.Message;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * RMI based implementation of the server-side View
 */
public class RMIView extends View {

    @Override
    public PowerupTile chooseSpawnpoint(List<PowerupTile> powerups) {
        return null;
    }

    @Override
    public PlayerInfo getPlayerInfo() {
        return null;
    }

    @Override
    public BoardFactory.Preset getChosenPreset() {
        return null;
    }

    @Override
    public int getChosenSkulls() {
        return 0;
    }

    @Override
    public Match.Mode getChosenMode() {
        return null;
    }

    public BlockingQueue<Message> getOutputMessageQueue() {
        return outputMessageQueue;
    }

    public InputMessageQueue getInputMessageQueue() {
        return inputMessageQueue;
    }
}

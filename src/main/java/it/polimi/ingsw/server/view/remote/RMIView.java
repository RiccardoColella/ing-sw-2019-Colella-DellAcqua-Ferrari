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
import java.util.concurrent.TimeUnit;

/**
 * RMI based implementation of the server-side View
 *
 * @author Carlo Dell'Acqua
 */
public class RMIView extends View {

    private RMIMessageProxy messageProxy = null;

    public RMIView(int answerTimeout, TimeUnit answerTimeoutUnit) {
        super(answerTimeout, answerTimeoutUnit);
    }

    public BlockingQueue<Message> getOutputMessageQueue() {
        return outputMessageQueue;
    }

    public InputMessageQueue getInputMessageQueue() {
        return inputMessageQueue;
    }

    public void setMessageProxy(RMIMessageProxy messageProxy) {
        this.messageProxy = messageProxy;
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (messageProxy != null) {
            messageProxy.close();
        }
    }
}

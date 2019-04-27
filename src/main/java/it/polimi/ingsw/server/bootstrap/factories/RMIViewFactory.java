package it.polimi.ingsw.server.bootstrap.factories;

import it.polimi.ingsw.server.view.remote.RMIView;

import java.util.concurrent.TimeUnit;

/**
 * Creates an RMIView with the initial configuration
 */
public class RMIViewFactory {

    private RMIViewFactory() { }

    /**
     * The answer timeout
     */
    private static int answerTimeoutMilliseconds = 1000;

    /**
     * Initializes this factory
     *
     * @param answerTimeoutMilliseconds the time to wait before considering the view disconnected
     */
    public static void initialize(int answerTimeoutMilliseconds) {
        RMIViewFactory.answerTimeoutMilliseconds = answerTimeoutMilliseconds;
    }

    /**
     * Creates an RMIView
     *
     * @return an RMIView
     */
    public static RMIView createRMIView() {
        return new RMIView(answerTimeoutMilliseconds, TimeUnit.MILLISECONDS);
    }
}

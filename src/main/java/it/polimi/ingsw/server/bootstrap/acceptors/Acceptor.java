package it.polimi.ingsw.server.bootstrap.acceptors;

import it.polimi.ingsw.server.view.View;

import java.util.concurrent.Callable;

/**
 * Interface of an acceptors, an objects which will implement the callable API and return a View
 */
public interface Acceptor extends Callable<View> {
}

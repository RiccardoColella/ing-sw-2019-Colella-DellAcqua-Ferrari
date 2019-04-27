package it.polimi.ingsw.server;

import it.polimi.ingsw.server.bootstrap.WaitingRoom;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.util.Arrays;

public class MainDebug {

    public static void main(String[] args) throws Exception {

        // Testing
        WaitingRoom waitingRoom = new WaitingRoom(9000, 9090, 10000);
        waitingRoom.collectAsync();

        View view = waitingRoom.pop().orElse(null);
        while (view == null) {
            Thread.sleep(1000);
            view = waitingRoom.pop().orElse(null);
        }


        System.out.println(
                view.select(
                        "Cosa?",
                        Arrays.asList(Direction.NORTH, Direction.EAST),
                        ClientApi.DIRECTION_QUESTION
                )
        );

        try {
            waitingRoom.close();
            view.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

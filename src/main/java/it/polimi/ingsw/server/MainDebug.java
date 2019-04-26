package it.polimi.ingsw.server;

import it.polimi.ingsw.server.bootstrap.WaitingRoom;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.io.IOException;
import java.util.Arrays;

public class MainDebug {

    public static void main(String[] args) throws IOException, InterruptedException {

        // Testing
        WaitingRoom waitingRoom = new WaitingRoom(9000, 9090, 10000);
        waitingRoom.collectAsync();


        View view = waitingRoom.pop().orElse(null);
        while (view == null) {
            Thread.sleep(1000);
            view = waitingRoom.pop().orElse(null);
        }


        Direction direction = view.select("Scegli", Arrays.asList(Direction.EAST, Direction.NORTH), ClientApi.DIRECTION_QUESTION);


        System.out.println(direction);
    }
}

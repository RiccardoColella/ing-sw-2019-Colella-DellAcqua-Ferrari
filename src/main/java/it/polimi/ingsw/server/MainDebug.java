package it.polimi.ingsw.server;

import it.polimi.ingsw.server.bootstrap.WaitingRoom;

public class MainDebug {

    public static void main(String[] args) throws Exception {

        // Testing
        WaitingRoom waitingRoom = new WaitingRoom(9000, 9090, 10000);
        waitingRoom.collectAsync();


        try {
            waitingRoom.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

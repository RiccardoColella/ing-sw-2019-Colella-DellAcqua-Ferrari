package it.polimi.ingsw.utils.io;

import it.polimi.ingsw.server.controller.exceptions.ViewDisconnectedException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UncheckedInputStream {

    private DataInputStream dataStream;

    public UncheckedInputStream(InputStream stream) {
        dataStream = new DataInputStream(stream);
    }

    public int readInt() {
        try {
            return dataStream.readInt();
        } catch (IOException e) {
            throw new ViewDisconnectedException("Unable to send data");
        }
    }

    public byte[] readBytes(int length) {
        try {
            byte[] received = new byte[length];
            if (dataStream.read(received) == length) {
                return received;
            } else {
                throw new ViewDisconnectedException("Invalid input");
            }
        } catch (IOException e) {
            throw new ViewDisconnectedException("Unable to send data");
        }
    }
}

package it.polimi.ingsw.utils.io;

import it.polimi.ingsw.server.controller.exceptions.ViewDisconnectedException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UncheckedOutputStream {

    private DataOutputStream dataStream;

    public UncheckedOutputStream(OutputStream stream) {
        dataStream = new DataOutputStream(stream);
    }

    public void writeInt(int value) {
        try {
            dataStream.writeInt(value);
        } catch (IOException e) {
            throw new ViewDisconnectedException("Unable to send data");
        }
    }

    public void writeBytes(byte[] bytes) {
        try {
            dataStream.write(bytes);
        } catch (IOException e) {
            throw new ViewDisconnectedException("Unable to send data");
        }
    }
}

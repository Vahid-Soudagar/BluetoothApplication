package com.example.bluetoothapplication.bluetooth.reader;

import java.io.IOException;
import java.io.InputStream;

public abstract class SocketReader {
    protected InputStream inputStream;

    public SocketReader(InputStream inputStream){
        this.inputStream = inputStream;
    }


    public byte[] read() throws IOException {
        return null;
    }
}

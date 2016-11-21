package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by Owner-PC on 11/7/2016.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handle;
    private final int MESSAGE_READ = 0;
    private int getX;

    public ConnectedThread(BluetoothSocket socket, Handler handle) {
        mmSocket = socket;
        this.handle = handle;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream, possibly shorten this
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                int x = ((buffer[0] & 0xFF << 24) | (buffer[1] & 0xFF << 16) |
                (buffer[2] & 0xFF << 8) | (buffer[3] & 0xFF));
                System.out.println(x + "\n");
                // Send the obtained bytes to the UI activity
                //handle.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        //.sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

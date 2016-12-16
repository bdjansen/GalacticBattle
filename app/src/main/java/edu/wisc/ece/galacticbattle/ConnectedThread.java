package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Owner-PC on 11/7/2016.
 * This thread is used to continue the bluetooth connection in the game in order to send the
 * ship location and bullet shooting information.  It cancels once the game is over.
 */
public class ConnectedThread extends Thread {
    // Socket and streams to send data over
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    // Handler in GameActivity to decide what to do once we get data
    private final Handler handle;
    // Channel to handle data over
    private final int MESSAGE_READ = 0;

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
        } catch (IOException e) {
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream, possibly shorten this
        int bytes; // amount of bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI activity
                    handle.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Owner-PC on 11/7/2016.
 */

public class ServerThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private BluetoothAdapter mAdapter;
    private BluetoothSocket socket;
    private Context appContext;

    public ServerThread(BluetoothAdapter adapter, Context appContext) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        socket = null;

        this.appContext = appContext;

        mAdapter = adapter;

        // Generate UUID
        UUID uuid = UUID.fromString("b2fb123e-4742-430f-99a5-7d0d96ff62ae");

        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mAdapter.listenUsingRfcommWithServiceRecord("Galactic Battle", uuid);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                //Do work to manage the connection (in a separate thread)
                manageConnectedSocket();
            }
        }
    }

    private void manageConnectedSocket()
    {
        CharSequence text = "You have connected to another device.";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(appContext, text, duration);
        toast.show();
    }

    public BluetoothSocket getSocket()
    {
        return socket;
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}

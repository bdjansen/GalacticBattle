package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import android.os.Handler;

/**
 * Created by Owner-PC on 11/7/2016.
 */

public class ServerThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothAdapter mAdapter;
    private BluetoothSocket socket;
    private final Handler connectionHandler;

    // Constructor
    public ServerThread(Handler handle) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        socket = null;
        connectionHandler = handle;

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        // Generate UUID
        UUID uuid = UUID.fromString("b2fb123e-4742-430f-99a5-7d0d96ff62ae");

        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord("Galactic Battle", uuid);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        mmServerSocket = tmp;
    }

    // Get the connection from the client to run
    public void run() {
        socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                System.out.println("Cannot Connect\n");
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                //Do work to manage the connection (in a separate thread)
                manageConnectedSocket();
                connectionHandler.obtainMessage(0,0,-1,null).sendToTarget();
            }
        }
    }

    private void manageConnectedSocket()
    {
        System.out.println("Server connected");
    }

    public BluetoothSocket getSocket()
    {
        return socket;
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

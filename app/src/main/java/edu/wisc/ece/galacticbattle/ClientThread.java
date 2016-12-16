package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;
import android.os.Handler;

/**
 * Created by Owner-PC on 11/7/2016.
 * This class is used by the user who requests to play a game.  This sends the request to the
 * other player to start the bluetooth connection.
 */
public class ClientThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothAdapter mAdapter;
    private final Handler connectionHandler;

    public ClientThread(BluetoothDevice device, Handler handle) {
        BluetoothSocket tmp = null;
        connectionHandler = handle;

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        // Generate uuid
        UUID uuid = UUID.fromString("b2fb123e-4742-430f-99a5-7d0d96ff62ae");

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // Create the socket from our uuid
            tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        mAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            System.out.println(connectException.getMessage());
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                System.out.println(closeException.getMessage());
            }
            return;
        }

        // We connected!
        // Send back to the find players screen that we have connected to the other player
        manageConnectedSocket();
        connectionHandler.obtainMessage(0,0,-1,null).sendToTarget();
    }

    public BluetoothSocket getSocket()
    {
        return mmSocket;
    }

    private void manageConnectedSocket()
    {
        System.out.println("You connected as a client.");
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

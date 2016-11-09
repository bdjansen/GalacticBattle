package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Owner-PC on 11/7/2016.
 */
public class ClientThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter mAdapter;
    private Context appContext;

    public ClientThread(BluetoothDevice device, BluetoothAdapter adapter, Context appContext) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;

        this.appContext = appContext;

        mAdapter = adapter;

        // Generate uuid
        UUID uuid = UUID.fromString("b2fb123e-4742-430f-99a5-7d0d96ff62ae");

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) { }
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
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket();
    }

    public BluetoothSocket getSocket()
    {
        return mmSocket;
    }

    private void manageConnectedSocket()
    {
        CharSequence text = "You have connected to another device.";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(appContext, text, duration);
        toast.show();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

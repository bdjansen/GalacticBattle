package edu.wisc.ece.galacticbattle;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

/**
 * Created by Blake on 11/9/2016.
 */
public class GalacticBattleApp extends Application {
    private BluetoothSocket mmSocket;

    public void setSocket(BluetoothSocket socket) {
        mmSocket = socket;
    }

    public BluetoothSocket getSocket() {
        return mmSocket;
    }
}

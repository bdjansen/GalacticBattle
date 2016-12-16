package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.ListActivity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;


/**
 * Created by Blake on 10/24/2016.
 */
public class FindPlayersActivity extends ListActivity {
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> FOLKS = new ArrayList<String>();
    private ClientThread client;
    private ServerThread server;

    // This message gets returned from the server to say the game is starting
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Context context = getApplicationContext();
            CharSequence text = "A player has linked up with you.  Game Starting in 2 seconds!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            receiveData.postDelayed(startGame, 2000);
        }
    };

    // This message gets returned from the client to say the game is starting
    private final Handler clientHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Context context = getApplicationContext();
            CharSequence text = "You linked up.  Game Starting in 2 seconds!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            receiveData.postDelayed(startGame, 2000);
        }
    };


    private final Handler receiveData = new Handler();
    private final Runnable startGame = new Runnable() {
        @Override
        public void run() {
            Go(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_players_activity);


        // Define an adapter to hold the blutooth devices
        mAdapter = new ArrayAdapter<String>(this,
                R.layout.activity_list_view_layout, FOLKS);

        // Assign the adapter to ListView
        setListAdapter(mAdapter);


        // Get the bluetooth adapter
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        // Make sure bluetooth is enabled on this device
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

        // Display all previously paired devices on the ListView
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and index to an array adapter to show in a ListView
                mAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        // Start both threads as a server until a device chooses a server to request
        server = new ServerThread(clientHandler);
        server.start();

        // Define the listener interface
       AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                // Get the BluetoothDevice object
                BluetoothDevice pickedDevice = mBluetoothAdapter.getRemoteDevice(address);

                // Make this device to client and send the request to the server
                client = new ClientThread(pickedDevice, mHandler);
                client.start();


            }
        };

        // Get the ListView and wire the listener
        ListView listView = getListView();
        listView.setOnItemClickListener(mListener);
    }

    public void Go(View v) {
        // Set the bluetooth socket for the game activity to use
        GalacticBattleApp myApp = (GalacticBattleApp)getApplicationContext();
        if (client != null && client.getSocket() != null)
        {
            myApp.setSocket(client.getSocket());
        }
        else if (server != null && server.getSocket() != null)
        {
            myApp.setSocket(server.getSocket());
        }


        // Go to the game activity
        Intent mIntent = new Intent(FindPlayersActivity.this,
                GameActivity.class);
        startActivity(mIntent);
    }

    // We don't want to do anything during a backpress in the middle of the game
    @Override
    public void onBackPressed() {
        if (client != null && client.getSocket() != null)
        {
            client.cancel();
        }
        else if (server != null && server.getSocket() != null)
        {
            server.cancel();
        }

        super.onBackPressed();
    }
}
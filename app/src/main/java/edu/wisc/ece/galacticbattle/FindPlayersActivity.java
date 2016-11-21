package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.ListActivity;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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

    private int REQUEST_ENABLE_BT;

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
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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

        final Context context = getApplicationContext();

        server = new ServerThread(context);
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

                client = new ClientThread(pickedDevice, context);
                client.start();
            }
        };

        // Get the ListView and wire the listener
        ListView listView = getListView();
        listView.setOnItemClickListener(mListener);
    }

    public void Go(View v) {

        ConnectedThread connection;

        // Go back to the main activity
        Intent mIntent = new Intent(FindPlayersActivity.this,
                GameActivity.class);

        if (client != null && client.getSocket() != null)
        {
            //connection = new ConnectedThread(client.getSocket(), new Handler());
            //mIntent.putExtra("Connected Thread", connection);
            System.out.println("Client Goes");
        }
        else if (server != null && server.getSocket() != null)
        {
            //connection = new ConnectedThread(server.getSocket(), new Handler());
            //mIntent.putExtra("Connected Thread", connection);\
            System.out.println("Server Goes");
        }
        else {
            System.out.println("No setup created\n");
            return;
        }

        GalacticBattleApp myApp = (GalacticBattleApp)getApplicationContext();
        if (client.getSocket() != null)
        {
            myApp.setSocket(client.getSocket());
        }
        else if (server.getSocket() != null)
        {
            myApp.setSocket(server.getSocket());
        }

        startActivity(mIntent);
    }
}
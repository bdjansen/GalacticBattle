package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.app.ListActivity;
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
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        final Object [] arrayOfDevices = pairedDevices.toArray();

        // If there are paired devices
        if (pairedDevices.size() > 0) {
            int i = 1;

            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and index to an array adapter to show in a ListView
                mAdapter.add(i + " " + device.getName());
                i++;
            }
        }

        // set up both devices as servers
        ServerThread server = new ServerThread(mBluetoothAdapter);
        server.run();

        // Define the listener interface
        AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // get the index of the pushed device
                TextView click = (TextView) view;
                String text = (String) click.getText();
                String [] split = text.split(" ");
                int index = Integer.parseInt(split[0]);

                // get the BluetoothDevice object and set up this device as a client
                BluetoothDevice pickedDevice = (BluetoothDevice) arrayOfDevices[index];
                ClientThread client = new ClientThread(pickedDevice, mBluetoothAdapter);
                client.run();
            }
        };

        // Get the ListView and wire the listener
        ListView listView = getListView();
        listView.setOnItemClickListener(mListener);
    }

    public void Go(View v) {
        // Go back to the main activity
        Intent mIntent = new Intent(FindPlayersActivity.this,
                GameActivity.class);
        startActivity(mIntent);
    }
}
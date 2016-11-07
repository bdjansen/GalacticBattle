package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.ListActivity;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;
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

        // Define a new adapter based off of our loaded in names
        mAdapter = new ArrayAdapter<String>(this,
                R.layout.activity_list_view_layout, FOLKS);

        // Assign the adapter to ListView
        setListAdapter(mAdapter);

        // Get the bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        // Make sure bluetooth is enabled on this device
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mAdapter.add(device.getName());
            }
        }



    }

    public void Go(View v) {
        // Go back to the main activity
        Intent mIntent = new Intent(FindPlayersActivity.this,
                GameActivity.class);
        startActivity(mIntent);
    }
}

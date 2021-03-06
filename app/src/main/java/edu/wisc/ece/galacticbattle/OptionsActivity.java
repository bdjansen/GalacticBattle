package edu.wisc.ece.galacticbattle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;

/**
 * Created by Blake on 10/17/2016.
 * This class gives the user the options to both set the color of their ship, and the sensitivity
 * for movement when they tilt their phones. It uses UserPreferences to store the data.
 */

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        this.loadUserData();

    }

    // Get their data for the items so that it loads in their current settings for them to change
    private void loadUserData() {
        // Create the shared preferences variable so we can load in the data
        String mKey = getString(R.string.preference_name);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);
        int shipColor;
        int shipSpeed;

        // Load the string of all the names and then split them by the correct character
        mKey = getString(R.string.preference_key_profile_colors);
        shipColor = mPrefs.getInt(mKey, 1);

        mKey = getString(R.string.preference_key_profile_speed);
        shipSpeed = mPrefs.getInt(mKey, 50);

        switch (shipColor){
            case 1: ((RadioButton)findViewById(R.id.shipColor1)).setChecked(true);
                break;
            case 2: ((RadioButton)findViewById(R.id.shipColor2)).setChecked(true);
                break;
            case 3: ((RadioButton)findViewById(R.id.shipColor3)).setChecked(true);
                break;
        }

        // We use a slider for them to choose how their speed goes
        SeekBar speedSlider = (SeekBar)findViewById(R.id.gameSpeedSlider);
        speedSlider.setMax(100);
        speedSlider.setProgress(shipSpeed);
    }

    // If they click save, we want to write out these changes to use for later
    public void saveUserData(View v) {
        String mKey = getString(R.string.preference_name);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);

        //Create our editor and clear it to put the new data into preferences
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.clear();

        mKey = getString(R.string.preference_key_profile_colors);
        if(((RadioButton)findViewById(R.id.shipColor1)).isChecked()) mEditor.putInt(mKey, 1);
        if(((RadioButton)findViewById(R.id.shipColor2)).isChecked()) mEditor.putInt(mKey, 2);
        if(((RadioButton)findViewById(R.id.shipColor3)).isChecked()) mEditor.putInt(mKey, 3);

        mKey = getString(R.string.preference_key_profile_speed);
        mEditor.putInt(mKey,((SeekBar)findViewById(R.id.gameSpeedSlider)).getProgress());

        // Officially commit the changes to the shared preferences
        mEditor.commit();

        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }

    // If they hit cancel, we don't want to keep any of our changes
    public void cancelChanges(View v){
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }

}

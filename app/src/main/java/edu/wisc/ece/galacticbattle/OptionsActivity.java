package edu.wisc.ece.galacticbattle;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by Blake on 10/17/2016.
 */

public class OptionsActivity extends AppCompatActivity {
    private boolean changedSpeed = false;
    private boolean changedColors = false;
    private boolean clearedStats = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (java.lang.NullPointerException e) {

        }
    }

    private void loadUserData() {
        // Create the shared preferences variable so we can load in the data
        String mKey = getString(R.string.preference_name);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);

        // Load the string of all the names and then split them by the correct character
        mKey = getString(R.string.preference_key_profile_colors);
        //TODO: Put in loading colors

        mKey = getString(R.string.preference_key_profile_speed);
        //TODO: Put in loading speed
    }

    private void saveUserData() {
        String mKey = getString(R.string.preference_name);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);

        //Create our editor and clear it to put the new data into preferences
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.clear();

        mKey = getString(R.string.preference_key_profile_colors);
        //TODO: Put in saving colors

        mKey = getString(R.string.preference_key_profile_speed);
        //TODO: Put in saving speed

        // Officially commit the changes to the shared preferences
        mEditor.commit();
    }
}

package edu.wisc.ece.galacticbattle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.View;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (java.lang.NullPointerException e) {

        }
    }

    public void campaign(View v) {
        // Go to the campaign mode
        Intent mIntent = new Intent(HomeScreen.this,
                CampaignActivity.class);
        startActivity(mIntent);
    }

    public void searchPlayers(View v) {
        // Go to the versus mode
        Intent mIntent = new Intent(HomeScreen.this,
                FindPlayersActivity.class);
        startActivity(mIntent);
    }

    public void openOptions(View v) {
        // Go to the game settings
        Intent mIntent = new Intent(HomeScreen.this,
                OptionsActivity.class);
        startActivity(mIntent);
    }
}

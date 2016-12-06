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

    public void searchPlayers(View v) {
        // Go back to the main activity
        Intent mIntent = new Intent(HomeScreen.this,
                FindPlayersActivity.class);
        startActivity(mIntent);
        finish();
    }

    public void openOptions(View v) {
        // Go back to the main activity
        Intent mIntent = new Intent(HomeScreen.this,
                OptionsActivity.class);
        startActivity(mIntent);
        finish();
    }

    public void campaign(View v) {
        // Go back to the main activity
        Intent mIntent = new Intent(HomeScreen.this,
                CampaignActivity.class);
        startActivity(mIntent);
        finish();
    }
}

package edu.wisc.ece.galacticbattle;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Blake on 10/24/2016.
 */
public class EndScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_screen);

        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (java.lang.NullPointerException e) {

        }

        Intent intent = getIntent();
        String message = intent.getStringExtra(GameActivity.EXTRA_OUTCOME);
        String endTitle = "YOU " + message;

        TextView label = (TextView) findViewById(R.id.endOutcome);
        label.setText(endTitle);
    }

    //@Override
   // public void onBackPressed() {
    //    goToHome();
   // }

    public void homeScreen(View v) {
        /**GalacticBattleApp myApp = (GalacticBattleApp) getApplicationContext();
        try {
            myApp.getSocket().close();
        }
        catch (IOException e)
        {
            System.out.println("IO error");
        }
         */
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }

    public void findPlayers(View v) {
       /** GalacticBattleApp myApp = (GalacticBattleApp) getApplicationContext();
        try {
            myApp.getSocket().close();
        }
        catch (IOException e)
        {
            System.out.println("IO error");
        }
        */
        Intent intent = new Intent(this, FindPlayersActivity.class);
        startActivity(intent);
    }

    public void playAgain(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}

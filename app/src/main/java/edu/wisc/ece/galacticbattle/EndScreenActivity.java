package edu.wisc.ece.galacticbattle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;

/**
 * Created by Blake on 10/24/2016.
 * This class handles the end screen for our game. Once a user finishes a campaign or versus mode
 * game, they are redirected to this screen which will prompt them to play again or go back to the
 * home screen. When redirected here from the versus mode there is also an option to go back to the
 * find players screen so that they may play again with a different opponent.
 */
public class EndScreenActivity extends AppCompatActivity {

    // This variable holds what game mode was just completed
    private String gameMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_screen);

        // Hide the action bar because we do not need it
        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (java.lang.NullPointerException e) {

        }

        // Get the passed message from the intent and split it so that we can grab which game mode
        // was being played before the game was redirected to this screen, and whether or not this
        // user won or lost said game.
        Intent intent = getIntent();
        String message = intent.getStringExtra(GameActivity.EXTRA_OUTCOME);
        String [] messagePieces = message.split(" ");
        String endTitle = "YOU " + messagePieces[0];
        gameMode = messagePieces[1];

        // If the game mode was campaign, we hide the go back to find players button and adjust the
        // button weights accordingly.
        if (gameMode.equals("campaign"))
        {
            Button findPlayers = (Button) findViewById(R.id.findPlayers);
            findPlayers.setVisibility(View.GONE);
            LinearLayout layout = (LinearLayout)findViewById(R.id.endScreen);
            layout.setWeightSum(6f);
        }

        // Show if this user won or lost
        TextView label = (TextView) findViewById(R.id.endOutcome);
        label.setText(endTitle);
    }

    @Override
    public void onBackPressed() {
    }

    // This method is called if the user presses the go back to home screen button
    public void homeScreen(View v) {

        // If we came from the versus game mode, we must close the bluetooth socket
        if (gameMode.equals("versus")) {
            GalacticBattleApp myApp = (GalacticBattleApp) getApplicationContext();
            try {
                myApp.getSocket().close();
            } catch (IOException e) {
                System.out.println("IO error");
            }
        }

        // Start the intent to go back to the home screen
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }

    // This method is called if the user presses the go back to find players button
    public void findPlayers(View v) {

        // Since this button can only be pressed if we came from the versus game mode, we must close
        // the bluetooth socket
        GalacticBattleApp myApp = (GalacticBattleApp) getApplicationContext();
        try {
            myApp.getSocket().close();
        }
        catch (IOException e)
        {
            System.out.println("IO error");
        }

        // Start the intent to go back to the find players screen
        Intent intent = new Intent(this, FindPlayersActivity.class);
        startActivity(intent);
    }

    // This method is called if the user presses the play again button
    public void playAgain(View v) {

        // If we came from the campaign mode, we want the intent to route back to the campaign
        // activity otherwise it should route back to the versus activity.
        Intent intent;
        if (gameMode.equals("campaign"))
        {
            intent = new Intent(this, CampaignActivity.class);
        }
        else
        {
            intent = new Intent(this, GameActivity.class);

        }
        startActivity(intent);
    }
}

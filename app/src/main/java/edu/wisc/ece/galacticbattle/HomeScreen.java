package edu.wisc.ece.galacticbattle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
    }

    public void searchPlayers(View view) {
        // Go back to the main activity
        Intent mIntent = new Intent(HomeScreen.this,
                GameActivity.class);
        startActivity(mIntent);
    }

    public void openOptions(View view) {
        // Go back to the main activity
        Intent mIntent = new Intent(HomeScreen.this,
                OptionsActivity.class);
        startActivity(mIntent);
    }
}

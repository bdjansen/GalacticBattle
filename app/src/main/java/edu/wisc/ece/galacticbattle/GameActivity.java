package edu.wisc.ece.galacticbattle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by Blake on 10/17/2016.
 */

public class GameActivity extends AppCompatActivity {
    private Spaceship myShip = new Spaceship(0, 100);
    private Spaceship enemyShip = new Spaceship(0, -100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
    }
}

package edu.wisc.ece.galacticbattle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Blake on 10/17/2016.
 */

public class GameActivity extends AppCompatActivity {
    private Spaceship myShip = new Spaceship(0, 100);
    private Spaceship enemyShip = new Spaceship(0, -100);

    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
    }


    private boolean shipHit(Spaceship ship)
    {
        for (Bullet current : bullets)
        {
            if (ship.isHit(current))
            {
                return true;
            }
        }

        return false;
    }
}

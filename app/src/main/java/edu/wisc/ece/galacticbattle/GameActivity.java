package edu.wisc.ece.galacticbattle;

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Blake on 10/17/2016.
 */

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    private Spaceship myShip = new Spaceship(0, 100);
    private Spaceship enemyShip = new Spaceship(0, -100);

    private View myShipV;
    private View enemyShipV;

    private SensorManager sensorManager;

    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        myShipV = findViewById(R.id.myShip);
        enemyShipV = findViewById(R.id.enemyShip);

        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (java.lang.NullPointerException e) {

        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

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

    @Override
    public void onSensorChanged (SensorEvent event) {
        Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        int maxX = mdispSize.x;

        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            int degrees = ((int)event.values[0]);

            switch((degrees/30))
            {
                case 1:
                case 2:
                case 3:
                    if (myShipV.getX() < (maxX - 25)) {
                        myShipV.setX(myShipV.getX() + 5);
                    }
                    break;
                case 10:
                case 11:
                case 12:
                    if (myShipV.getX() > 5) {
                        myShipV.setX(myShipV.getX() - 5);
                    }
                    break;
            }

        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


   // @Override
   // public void onBackPressed() {

//    }
}

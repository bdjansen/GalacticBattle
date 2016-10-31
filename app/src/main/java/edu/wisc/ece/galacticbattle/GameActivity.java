package edu.wisc.ece.galacticbattle;

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Blake on 10/17/2016.
 */

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    public final static String EXTRA_OUTCOME = "com.example.galacticbattle.OUTCOME";
    private Spaceship myShip;
    private Spaceship enemyShip;

    private int maxX;

    private SensorManager sensorManager;

    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        View myShipV = findViewById(R.id.myShip);
        View enemyShipV = findViewById(R.id.enemyShip);

        myShip = new Spaceship((int)myShipV.getX(), (int)myShipV.getY(), myShipV);
        enemyShip = new Spaceship((int)enemyShipV.getX(), (int)enemyShipV.getY(), enemyShipV);

        Display mdisp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mdisp.getSize(size);
        maxX = size.x;

        int magicNumber = 150;

        myShip.setX(maxX/2 - magicNumber);

        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (java.lang.NullPointerException e) {

        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                System.out.println("Action was DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE) :
                System.out.println("Action was MOVE");
                return true;
            case (MotionEvent.ACTION_UP) :
                System.out.println("Action was UP");
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                System.out.println("Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                System.out.println("Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }


    private boolean shipHit(Spaceship ship)
    {
        for (Bullet current : bullets)
        {
            if (ship.isHit(current))
            {
                ship.hit();
                return true;
            }
        }

        return false;
    }

    @Override
    public void onSensorChanged (SensorEvent event) {
        int speed = 0;
        int imageWidth = 300;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = ((int)event.values[0]);
            x = -(x);

            switch(x)
            {
                case -10:
                case -9:
                case -8:
                case -7:
                case -6:
                case -5:
                    speed = 10;
                    break;
                case -4:
                case -3:
                case -2:
                case -1:
                    speed = 5;
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    speed = 5;
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    speed = 10;
                    break;
            }

            if (x > 0)
            {
                if (myShip.getX() < maxX - (imageWidth + speed))
                {
                    myShip.setX(myShip.getX() + speed);
                }
            }
            else
            {
                if (myShip.getX() > speed)
                {
                    myShip.setX(myShip.getX() - speed);
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //@Override
   //public void onBackPressed() {

   //}
}

package edu.wisc.ece.galacticbattle;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.Space;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Blake on 10/17/2016.
 */

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    public final static String EXTRA_OUTCOME = "com.example.galacticbattle.OUTCOME";
    private Spaceship myShip;
    private Spaceship enemyShip;

    private int maxX;
    private int shipSpeed;
    int counter = 0;

    private SensorManager sensorManager;

    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    private ArrayList<Bullet> enemyBullets = new ArrayList<Bullet>();

    public boolean canShoot = true;
    public boolean timing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        Display mdisp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mdisp.getSize(size);
        maxX = size.x;
        int maxY = size.y;

        ImageView myShipV = (ImageView) findViewById(R.id.myShip);
        ImageView enemyShipV = (ImageView) findViewById(R.id.enemyShip);

        myShipV.layout(maxX / 2 - 150,maxY,maxX / 2 + 150,maxY - 300);
        enemyShipV.layout(maxX / 2 - 150,0,maxX / 2 + 150,300);

        System.out.println("SEARCH THIS: MY VIEW SHIP (x,y) = (" + myShipV.getX() + "," + myShipV.getY() + ")");
        System.out.println("SEARCH THIS: ENEMY VIEW SHIP (x,y) = (" + enemyShipV.getX() + "," + enemyShipV.getY() + ")");

        myShip = new Spaceship((int) myShipV.getX(), (int) myShipV.getY(), myShipV);
        enemyShip = new Spaceship((int) enemyShipV.getX(), (int) enemyShipV.getY(), enemyShipV);

        int magicNumber = 150;

        myShip.setX(maxX / 2);

        loadUserData();

        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (java.lang.NullPointerException e) {

        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        //This thread is set to update the timer as long as a boolean is set
        //It also uses the variables to do the logic corresponding to the timer value
        Thread shootTimer = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        if (timing) {
                            Thread.sleep(1000);
                            timing = false;
                            canShoot = true;
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        shootTimer.start();

        Thread bulletLogic = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        counter++;
                        Thread.sleep(1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bullet current[] = new Bullet[bullets.size()];
                                bullets.toArray(current);
                                for (int i = 0; i < current.length; i++) {
                                    current[i].move();
                                    if (current[i].getY() < 0) {
                                        RelativeLayout layout =
                                                (RelativeLayout) findViewById(R.id.layout);
                                        layout.removeView(current[i].image());
                                        bullets.remove(current[i]);
                                    }
                                    if(counter % 100 == 0) {
                                        System.out.println("1 " + current[i].getX());
                                        System.out.println(current[i].getY());
                                        System.out.println(enemyShip.getX());
                                        System.out.println(enemyShip.getY());
                                    }
                                    shipHit(myShip);
                                    enemyHit();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        bulletLogic.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if (canShoot) {
                    System.out.println("Action was UP");
                    canShoot = false;
                    timing = true;
                    ImageView v = new ImageView(this);
                    Bullet shot = new Bullet(myShip.getX() + myShip.getWidthRadius() -
                            15, myShip.getY() - myShip.getHeightRadius(), 15, 100, v);
                    shot.setSource();
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
                    layout.addView(shot.image());
                    bullets.add(shot);
                } else {
                    System.out.println("Tapped but didn't shoot");
                    System.out.println(enemyShip.getY());
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }

    }


    private boolean shipHit(Spaceship ship) {
        for (Bullet current : enemyBullets) {
            if (ship.isHit(current)) {
                ship.hit();
                enemyBullets.remove(current);
                return true;
            }
        }

        return false;
    }

    private void enemyHit() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bullet arrayBullets[] = new Bullet[bullets.size()];
                bullets.toArray(arrayBullets);
                for (Bullet current : arrayBullets) {
                    if (enemyShip.isHit(current)) {
                        RelativeLayout layout =
                                (RelativeLayout) findViewById(R.id.layout);
                        layout.removeView(current.image());
                        System.out.println("HIT THE ENEMY");
                        bullets.remove(current);
                    }
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int speed = 0;
        int imageWidth = 300;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = ((int) event.values[0]);
            x = -(x);

            switch (x) {
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

            if (x > 0 && Math.abs(x) > 1) {
                if (myShip.getX() < maxX - (imageWidth + speed)) {
                    myShip.setX(myShip.getX() + (speed + shipSpeed));
                }
            } else if(x <= 0 && Math.abs(x) > 1) {
                if (myShip.getX() > speed) {
                    myShip.setX(myShip.getX() - (speed + shipSpeed));
                }
            }
        }
    }

    private void loadUserData() {
        // Create the shared preferences variable so we can load in the data
        String mKey = getString(R.string.preference_name);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);
        int shipColor;

        // Load the string of all the names and then split them by the correct character
        mKey = getString(R.string.preference_key_profile_colors);
        shipColor = mPrefs.getInt(mKey, 1);

        mKey = getString(R.string.preference_key_profile_speed);
        shipSpeed = mPrefs.getInt(mKey, 50);

        switch (shipColor) {
            case 1:
                myShip.setSource(R.drawable.spaceship);
                enemyShip.setSource(R.drawable.spaceship);
                break;
            case 2:
                myShip.setSource(R.drawable.spaceship_2);
                enemyShip.setSource(R.drawable.spaceship_2);
                break;
            case 3:
                myShip.setSource(R.drawable.spaceship_3);
                enemyShip.setSource(R.drawable.spaceship_3);
                break;
        }

        switch (shipSpeed / 25) {
            case 0:
                shipSpeed = 0;
                break;
            case 1:
                shipSpeed = 4;
                break;
            case 2:
                shipSpeed = 8;
                break;
            case 3:
                shipSpeed = 12;
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    //@Override
   //public void onBackPressed() {

   //}
}

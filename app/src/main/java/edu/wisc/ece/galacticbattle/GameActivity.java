package edu.wisc.ece.galacticbattle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.util.ArrayList;

/**
 * Created by Blake on 10/17/2016.
 */

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    public final static String EXTRA_OUTCOME = "com.example.galacticbattle.OUTCOME";
    private Spaceship myShip;
    private Spaceship enemyShip;
    private ConnectedThread connectionThread;

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

        GalacticBattleApp myApp = (GalacticBattleApp) getApplicationContext();
        connectionThread = new ConnectedThread(myApp.getSocket(), null);
        connectionThread.start();

        Display mdisp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mdisp.getSize(size);
        maxX = size.x;
        int maxY = size.y;

        System.out.println("x " + size.x + "y " + size.y + "\n");

        ImageView myShipV = (ImageView) findViewById(R.id.myShip);
        ImageView enemyShipV = (ImageView) findViewById(R.id.enemyShip);

        myShipV.layout((int) (maxX / 2 - maxX*.1041667),(int) (maxY - maxY*.11719),
                (int) (maxX / 2 + maxX*.1041667),maxY);
        enemyShipV.layout((int) (maxX / 2 - maxX*.1041667),0,
                (int) (maxX / 2 + maxX*.1041667),(int) (maxY*.11719));

        myShip = new Spaceship((int) (myShipV.getX() - maxX*.1041667), (int) myShipV.getY(), myShipV);
        enemyShip = new Spaceship((int) (enemyShipV.getX() - maxX*.1041667),
                (int) enemyShipV.getY(), enemyShipV);

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
                                Bullet current[] = new Bullet[enemyBullets.size()];
                                enemyBullets.toArray(current);
                                for (int i = 0; i < current.length; i++) {
                                    current[i].move();
                                    if (current[i].getY() > 2560) {
                                        RelativeLayout layout =
                                                (RelativeLayout) findViewById(R.id.layout);
                                        layout.removeView(current[i].image());
                                        enemyBullets.remove(current[i]);
                                    }
                                    if(counter % 100 == 0) {
                                        //System.out.println("1 " + current[i].getX());
                                        //System.out.println(current[i].getY());
                                        //System.out.println(enemyShip.getX());
                                        //System.out.println(enemyShip.getY());
                                    }
                                    shipHit();
                                    enemyHit();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        Thread writeLogic = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        int countWrite = 0;
                        countWrite++;
                        Thread.sleep(1);
                        if(countWrite % 1000 == 0) {
                            byte[] location = new byte[4];
                            location[0] = (byte) ((((int) myShip.getX()) >> 24) & 0xFF);
                            location[1] = (byte) ((((int) myShip.getX()) >> 16) & 0xFF);
                            location[2] = (byte) ((((int) myShip.getX()) >> 8) & 0xFF);
                            location[3] = (byte) (((int) myShip.getX()) & 0xFF);
                            connectionThread.write(location);
                        }
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
                    Bullet shot = new Bullet((int) enemyShip.getX() + enemyShip.getWidthRadius() -
                            enemyShip.getWidthRadius()/10, (int )enemyShip.getY() - enemyShip.getHeightRadius(),
                            enemyShip.getWidthRadius()/10, enemyShip.getWidthRadius()*10/15, v);
                    shot.setSource();
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
                    layout.addView(shot.image());
                    enemyBullets.add(shot);
                } else {
                    System.out.println("Tapped but didn't shoot");
                    System.out.println(enemyShip.getY());
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }

    }


    private void shipHit() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bullet arrayBullets[] = new Bullet[enemyBullets.size()];
                enemyBullets.toArray(arrayBullets);
                for (Bullet current : arrayBullets) {
                    if (myShip.isHit(current)) {
                        RelativeLayout layout =
                                (RelativeLayout) findViewById(R.id.layout);
                        layout.removeView(current.image());
                        System.out.println("WE GOT HIT");
                        enemyBullets.remove(current);
                        myShip.hit();
                        if(!myShip.isAlive()) {
                            endGame("LOST");
                        }
                    }
                }
            }
        });
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
                        enemyShip.hit();
                        if(!enemyShip.isAlive()) {
                            endGame("WON");
                        }
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
                    myShip.setX((int) myShip.getX() + (speed + shipSpeed));
                }
            } else if(x <= 0 && Math.abs(x) > 1) {
                if (myShip.getX() > speed) {
                    myShip.setX((int) myShip.getX() - (speed + shipSpeed));
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
                enemyShip.setSource(R.drawable.enemy);
                break;
            case 2:
                myShip.setSource(R.drawable.spaceship_2);
                enemyShip.setSource(R.drawable.spaceship_2_enemy);
                break;
            case 3:
                myShip.setSource(R.drawable.spaceship_3);
                enemyShip.setSource(R.drawable.spaceship_3_enemy);
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

    public void endGame(String message) {
        Intent intent = new Intent(this, EndScreenActivity.class);
        intent.putExtra(EXTRA_OUTCOME, message);
        startActivity(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    //@Override
   //public void onBackPressed() {

   //}
}

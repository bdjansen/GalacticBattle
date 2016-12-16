package edu.wisc.ece.galacticbattle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.nio.ByteBuffer;
import java.util.ArrayList;



/**
 * Created by Blake on 10/17/2016.
 * This class handles the entirety of the multiplayer game.  It goes through the ships moving,
 * shooting bullets, writing out bluetooth data, and making players invulnerable.
 */

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    // This string says whether the user won or lost
    public final static String EXTRA_OUTCOME = "com.example.galacticbattle.OUTCOME";

    // These objects are the characters in the game
    private Spaceship myShip;
    private Spaceship enemyShip;
    private ArrayList<SpaceInvader> spaceInvaders;

    // These variables are used for bluetooth data
    private ConnectedThread connectionThread;
    private byte[] bytePacket = new byte[1024];
    private int counterWriteInt = 0;

    // Screen size and ship speed
    private int maxX;
    private int maxY;
    private int shipSpeed;

    Thread writeLogic, bulletLogic;
    GameActivity activity = this;

    // Sensor for moving the ship back and forth
    private SensorManager sensorManager;

    // Theses arraylists and arrays handle the bullets and space invader's data
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Bullet> enemyBullets = new ArrayList<>();
    private Bullet bulletsArray[] = new Bullet[20];
    private Bullet enemyBulletsArray[] = new Bullet[20];
    private Bullet current[] = new Bullet[20];
    private Bullet currentEnemy[] = new Bullet[20];
    private SpaceInvader invadersArray[] = new SpaceInvader[10];

    // invulnerability animation
    private final Animation blinking = new AlphaAnimation(1, 0);

    RelativeLayout layout;

    // Booleans and runnables to handle invulnerability and shooting
    public boolean canShoot = true;
    public boolean invulnerable = false;
    public boolean enemyInvulnerable = false;
    public boolean enemyShoot = true;
    private final Handler timerHandler = new Handler();
    private final Runnable rTimer = new Runnable() {
        @Override
        public void run() {
            if (!canShoot)
                canShoot = true;
        }
    };

    private final Handler enemyTimerHandler = new Handler();
    private final Runnable enemyTimer = new Runnable() {
        @Override
        public void run() {
            if (!enemyShoot)
                enemyShoot = true;
        }
    };

    private final Handler invulnerableHandler = new Handler();
    private final Runnable rInvulnerable = new Runnable() {
        @Override
        public void run() {
            if (invulnerable)
                invulnerable = false;
        }
    };

    private final Handler eInvulnerableHandler = new Handler();
    private final Runnable rEInvulnerable = new Runnable() {
        @Override
        public void run() {
            if (enemyInvulnerable)
                enemyInvulnerable = false;
        }
    };

    // Bluetooth handler.  Processes the data that we receive
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                // Receive the bytes from the message
                byte[] x = (byte[]) msg.obj;
                ByteBuffer reader = ByteBuffer.wrap(x);

                // Get the enemy ship location
                final float shipX = reader.getFloat();

                // Get the integer to check if they shot a bullet
                int bullet = reader.getInt(8);
                if (bullet == 1 && enemyShoot) {
                    // Create and add the new enemy bullet to the list
                    enemyShoot = false;
                    ImageView v = new ImageView(activity);
                    Bullet shot = new Bullet(enemyShip.getX(), enemyShip.getY() + enemyShip.getHeightRadius(), v);
                    shot.setSource();
                    v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
                    v.setY((shot.getY() - shot.getHeightRadius()) * maxY);
                    v.setLayoutParams(new RelativeLayout.LayoutParams((int) (shot.getWidthRadius() * 2 * maxX),
                            (int) (shot.getHeightRadius() * 2 * maxY)));
                    layout.addView(shot.image());
                    enemyBullets.add(shot);
                    enemyTimerHandler.postDelayed(enemyTimer, 1000);
                }
                // Put the ship to the correct location
                if (shipX > 0) {
                    ImageView shipView = enemyShip.image();
                    enemyShip.setX(shipX);
                    shipView.setX((enemyShip.getX() - enemyShip.getWidthRadius()) * maxX);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        // Get the overall app bluetooth socket and create our bluetooth connection from it
        GalacticBattleApp myApp = (GalacticBattleApp) getApplicationContext();
        connectionThread = new ConnectedThread(myApp.getSocket(), mHandler);
        connectionThread.start();

        // Get the screen size so we know where to draw the ships
        Display mdisp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mdisp.getSize(size);
        maxX = size.x;
        maxY = size.y;

        // Set the animation for a ship getting hit
        blinking.setDuration(500);
        blinking.setInterpolator(new LinearInterpolator());
        blinking.setRepeatCount(5);
        blinking.setRepeatMode(Animation.REVERSE);

        //Create our two ship images and set their parameters
        ImageView myShipV = (ImageView) findViewById(R.id.myShip);
        ImageView enemyShipV = (ImageView) findViewById(R.id.enemyShip);
        myShipV.setLayoutParams(new RelativeLayout.LayoutParams((int)(maxX * 0.1) , (int)(maxX * .1)));
        enemyShipV.setLayoutParams(new RelativeLayout.LayoutParams((int)(maxX * 0.1) , (int)(maxX * .1)));

        myShip = new Spaceship((float)0.5, (float)0.95, myShipV);
        enemyShip = new Spaceship((float)0.5, (float)0.05, enemyShipV);

        // Set the image locations of the ship (they are different than the object's location)
        myShipV.setX(maxX * (myShip.getX() - myShip.getWidthRadius()));
        myShipV.setY(maxY * (myShip.getY() - myShip.getWidthRadius()));
        enemyShipV.setX(maxX * (enemyShip.getX() - enemyShip.getWidthRadius()));
        enemyShipV.setY(maxY * (enemyShip.getY() - enemyShip.getWidthRadius()));

        // Set up our space invaders
        spaceInvaders = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = (RelativeLayout) findViewById(R.id.layout);

        for (int i = 0; i < 8; i++) {
            // Set up the picture to the correct screen size
            ImageView currInvader = (ImageView) inflater.inflate(R.layout.space_invader_view, null);
            currInvader.setId(1000 + i);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int)(maxX * 0.1), (int)(maxX * 0.1));
            currInvader.setLayoutParams(params);
            layout.addView(currInvader);

            // The space invaders are set up in a straight horizontal line along the center of the screen
            SpaceInvader newInvader = new SpaceInvader((((float)i) / 8) + (float)0.07, (float) 0.5, currInvader, true);//0.07 is a magic number

            // Add space invader to the list and put the image in the correct location
            spaceInvaders.add(newInvader);
            currInvader.setX((int)(maxX * (newInvader.getX() - newInvader.getWidthRadius())));
            currInvader.setY((int)(maxY * (newInvader.getY() - newInvader.getHeightRadius())));
            currInvader.requestLayout();
        }

        // This gets the data from the options screen to set ship color and move sensitivity
        loadUserData();

        // Hide the action bar because we do not need it
        ActionBar bar = getSupportActionBar();
        try {
            if(bar != null)
                bar.hide();
        } catch (NullPointerException e) {

        }

        // Set up the sensor manager for accelerometer to measure tilting for movement
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);


        // This thread repeatedly runs to move bullets and calculate hit detection to check
        // if any bullets have hit any ships or space invaders
        bulletLogic = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        // We want this to run every millisecond
                        Thread.sleep(1);
                        // For each of our bullets, make them move, and if they go off-
                        // screen, remove them from the list
                        bullets.toArray(current);
                        for (int i = 0; i < current.length; i++) {
                            if(current[i] == null)
                                continue;
                            current[i].move();
                            final ImageView movedImage = current[i].image();
                            final Bullet b = current[i];
                            runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            movedImage.setY((b.getY() - b.getHeightRadius())*maxY);
                                        }
                                    });
                                    if (current[i].getY() < 0) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                layout.removeView(b.image());
                                            }
                                        });
                                        bullets.remove(current[i]);
                                    }

                        }

                        // For each of our enemies bullets, make them move, and if they go off-
                        // screen, remove them from the list
                        enemyBullets.toArray(currentEnemy);
                        for (int i = 0; i < currentEnemy.length; i++) {
                            if(currentEnemy[i] == null)
                                continue;
                            currentEnemy[i].moveEnemy();
                            final ImageView movedImage = currentEnemy[i].image();
                            final Bullet b = currentEnemy[i];
                            runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            movedImage.setY((b.getY() - b.getHeightRadius())*maxY);
                                        }
                                    });
                                    if (currentEnemy[i].getY() > 1) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                layout.removeView(b.image());
                                            }
                                        });
                                        enemyBullets.remove(currentEnemy[i]);
                                    }
                        }

                        // Check to see if anything was hit by the bullets
                        enemyHit();
                        shipHit();
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        // This is the logic used to send the bluetooth data to the other machine
        writeLogic = new Thread() {

            @Override
            public void run() {
                try {
                    int countWrite = 0;
                    while (true) {
                        countWrite++;
                        Thread.sleep(1);
                        // We want to write every 10 milliseconds
                        if(countWrite % 10 == 0) {
                            counterWriteInt++;

                            // Write and send the location of our ship
                            float value = 1 - myShip.getX();
                            byte[] location = ByteBuffer.allocate(8).putFloat(value).array();
                            for(int i = 0; i < 8; i++) {
                                bytePacket[i] = location[i];
                            }

                            // Send the data
                            connectionThread.write(bytePacket);

                            // If a bullet is shot, we will write it 5 times so assure it sent
                            // (We had some errors where it would not receive it)
                            if(counterWriteInt == 5) {
                                // Clear the bullet shooting once we are done sending it
                                for (int i = 0; i < 4; i++) {
                                    bytePacket[i + 8] = 0;
                                    counterWriteInt = 0;
                                }
                            }
                        }
                     }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        bulletLogic.start();
        writeLogic.start();
    }

    // Our touch event is used to shoot bullets by tapping the screen
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            // We want the action to be when we tap down, it shoots
            case (MotionEvent.ACTION_DOWN):
                if (canShoot) {
                    canShoot = false;

                    // Reset the bluetooth bullet send boolean
                    counterWriteInt = 0;

                    // Set up the bluetooth variables to send bullet
                    byte[] location = ByteBuffer.allocate(4).putInt(1).array();
                    for(int i = 0; i < 4; i++) {
                        bytePacket[i + 8] = location[i];
                    }

                    // Create the bullet object and image to shoot
                    ImageView v = new ImageView(this);
                    Bullet shot = new Bullet(myShip.getX(), myShip.getY() - myShip.getHeightRadius()
                            - 0.0375f, v);
                    shot.setSource();
                    v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
                    v.setY((shot.getY() - shot.getHeightRadius()) * maxY);
                    v.setLayoutParams(new RelativeLayout.LayoutParams((int)(shot.getWidthRadius() * 2 * maxX),
                            (int)(shot.getHeightRadius()* 2 * maxY)));
                    layout.addView(shot.image());

                    // Add the bullet to shoot, and then set the timer for 1 second until
                    // They can shoot again
                    bullets.add(shot);
                    timerHandler.postDelayed(rTimer, 1000);
                } else {
                    System.out.println("Tapped but didn't shoot");
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }

    }


    // Check to see if our ship or space invaders got hit by any enemy bullets
    private void shipHit() {
                enemyBullets.toArray(enemyBulletsArray);
                for (Bullet current : enemyBulletsArray) {
                    if (current == null)
                        continue;

                    // When we get hit, start invulnerability and lose a life
                    if (myShip.isHit(current)) {
                        final Bullet b = current;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout.removeView(b.image());
                            }
                        });
                        enemyBullets.remove(current);
                        if (!invulnerable) {
                            myShip.hit();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myShip.ship.startAnimation(blinking);
                                }
                            });
                            invulnerable = true;
                            invulnerableHandler.postDelayed(rInvulnerable, 2500);
                        }

                        // If we are out of lives, we lose the game
                        if(!myShip.isAlive()) {
                            endGame("LOST");
                        }
                    }

                    // Check if any space invaders were hit by the bullets
                    spaceInvaders.toArray(invadersArray);
                    for (SpaceInvader invader : invadersArray) {
                        if(invader == null)
                            continue;
                        if (invader.isHit(current)) {
                            final Bullet b = current;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    layout.removeView(b.image());
                                }
                            });
                            enemyBullets.remove(current);
                            invader.hit();

                            // Destroy the space invader if it dies
                            final SpaceInvader i = invader;
                            if (!invader.isAlive()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layout.removeView(i.image());
                                    }
                                });
                                spaceInvaders.remove(invader);
                            }
                        }
                    }
                }
    }

    // Check to see if the enemy or any space invader is hit by one of our bullets
    private void enemyHit() {
                bullets.toArray(bulletsArray);
                for (Bullet current : bulletsArray) {
                    if (current == null) {
                        continue;
                    }
                    // Remove the bullet and mark the enemy as hit
                    if (enemyShip.isHit(current)) {
                        final Bullet b = current;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout.removeView(b.image());
                            }
                        });
                        bullets.remove(current);
                        if (!enemyInvulnerable) {
                            enemyShip.hit();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    enemyShip.ship.startAnimation(blinking);
                                }
                            });
                            enemyInvulnerable = true;
                            eInvulnerableHandler.postDelayed(rEInvulnerable, 2500);
                        }
                        // End the game if the enemy dies
                        if(!enemyShip.isAlive()) {
                            endGame("WON");
                        }
                    }
                    // If a space invader is hit, kill the bullet and the space invader
                    spaceInvaders.toArray(invadersArray);
                    for (SpaceInvader invader : invadersArray) {
                        if(invader == null)
                            continue;
                        if (invader.isHit(current)) {
                            final Bullet b = current;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    layout.removeView(b.image());
                                }
                            });
                            bullets.remove(current);
                            invader.hit();
                            final SpaceInvader i = invader;
                            if (!invader.isAlive()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layout.removeView(i.image());
                                    }
                                });
                                spaceInvaders.remove(invader);
                            }
                        }
                    }
                }
    }

    // Use the accelerometer to allow the player to steer the ship to move back and forth
    @Override
    public void onSensorChanged(SensorEvent event) {
        float speed = 0;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = ((int) event.values[0]);
            x = -(x);

            // Case statement to set the speed of the ship depending on how far we are
            // tilting the screen (taking into account the player's sensitivity preference)
            switch(shipSpeed){
                case 1:
                    switch (x) {
                        case -10:
                        case -9:
                        case -8:
                            speed = (float)0.008;
                            break;
                        case -7:
                        case -6:
                        case -5:

                        case -4:
                        case -3:
                            speed = (float)0.004;
                            break;
                        case -2:
                        case -1:

                        case 1:
                        case 2:
                        case 3:
                        case 4:

                        case 5:
                        case 6:
                            speed = (float)0.004;
                            break;
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                            speed = (float)0.008;
                            break;
                    }
                    break;
                case 2:
                    switch (x) {
                        case -10:
                        case -9:
                        case -8:
                        case -7:
                        case -6:
                        case -5:
                            speed = (float)0.008;
                            break;
                        case -4:
                        case -3:
                        case -2:
                        case -1:
                            speed = (float)0.004;
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            speed = (float)0.004;
                            break;
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                            speed = (float)0.008;
                            break;
                    }
                    break;
                case 3:
                    switch (x) {
                        case -10:
                        case -9:
                        case -8:
                        case -7:
                        case -6:
                        case -5:

                        case -4:
                            speed = (float)0.008;
                            break;
                        case -3:
                        case -2:
                        case -1:
                            speed = (float)0.004;
                            break;
                        case 1:
                        case 2:
                        case 3:
                            speed = (float)0.004;
                            break;
                        case 4:

                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                            speed = (float)0.008;
                            break;
                    }
                    break;
                case 4:
                    switch (x) {
                        case -10:
                        case -9:
                        case -8:
                        case -7:
                        case -6:
                        case -5:

                        case -4:
                        case -3:
                            speed = (float)0.008;
                            break;
                        case -2:
                        case -1:
                            speed = (float)0.004;
                            break;
                        case 1:
                        case 2:
                            speed = (float)0.004;
                            break;
                        case 3:
                        case 4:

                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                            speed = (float)0.008;
                            break;
                    }
                    break;
            }

            ImageView shipView = myShip.image();

            // Make sure we don't run into the right wall
            if (x > 0 && Math.abs(x) > 1) {
                if (myShip.getX() < (1 - myShip.getWidthRadius())) {
                        myShip.setX(myShip.getX() + speed);
                        shipView.setX((myShip.getX() - myShip.getWidthRadius()) * maxX);
                }
                // Make sure we don't run into the left wall
            } else if (x <= 0 && Math.abs(x) > 1) {
                if (myShip.getX() > myShip.getWidthRadius()) {
                        myShip.setX(myShip.getX() - speed );
                        shipView.setX((myShip.getX() - myShip.getWidthRadius()) * maxX);
                    }
            }
        }
    }

    // Put all of the user preferences from the options screen into the game
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

        // Get the user preference for ship color
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

        // Get the user preference for ship sensitivity
        switch (shipSpeed / 25) {
            case 0:
                shipSpeed = 1;
                break;
            case 1:
                shipSpeed = 2;
                break;
            case 2:
                shipSpeed = 3;
                break;
            case 3:
                shipSpeed = 4;
                break;
        }
    }


    // When we finish the game, we want to go to the end screen
    public void endGame(String message) {
        // Cancel the bluetooth and close all threads and destory them
        connectionThread.cancel();
        connectionThread.interrupt();
        writeLogic.interrupt();
        bulletLogic.interrupt();
        connectionThread = null;
        writeLogic = null;
        bulletLogic = null;

        // Stop listening to the accelerometer
        sensorManager.unregisterListener(this);

        // Go to the ending screen
        Intent intent = new Intent(this, EndScreenActivity.class);
        message = message + " versus";
        intent.putExtra(EXTRA_OUTCOME, message);
        startActivity(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // We don't want to do anything during a back press in the middle of the game
    @Override
    public void onBackPressed() {
        connectionThread.cancel();

        super.onBackPressed();
    }

}

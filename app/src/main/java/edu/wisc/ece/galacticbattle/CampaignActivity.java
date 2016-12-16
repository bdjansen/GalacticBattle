package edu.wisc.ece.galacticbattle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.util.ArrayList;

/**
 * Created by Matt on 11/30/2016.
 * This class handles the entire campaign mode of our game. It deals with ship and space invader
 * movement, ship and space invader bullet shots, and the transition between levels. There are three
 * levels in our campaign mode which are all of pretty equal difficulty.
 */
public class CampaignActivity extends AppCompatActivity implements SensorEventListener {
    // This string says whether the user won or lost
    public final static String EXTRA_OUTCOME = "com.example.galacticbattle.OUTCOME";

    // This is used as a work around to make a reference to "this" activity within a thread
    private CampaignActivity thisC = this;

    // These objects are the characters in the game
    private Spaceship myShip;
    private ArrayList<SpaceInvader> spaceInvaders;

    // Screen size, ship speed, current level, and an integer to keep track of which space invader
    // is currently shooting
    private int maxX;
    private int maxY;
    private int shipSpeed;
    private int level;
    private int swapInvaders = 0;

    // Sensor for moving the ship back and forth, reference to our game layout for this class
    private SensorManager sensorManager;
    private RelativeLayout gameLayout;

    // Theses arraylists and arrays handle the bullets and space invader's data
    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    private ArrayList<Bullet> enemyBullets = new ArrayList<Bullet>();
    private Bullet [] myBulletsArray = new Bullet[20];
    private Bullet [] myBulletsArray2 = new Bullet[20];
    private Bullet [] enemyBulletsArray = new Bullet[20];
    private Bullet [] enemyBulletsArray2 = new Bullet[20];
    private SpaceInvader [] spaceInvadersArray = new SpaceInvader[10];

    // invulnerability animation and threads that we want to interrupt before moving to another
    // activity
    private final Animation blinking = new AlphaAnimation(1, 0);
    private Thread spaceInvadersMove, bulletLogic;

    // Booleans and runnables to handle invulnerability and shooting
    public boolean canShoot = true;
    public boolean spaceInvaderShoot = true;
    public boolean invulnerable = false;
    private final Handler timerHandler = new Handler();
    private final Runnable rTimer = new Runnable() {
        @Override
        public void run() {
            if (!canShoot)
                canShoot = true;
        }
    };

    private final Handler timerHandler_two = new Handler();
    private final Runnable rTimer_two = new Runnable() {
        @Override
        public void run() {
            if (!spaceInvaderShoot)
                spaceInvaderShoot = true;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);

        // Get the screen size so we know where to draw the ships
        Display mdisp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mdisp.getSize(size);
        maxX = size.x;
        maxY = size.y;

        // Set the starting level to zero
        level = 0;

        // Set the animation for a ship getting hit
        blinking.setDuration(500);
        blinking.setInterpolator(new LinearInterpolator());
        blinking.setRepeatCount(5);
        blinking.setRepeatMode(Animation.REVERSE);

        //Create our ship image and set its parameters
        ImageView myShipV = (ImageView) findViewById(R.id.myShip);
        myShipV.setLayoutParams(new RelativeLayout.LayoutParams((int)(maxX * 0.1) , (int)(maxX * .1)));

        myShip = new Spaceship((float)0.5, (float)0.95, myShipV);

        // Set the image location of the ship (it is different than the object's location)
        myShipV.setX(maxX * (myShip.getX() - myShip.getWidthRadius()));
        myShipV.setY(maxY * (myShip.getY() - myShip.getWidthRadius()));

        // Assign our layout variable to the layout for this activity
        gameLayout = (RelativeLayout) findViewById(R.id.layoutC);

        // Set up our space invaders
        spaceInvaders = new ArrayList<SpaceInvader>();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < 8; i++) {
            // Set up the picture to the correct screen size
            ImageView currInvader = (ImageView) inflater.inflate(R.layout.space_invader_view, null);
            currInvader.setId(1000 + i);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int)(maxX * 0.1), (int)(maxX * 0.1));
            currInvader.setLayoutParams(params);
            gameLayout.addView(currInvader);

            //For level one, the space invaders are set up in a diagonal line from the top left to
            // the bottom right of the screen
            SpaceInvader newInvader = new SpaceInvader((((float)i) / 11) + (float)0.07, (((float)i) / 11) + (float)0.07, currInvader, true);//0.07 is a magic number

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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For each of our bullets, make them move, and if they go off-
                                // screen, remove them from the list
                                bullets.toArray(myBulletsArray);
                                for (int i = 0; i < myBulletsArray.length; i++) {
                                    if(myBulletsArray[i] != null) {
                                        myBulletsArray[i].move();
                                        ImageView movedImage = myBulletsArray[i].image();
                                        movedImage.setY((myBulletsArray[i].getY() - myBulletsArray[i].getHeightRadius()) * maxY);
                                        if (myBulletsArray[i].getY() < 0) {
                                            gameLayout.removeView(myBulletsArray[i].image());
                                            bullets.remove(myBulletsArray[i]);
                                        }
                                    }
                                }

                                // For each of our enemies bullets, make them move, and if they go off-
                                // screen, remove them from the list
                                enemyBullets.toArray(enemyBulletsArray2);
                                for (int i = 0; i < enemyBulletsArray2.length; i++) {
                                    if(enemyBulletsArray2[i] != null) {
                                        enemyBulletsArray2[i].moveEnemy();
                                        ImageView movedImage = enemyBulletsArray2[i].image();
                                        movedImage.setY((enemyBulletsArray2[i].getY() - enemyBulletsArray2[i].getHeightRadius()) * maxY);
                                        if (enemyBulletsArray2[i].getY() > 1) {
                                            gameLayout.removeView(enemyBulletsArray2[i].image());
                                            enemyBullets.remove(enemyBulletsArray2[i]);
                                        }
                                    }
                                }

                                // Check to see if anything was hit by the bulelts
                                shipHit();
                                enemyHit();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        // This thread is used to make the space invaders move for level 1. In level one, the space
        // invaders should be in a diagonal line from top left to bottom right and should move in
        // such a diagonal line back and forth across the screen.
        spaceInvadersMove = new Thread() {

            @Override
            public void run() {
                try {

                    while (true) {
                        // We want this to run every millisecond
                        Thread.sleep(1);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Loop through all the space invaders
                                spaceInvaders.toArray(spaceInvadersArray);
                                for (int i = 0; i < spaceInvadersArray.length; i++) {
                                    if (spaceInvadersArray[i] != null) {
                                        ImageView movedImage = spaceInvadersArray[i].image();

                                        // If it can move right, do so
                                        if (spaceInvadersArray[i].getRight()) {
                                            spaceInvadersArray[i].moveRight();
                                            if (spaceInvadersArray[i].getRight()) {
                                                movedImage.setX((spaceInvadersArray[i].getX() - spaceInvadersArray[i].getWidthRadius()) * maxX);
                                            }
                                        }

                                        // If it can't, then move left
                                        else {
                                            spaceInvadersArray[i].moveLeft();
                                            if (!spaceInvadersArray[i].getRight()) {
                                                movedImage.setX((spaceInvadersArray[i].getX() - spaceInvadersArray[i].getWidthRadius()) * maxX);
                                            }
                                        }

                                        // This if statement deals with the space invaders shooting, they shoot only once
                                        // a second, and the designated space invader to shoot is swapped by the swapInvaders
                                        // counter
                                        if (spaceInvaderShoot && (i == swapInvaders)) {
                                            // Set the boolean to false so they can't shoot for another second
                                            spaceInvaderShoot = false;

                                            // Generate the bullet shot by the space invader, and set its image view parameters
                                            ImageView v = new ImageView(thisC);
                                            Bullet shot = new Bullet(spaceInvadersArray[i].getX(), spaceInvadersArray[i].getY(), v);
                                            shot.setSource();
                                            v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
                                            v.setY((shot.getY() - shot.getHeightRadius()) * maxY);
                                            v.setLayoutParams(new RelativeLayout.LayoutParams((int) (shot.getWidthRadius() * 2 * maxX),
                                                    (int) (shot.getHeightRadius() * 2 * maxY)));

                                            // Add the bullet to the game layout and our array list of enemy bullets
                                            gameLayout.addView(shot.image());
                                            enemyBullets.add(shot);

                                            // Start the handler that controls the one second delay for shots
                                            timerHandler_two.postDelayed(rTimer_two, 1000);
                                        }
                                    }
                                }

                                // If every space invader has shot, set the count back to zero, otherwise
                                // increment it
                                if(swapInvaders == 7)
                                {
                                    swapInvaders = 0;
                                }
                                else
                                {
                                    swapInvaders++;
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        spaceInvadersMove.start();
        bulletLogic.start();
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

                    // Create the bullet object and image to shoot
                    ImageView v = new ImageView(this);
                    Bullet shot = new Bullet(myShip.getX(), myShip.getY() - myShip.getHeightRadius(), v);
                    shot.setSource();
                    v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
                    v.setY((shot.getY() - shot.getHeightRadius()) * maxY);
                    v.setLayoutParams(new RelativeLayout.LayoutParams((int)(shot.getWidthRadius() * 2 * maxX),
                            (int)(shot.getHeightRadius()* 2 * maxY)));
                    gameLayout.addView(shot.image());

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

    // Check to see if our ship got hit by any enemy bullets
    private void shipHit() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enemyBullets.toArray(enemyBulletsArray);
                for (Bullet current : enemyBulletsArray) {
                    if (current != null) {

                        // When we get hit, start invulnerability and lose a life
                        if (myShip.isHit(current)) {
                            gameLayout.removeView(current.image());
                            enemyBullets.remove(current);
                            if (!invulnerable) {
                                myShip.hit();
                                myShip.ship.startAnimation(blinking);
                                invulnerable = true;
                                invulnerableHandler.postDelayed(rInvulnerable, 2500);
                            }

                            // If we are out of lives, we lose the game
                            if (!myShip.isAlive()) {
                                level++;
                                endGame("LOST");
                            }
                        }
                    }
                }
            }
        });
    }

    // Check to see if the space invaders are hit by one of our bullets
    private void enemyHit() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bullets.toArray(myBulletsArray2);
                for (Bullet current : myBulletsArray2) {
                    spaceInvaders.toArray(spaceInvadersArray);
                    for (SpaceInvader invader : spaceInvadersArray) {
                        if (current != null && invader != null) {

                            // Remove the bullet and mark the space invader as hit
                            if (invader.isHit(current)) {
                                gameLayout.removeView(current.image());
                                bullets.remove(current);
                                invader.hit();

                                // Destroy the space invader if it dies
                                if (!invader.isAlive()) {
                                    gameLayout.removeView(invader.image());
                                    spaceInvaders.remove(invader);
                                }
                            }
                        }
                    }

                    // If all the space invaders are dead, we win
                    if(spaceInvaders.size() == 0)
                    {
                        level++;
                        endGame("WIN");
                    }
                }
            }
        });
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
                break;
            case 2:
                myShip.setSource(R.drawable.spaceship_2);
                break;
            case 3:
                myShip.setSource(R.drawable.spaceship_3);
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

    // When we beat a level, we want to move on to the next level, when we beat all levels, we want
    // to go to the end screen
    public void endGame(String message) {
        // Move to level two if we beat level one
        if (level == 1 && message.equals("WIN"))
        {
            spaceInvadersMove.interrupt();
            levelTwo();
        }
        // Move to level three if we beat level two
        else if (level == 2 && message.equals("WIN")) {
            spaceInvadersMove.interrupt();
            levelThree();
        }
        else
        {
            // Close all the threads and destroy them
            spaceInvadersMove.interrupt();
            bulletLogic.interrupt();

            // Stop listening to the accelerometer
            sensorManager.unregisterListener(this);

            // Go to the ending screen
            Intent intent = new Intent(this, EndScreenActivity.class);
            message = message + " campaign";
            intent.putExtra(EXTRA_OUTCOME, message);
            startActivity(intent);
        }
    }

    public void levelTwo()
    {
        // Set up our space invaders
        spaceInvaders = new ArrayList<SpaceInvader>();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // This boolean deals with the direction the space invaders will start moving in
        boolean right;

        for (int i = 0; i < 8; i++) {
            // For level two, our space invaders will start out in a straight vertical line in the center of the
            // screen and the top half of the invaders will start by moving right while the bottom half will
            // start by moving left
            if (i <= 3)
            {
                right = true;
            }
            else
            {
                right = false;
            }

            // Set up the picture to the correct screen size
            ImageView currInvader = (ImageView) inflater.inflate(R.layout.space_invader_view, null);
            currInvader.setId(1000 + i);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int)(maxX * 0.1), (int)(maxX * 0.1));
            currInvader.setLayoutParams(params);
            gameLayout.addView(currInvader);

            // For level two the space invaders start out in a straight vertical line in the center of the screen
            SpaceInvader newInvader = new SpaceInvader((float) 0.45, (((float)i) / 11) + (float)0.07, currInvader, right);//0.07 is a magic number

            // Add space invader to the list and put the image in the correct location
            spaceInvaders.add(newInvader);
            currInvader.setX((int)(maxX * (newInvader.getX() - newInvader.getWidthRadius())));
            currInvader.setY((int)(maxY * (newInvader.getY() - newInvader.getHeightRadius())));
            currInvader.requestLayout();
        }

        // This thread is used to make the space invaders move for level 2.
        spaceInvadersMove = new Thread() {

            @Override
            public void run() {
                try {

                    while (true) {
                        // We want this to run every millisecond
                        Thread.sleep(1);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Loop through all the space invaders
                                spaceInvaders.toArray(spaceInvadersArray);
                                for (int i = 0; i < spaceInvadersArray.length; i++) {
                                    if (spaceInvadersArray[i] != null) {
                                        ImageView movedImage = spaceInvadersArray[i].image();

                                        // If it can move right, do so
                                        if (spaceInvadersArray[i].getRight()) {
                                            spaceInvadersArray[i].moveRight();
                                            if (spaceInvadersArray[i].getRight()) {
                                                movedImage.setX((spaceInvadersArray[i].getX() - spaceInvadersArray[i].getWidthRadius()) * maxX);
                                            }
                                        }

                                        // If it can't, then move left
                                        else {
                                            spaceInvadersArray[i].moveLeft();
                                            if (!spaceInvadersArray[i].getRight()) {
                                                movedImage.setX((spaceInvadersArray[i].getX() - spaceInvadersArray[i].getWidthRadius()) * maxX);
                                            }
                                        }

                                        // This if statement deals with the space invaders shooting, they shoot only once
                                        // a second, and the designated space invader to shoot is swapped by the swapInvaders
                                        // counter
                                        if (spaceInvaderShoot && (i == swapInvaders)) {
                                            // Set the boolean to false so they can't shoot for another second
                                            spaceInvaderShoot = false;

                                            // Generate the bullet shot by the space invader, and set its image view parameters
                                            ImageView v = new ImageView(thisC);
                                            Bullet shot = new Bullet(spaceInvadersArray[i].getX(), spaceInvadersArray[i].getY(), v);
                                            shot.setSource();
                                            v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
                                            v.setY((shot.getY() - shot.getHeightRadius()) * maxY);
                                            v.setLayoutParams(new RelativeLayout.LayoutParams((int) (shot.getWidthRadius() * 2 * maxX),
                                                    (int) (shot.getHeightRadius() * 2 * maxY)));

                                            // Add the bullet to the game layout and our array list of enemy bullets
                                            gameLayout.addView(shot.image());
                                            enemyBullets.add(shot);

                                            // Start the handler that controls the one second delay for shots
                                            timerHandler_two.postDelayed(rTimer_two, 1000);
                                        }
                                    }
                                }

                                // If every space invader has shot, set the count back to zero, otherwise
                                // increment it
                                if(swapInvaders == 7)
                                {
                                    swapInvaders = 0;
                                }
                                else
                                {
                                    swapInvaders++;
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        spaceInvadersMove.start();
    }

    public void levelThree()
    {
        // Set up our space invaders
        spaceInvaders = new ArrayList<SpaceInvader>();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // This boolean deals with the direction the space invaders will start moving in
        boolean right;

        for (int i = 0; i < 8; i++) {
            // For level three, our space invaders will start out in a straight vertical line in the center of the
            // screen and every other space invader will move in a different direction. So the first invader will
            // move right , the second left, the third right, etc.
            if (i % 2 == 0)
            {
                right = true;
            }
            else
            {
                right = false;
            }

            // Set up the picture to the correct screen size
            ImageView currInvader = (ImageView) inflater.inflate(R.layout.space_invader_view, null);
            currInvader.setId(1000 + i);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int)(maxX * 0.1), (int)(maxX * 0.1));
            currInvader.setLayoutParams(params);
            gameLayout.addView(currInvader);

            // For level three the space invaders start out in a straight vertical line in the center of the screen
            SpaceInvader newInvader = new SpaceInvader((float) 0.45, (((float)i) / 11) + (float)0.07, currInvader, right);//0.07 is a magic number

            // Add space invader to the list and put the image in the correct location
            spaceInvaders.add(newInvader);
            currInvader.setX((int)(maxX * (newInvader.getX() - newInvader.getWidthRadius())));
            currInvader.setY((int)(maxY * (newInvader.getY() - newInvader.getHeightRadius())));
            currInvader.requestLayout();
        }

        // This thread is used to make the space invaders move for level 3.
        spaceInvadersMove = new Thread() {

            @Override
            public void run() {
                try {

                    while (true) {
                        // We want this to run every millisecond
                        Thread.sleep(1);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Loop through all the space invaders
                                spaceInvaders.toArray(spaceInvadersArray);
                                for (int i = 0; i < spaceInvadersArray.length; i++) {
                                    if (spaceInvadersArray[i] != null) {
                                        ImageView movedImage = spaceInvadersArray[i].image();

                                        // If it can move right, do so
                                        if (spaceInvadersArray[i].getRight()) {
                                            spaceInvadersArray[i].moveRight();
                                            if (spaceInvadersArray[i].getRight()) {
                                                movedImage.setX((spaceInvadersArray[i].getX() - spaceInvadersArray[i].getWidthRadius()) * maxX);
                                            }
                                        }

                                        // If it can't, then move left
                                        else {
                                            spaceInvadersArray[i].moveLeft();
                                            if (!spaceInvadersArray[i].getRight()) {
                                                movedImage.setX((spaceInvadersArray[i].getX() - spaceInvadersArray[i].getWidthRadius()) * maxX);
                                            }
                                        }

                                        // This if statement deals with the space invaders shooting, they shoot only once
                                        // a second, and the designated space invader to shoot is swapped by the swapInvaders
                                        // counter
                                        if (spaceInvaderShoot && (i == swapInvaders)) {
                                            // Set the boolean to false so they can't shoot for another second
                                            spaceInvaderShoot = false;

                                            // Generate the bullet shot by the space invader, and set its image view parameters
                                            ImageView v = new ImageView(thisC);
                                            Bullet shot = new Bullet(spaceInvadersArray[i].getX(), spaceInvadersArray[i].getY(), v);
                                            shot.setSource();
                                            v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
                                            v.setY((shot.getY() - shot.getHeightRadius()) * maxY);
                                            v.setLayoutParams(new RelativeLayout.LayoutParams((int) (shot.getWidthRadius() * 2 * maxX),
                                                    (int) (shot.getHeightRadius() * 2 * maxY)));

                                            // Add the bullet to the game layout and our array list of enemy bullets
                                            gameLayout.addView(shot.image());
                                            enemyBullets.add(shot);

                                            // Start the handler that controls the one second delay for shots
                                            timerHandler_two.postDelayed(rTimer_two, 1000);
                                        }
                                    }
                                }

                                // If every space invader has shot, set the count back to zero, otherwise
                                // increment it
                                if(swapInvaders == 7)
                                {
                                    swapInvaders = 0;
                                }
                                else
                                {
                                    swapInvaders++;
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        spaceInvadersMove.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // We don't want to do anything during a back press in the middle of the game
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

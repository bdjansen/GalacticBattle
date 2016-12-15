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
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;



/**
 * Created by Blake on 10/17/2016.
 */

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    public final static String EXTRA_OUTCOME = "com.example.galacticbattle.OUTCOME";
    private Spaceship myShip;
    private Spaceship enemyShip;
    private ArrayList<SpaceInvader> spaceInvaders;
    private ConnectedThread connectionThread;

    private int maxX;
    private int maxY;
    private int shipSpeed;
    private gamePacket packet = new gamePacket();
    private byte[] bytePacket = new byte[1024];
    Thread writeLogic, bulletLogic;
    GameActivity activity = this;

    private SensorManager sensorManager;

    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    private ArrayList<Bullet> enemyBullets = new ArrayList<Bullet>();
    private Bullet bulletsArray[] = new Bullet[20];
    private Bullet enemyBulletsArray[] = new Bullet[20];
    private Bullet current[] = new Bullet[20];
    private Bullet currentEnemy[] = new Bullet[20];
    private SpaceInvader invadersArray[] = new SpaceInvader[10];
    private int counterWriteInt = 0;

    private final Animation blinking = new AlphaAnimation(1, 0);

    RelativeLayout layout;

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

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                byte[] x = (byte[]) msg.obj;
                ByteBuffer reader = ByteBuffer.wrap(x);
                final float shipX = reader.getFloat();
                int bullet = reader.getInt(8);
                if (bullet == 1 && enemyShoot) {
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

        GalacticBattleApp myApp = (GalacticBattleApp) getApplicationContext();
        connectionThread = new ConnectedThread(myApp.getSocket(), mHandler);
        connectionThread.start();

        Display mdisp = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mdisp.getSize(size);
        maxX = size.x;
        maxY = size.y;

        // Ship blink code
        blinking.setDuration(500);
        blinking.setInterpolator(new LinearInterpolator());
        blinking.setRepeatCount(5);
        blinking.setRepeatMode(Animation.REVERSE);

        ImageView myShipV = (ImageView) findViewById(R.id.myShip);
        ImageView enemyShipV = (ImageView) findViewById(R.id.enemyShip);

        myShipV.setLayoutParams(new RelativeLayout.LayoutParams((int)(maxX * 0.1) , (int)(maxX * .1)));
        enemyShipV.setLayoutParams(new RelativeLayout.LayoutParams((int)(maxX * 0.1) , (int)(maxX * .1)));

        myShip = new Spaceship((float)0.5, (float)1, myShipV);//middle of ship location
        enemyShip = new Spaceship((float)0.5, (float)0.05, enemyShipV);

        myShipV.setX(maxX * (myShip.getX() - myShip.getWidthRadius()));
        myShipV.setY(maxY * (myShip.getY() - myShip.getWidthRadius()));
        enemyShipV.setX(maxX * (enemyShip.getX() - enemyShip.getWidthRadius()));
        enemyShipV.setY(maxY * (enemyShip.getY() - enemyShip.getWidthRadius()));

        System.out.println("SEARCH THIS: MY VIEW SHIP (x,y) = (" + myShipV.getX() + "," + myShipV.getY() + ")");
        System.out.println("SEARCH THIS: ENEMY VIEW SHIP (x,y) = (" + enemyShipV.getX() + "," + enemyShipV.getY() + ")");

        spaceInvaders = new ArrayList<SpaceInvader>();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout = (RelativeLayout) findViewById(R.id.layout);

        for (int i = 0; i < 8; i++) {
            ImageView currInvader = (ImageView) inflater.inflate(R.layout.space_invader_view, null);
            currInvader.setId(1000 + i);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int)(maxX * 0.1), (int)(maxX * 0.1));
            currInvader.setLayoutParams(params);
            layout.addView(currInvader);
            SpaceInvader newInvader = new SpaceInvader((((float)i) / 8) + (float)0.07, (float) 0.5, currInvader, true);//0.07 is a magic number
            spaceInvaders.add(newInvader);
            //currInvader.layout((int) (maxX * newInvader.getX() + (maxX / 32)), (int) (maxY * newInvader.getY() + maxX / 20),
            //        (int) (maxX * newInvader.getX() + (maxX * 3 / 32)), (int) (maxY * newInvader.getY() - maxX / 20));
            currInvader.setX((int)(maxX * (newInvader.getX() - newInvader.getWidthRadius())));
            currInvader.setY((int)(maxY * (newInvader.getY() - newInvader.getHeightRadius())));
            currInvader.requestLayout();
        }

        loadUserData();


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (NullPointerException e) {

        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);


        bulletLogic = new Thread() {

            @Override
            public void run() {
                try {
                    int counter = 0;
                    while (true) {
                        Thread.sleep(1);
                        counter++;
                        final int threadCounter = counter;
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
                                    enemyHit();
                                    shipHit();
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        writeLogic = new Thread() {

            @Override
            public void run() {
                try {
                    int countWrite = 0;
                    while (true) {
                        countWrite++;
                        Thread.sleep(1);
                        if(countWrite % 10 == 0) {
                            counterWriteInt++;
                            float value = 1 - myShip.getX();
                            byte[] location = ByteBuffer.allocate(8).putFloat(value).array();
                            for(int i = 0; i < 8; i++) {
                                bytePacket[i] = location[i];
                            }
                            connectionThread.write(bytePacket);
                            if(counterWriteInt == 5) {
                                for (int i = 0; i < 4; i++) {
                                    bytePacket[i + 8] = 0;
                                    counterWriteInt = 0;
                                }
                            }
                        }
                     }
                } catch (InterruptedException e) {
                }
            }
        };

        bulletLogic.start();
        writeLogic.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if (canShoot) {
                    canShoot = false;
                    counterWriteInt = 0;
                    ImageView v = new ImageView(this);
                    Bullet shot = new Bullet(myShip.getX(), myShip.getY() - myShip.getHeightRadius(), v);
                    shot.setSource();
                    v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
                    packet.setBulletX(shot.getX());
                    byte[] location = ByteBuffer.allocate(4).putInt(1).array();
                    for(int i = 0; i < 4; i++) {
                        bytePacket[i + 8] = location[i];
                    }
                    v.setY((shot.getY() - shot.getHeightRadius()) * maxY);

                    v.setLayoutParams(new RelativeLayout.LayoutParams((int)(shot.getWidthRadius() * 2 * maxX),
                            (int)(shot.getHeightRadius()* 2 * maxY)));

                    layout.addView(shot.image());
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


    private void shipHit() {

                enemyBullets.toArray(enemyBulletsArray);
                for (Bullet current : enemyBulletsArray) {
                    if (current == null)
                        continue;
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
                        if(!myShip.isAlive()) {
                            endGame("LOST");
                        }
                    }
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
                            System.out.println("HIT THE INVADER");
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

    private void enemyHit() {

                bullets.toArray(bulletsArray);
                for (Bullet current : bulletsArray) {
                    if (current == null) {
                        continue;
                    }
                    if (enemyShip.isHit(current)) {

                        final Bullet b = current;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout.removeView(b.image());
                            }
                        });
                        System.out.println("HIT THE ENEMY");
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
                        if(!enemyShip.isAlive()) {
                            endGame("WON");
                        }
                    }
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
                            System.out.println("HIT THE INVADER");
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        float speed = 0;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = ((int) event.values[0]);
            x = -(x);

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

            //hey
            /*
            switch (x) {
                case -10:
                case -9:
                case -8:
                case -7:
                case -6:
                case -5:
                    speed = (float)0.001;
                    break;
                case -4:
                case -3:
                case -2:
                case -1:
                    speed = (float)0.0005;
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    speed = (float)0.0005;
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    speed = (float)0.001;
                    break;
            }
            */

            ImageView shipView = myShip.image();
            if (x > 0 && Math.abs(x) > 1) {
                if (myShip.getX() < (1 - myShip.getWidthRadius())) {
                        myShip.setX(myShip.getX() + speed);
                        shipView.setX((myShip.getX() - myShip.getWidthRadius()) * maxX);
                }
            } else if (x <= 0 && Math.abs(x) > 1) {
                if (myShip.getX() > myShip.getWidthRadius()) {
                        myShip.setX(myShip.getX() - speed );
                        shipView.setX((myShip.getX() - myShip.getWidthRadius()) * maxX);
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

    public void endGame(String message) {
        Intent intent = new Intent(this, EndScreenActivity.class);
        message = message + " versus";
        intent.putExtra(EXTRA_OUTCOME, message);
        connectionThread.cancel();
        connectionThread.interrupt();
        writeLogic.interrupt();
        bulletLogic.interrupt();
        connectionThread = null;
        writeLogic = null;
        bulletLogic = null;
        sensorManager.unregisterListener(this);
        startActivity(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    //@Override
    //public void onBackPressed() {

    //}
    @Override
    protected void onDestroy()
    {
        //
        super.onDestroy();
    }

}

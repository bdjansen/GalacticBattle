package edu.wisc.ece.galacticbattle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
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
    private byte[] bytePacket = new byte[12];
    GameActivity activity = this;

    private SensorManager sensorManager;

    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    private ArrayList<Bullet> enemyBullets = new ArrayList<Bullet>();

    public boolean canShoot = true;
    public boolean invulnerable = false;
    public boolean enemyInvulnerable = false;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] x = (byte[]) msg.obj;
            ByteBuffer reader = ByteBuffer.wrap(x);
            float shipX = reader.getFloat();
            int bullet = reader.getInt(8);
            if(bullet == 1) {
                    ImageView v = new ImageView(activity);
                    Bullet shot = new Bullet(enemyShip.getX(), enemyShip.getY() - enemyShip.getHeightRadius(), v);
                    shot.setSource();
                    v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
                    v.setY((shot.getY() - shot.getHeightRadius()) * maxY);
                    v.setLayoutParams(new RelativeLayout.LayoutParams((int)(shot.getWidthRadius() * 2 * maxX),
                            (int)(shot.getHeightRadius()* 2 * maxY)));
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
                    layout.addView(shot.image());
                    enemyBullets.add(shot);
              }
              if(shipX > 0) {
                  ImageView shipView = enemyShip.image();
                  enemyShip.setX(shipX);
                  shipView.setX((enemyShip.getX() - enemyShip.getWidthRadius()) * maxX);
              }
//            try {
//                GamePacket received = (GamePacket) deserialize(x);
//                float shipX = received.getX();
//                float bulletX = received.getBulletX();
//                if(bulletX != -1) {
//                    ImageView v = new ImageView(activity);
//                    Bullet shot = new Bullet(bulletX, enemyShip.getY() - enemyShip.getHeightRadius(), v);
//                    shot.setSource();
//                    v.setX((shot.getX() - shot.getWidthRadius()) * maxX);
//                    v.setY((shot.getY() - shot.getHeightRadius()) * maxY);
//                    v.setLayoutParams(new RelativeLayout.LayoutParams((int)(shot.getWidthRadius() * 2 * maxX),
//                            (int)(shot.getHeightRadius()* 2 * maxY)));
//                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
//                    layout.addView(shot.image());
//                    enemyBullets.add(shot);
//                }
//                if (shipX > 0) {
//                    ImageView shipView = enemyShip.image();
//                    enemyShip.setX(shipX);
//                    shipView.setX((enemyShip.getX() - enemyShip.getWidthRadius()) * maxX);
//                }
//                if (received.getHit() == 1) {
//
//                }
//            } catch(IOException e){
//                System.out.println("Couldn't Deserialize\n");
//            } catch(ClassNotFoundException e) {
//                System.out.println("Couldn't Deserialize\n");
//            }
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

        ImageView myShipV = (ImageView) findViewById(R.id.myShip);
        ImageView enemyShipV = (ImageView) findViewById(R.id.enemyShip);

        myShipV.setLayoutParams(new RelativeLayout.LayoutParams((int)(maxX * 0.1) , (int)(maxX * .1)));
        enemyShipV.setLayoutParams(new RelativeLayout.LayoutParams((int)(maxX * 0.1) , (int)(maxX * .1)));

        myShip = new Spaceship((float)0.5, (float)0.95, myShipV);//middle of ship location
        enemyShip = new Spaceship((float)0.5, (float)0.05, enemyShipV);

        myShipV.setX(maxX * (myShip.getX() - myShip.getWidthRadius()));
        myShipV.setY(maxY * (myShip.getY() - myShip.getWidthRadius()));
        enemyShipV.setX(maxX * (enemyShip.getX() - enemyShip.getWidthRadius()));
        enemyShipV.setY(maxY * (enemyShip.getY() - enemyShip.getWidthRadius()));

        System.out.println("SEARCH THIS: MY VIEW SHIP (x,y) = (" + myShipV.getX() + "," + myShipV.getY() + ")");
        System.out.println("SEARCH THIS: ENEMY VIEW SHIP (x,y) = (" + enemyShipV.getX() + "," + enemyShipV.getY() + ")");

        spaceInvaders = new ArrayList<SpaceInvader>();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RelativeLayout gameLayout = (RelativeLayout) findViewById(R.id.layout);

        for (int i = 0; i < 8; i++) {
            ImageView currInvader = (ImageView) inflater.inflate(R.layout.space_invader_view, null);
            currInvader.setId(1000 + i);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int)(maxX * 0.1), (int)(maxX * 0.1));
            currInvader.setLayoutParams(params);
            gameLayout.addView(currInvader);
            SpaceInvader newInvader = new SpaceInvader((((float)i) / 8) + (float)0.07, (float) 0.5, currInvader);//0.07 is a magic number
            spaceInvaders.add(newInvader);
            //currInvader.layout((int) (maxX * newInvader.getX() + (maxX / 32)), (int) (maxY * newInvader.getY() + maxX / 20),
            //        (int) (maxX * newInvader.getX() + (maxX * 3 / 32)), (int) (maxY * newInvader.getY() - maxX / 20));
            currInvader.setX((int)(maxX * (newInvader.getX() - newInvader.getWidthRadius())));
            currInvader.setY((int)(maxY * (newInvader.getY() - newInvader.getHeightRadius())));
            currInvader.requestLayout();
        }

        loadUserData();

        ActionBar bar = getSupportActionBar();
        try {
            bar.hide();
        } catch (NullPointerException e) {

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
                        if (!canShoot) {
                            Thread.sleep(1000);
                            canShoot = true;
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        };

//        final Thread invulnerableThread = new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    while (true) {
//                        if (invulnerable) {
//                            Thread.sleep(2500);
//                            invulnerable = false;
//                        }
//                    }
//                } catch (InterruptedException e) {
//                }
//            }
//        };
//
//        Thread enemyInvulnerableThread = new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    while (true) {
//                        if (enemyInvulnerable) {
//                            Thread.sleep(2500);
//                            enemyInvulnerable = false;
//                        }
//                    }
//                } catch (InterruptedException e) {
//                }
//            }
//        };

        shootTimer.start();
//        invulnerableThread.start();
//        enemyInvulnerableThread.start();

        Thread bulletLogic = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Bullet current[] = new Bullet[bullets.size()];
//                                bullets.toArray(current);
//                                for (int i = 0; i < current.length; i++) {
//                                    current[i].move();
//                                    ImageView movedImage = current[i].image();
//                                    movedImage.setY((current[i].getY() - current[i].getHeightRadius())*maxY);
//                                    if (current[i].getY() < 0) {
//                                        RelativeLayout layout =
//                                                (RelativeLayout) findViewById(R.id.layout);
//                                        layout.removeView(current[i].image());
//                                        bullets.remove(current[i]);
//                                    }
//
//                                }
//                                Bullet currentEnemy[] = new Bullet[enemyBullets.size()];
//                                enemyBullets.toArray(currentEnemy);
//                                for (int i = 0; i < currentEnemy.length; i++) {
//                                    currentEnemy[i].moveEnemy();
//                                    ImageView movedImage = currentEnemy[i].image();
//                                    movedImage.setY((currentEnemy[i].getY() - currentEnemy[i].getHeightRadius())*maxY);
//                                    if (currentEnemy[i].getY() > maxY) {
//                                        RelativeLayout layout =
//                                                (RelativeLayout) findViewById(R.id.layout);
//                                        layout.removeView(currentEnemy[i].image());
//                                        enemyBullets.remove(currentEnemy[i]);
//                                    }
//                                }
//                                enemyHit();
//                                shipHit();
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
                    int countWrite = 0;
                    while (true) {
                        countWrite++;
                        Thread.sleep(1);
                        if(countWrite % 15 == 0) {
                            float value = 1 - myShip.getX();
                            byte[] location = ByteBuffer.allocate(8).putFloat(value).array();
                            for(int i = 0; i < 8; i++) {
                                bytePacket[i] = location[i];
                            }
                            connectionThread.write(bytePacket);
                            for(int i = 0; i < 4; i++) {
                                bytePacket[i + 8] = 0;
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

                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
                    layout.addView(shot.image());
                    bullets.add(shot);
                } else {
                    System.out.println("Tapped but didn't shoot");
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

//                // Ship blink code
//                final Animation blinking = new AlphaAnimation(1, 0);
//                blinking.setDuration(500);
//                blinking.setInterpolator(new LinearInterpolator());
//                blinking.setRepeatCount(5);
//                blinking.setRepeatMode(Animation.REVERSE);

                Bullet arrayBullets[] = new Bullet[enemyBullets.size()];
                enemyBullets.toArray(arrayBullets);
                for (Bullet current : arrayBullets) {
                    if (myShip.isHit(current)) {
                        RelativeLayout layout =
                                (RelativeLayout) findViewById(R.id.layout);
                        layout.removeView(current.image());
                        enemyBullets.remove(current);
                        if (!invulnerable) {
                            myShip.hit();
                            //myShip.ship.startAnimation(blinking);
                            invulnerable = true;
                        }
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
                // Ship blink code
//                final Animation blinking = new AlphaAnimation(1, 0);
//                blinking.setDuration(500);
//                blinking.setInterpolator(new LinearInterpolator());
//                blinking.setRepeatCount(5);
//                blinking.setRepeatMode(Animation.REVERSE);

                Bullet arrayBullets[] = new Bullet[bullets.size()];
                bullets.toArray(arrayBullets);
                for (Bullet current : arrayBullets) {
                    if (enemyShip.isHit(current)) {
                        RelativeLayout layout =
                                (RelativeLayout) findViewById(R.id.layout);
                        layout.removeView(current.image());
                        System.out.println("HIT THE ENEMY");
                        bullets.remove(current);
                        if (!enemyInvulnerable) {
                            enemyShip.hit();
                            //enemyShip.ship.startAnimation(blinking);
                            enemyInvulnerable = true;
                        }
                        if(!enemyShip.isAlive()) {
                            endGame("WON");
                        }
                    }
                    SpaceInvader arrayInvaders[] = new SpaceInvader[spaceInvaders.size()];
                    spaceInvaders.toArray(arrayInvaders);
                    for (SpaceInvader invader : arrayInvaders) {
                        if (invader.isHit(current)) {
                            RelativeLayout layout =
                                    (RelativeLayout) findViewById(R.id.layout);
                            layout.removeView(current.image());
                            System.out.println("HIT THE INVADER");
                            bullets.remove(current);
                            invader.hit();
                            if (!invader.isAlive()) {
                                layout.removeView(invader.image());
                                spaceInvaders.remove(invader);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float speed = 0;

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

            ImageView shipView = myShip.image();
            if (x > 0 && Math.abs(x) > 1) {
                if (myShip.getX() < (1 - myShip.getWidthRadius())) {
                    myShip.setX(myShip.getX() + (speed * shipSpeed));
                    shipView.setX((myShip.getX() - myShip.getWidthRadius()) * maxX);
                }
            } else if (x <= 0 && Math.abs(x) > 1) {
                if (myShip.getX() > myShip.getWidthRadius()) {
                    myShip.setX(myShip.getX() - (speed * shipSpeed));
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

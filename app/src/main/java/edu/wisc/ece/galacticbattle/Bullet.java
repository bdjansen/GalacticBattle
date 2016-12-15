package edu.wisc.ece.galacticbattle;

import android.widget.ImageView;

/**
 * Created by Blake on 10/17/2016.
 * This is the class for the bullet object, which all ships and space invaders shoot.
 * The main points of this class is that it has location variables, size variables,
 * an image associated with it, and the ability to move.
 */

public class Bullet {
    // Private variables
    private float x;
    private float y;
    private float widthRadius;
    private float heightRadius;
    private ImageView bullet;

    // Constructor
    public Bullet(float x, float y, ImageView bullet) {
        this.x = x;
        this.y = y;
        this.widthRadius = (float)0.00625;//1/160
        this.heightRadius = (float)0.0375;//6 times as tall
        this.bullet = bullet;
    }

    // Location variables
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    // Size variables
    public float getWidthRadius() { return widthRadius; }
    public float getHeightRadius() { return heightRadius; }

    // Move functions to shoot up (user) or down (enemies
    public void move() {
        this.y = this.y - (float)0.0005;
    }
    public void moveEnemy() {
        this.y = this.y + (float)0.0005;
    }

    // Image functions to draw on screen
    public void setSource() { bullet.setImageResource(R.drawable.laser_bullet); }
    public ImageView image() { return bullet; }
}

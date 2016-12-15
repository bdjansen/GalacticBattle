package edu.wisc.ece.galacticbattle;

import android.widget.ImageView;

/**
 * Created by Blake on 10/17/2016.
 *  * This is the class for the ship object, which is the user and enemy in 2 player.
 * The main points of this class is that it has location variables, size variables,
 * an image associated with it, hit detection, and lives system.
 */

public class Spaceship {
    // Private variables
    private float x;
    private float y;
    private float widthRadius;
    private float heightRadius;
    private int lives = 3;
    private boolean canHit = true;
    public ImageView ship;

    // Constructor
    public Spaceship(float x, float y, ImageView v) {
        this.x = x;
        this.y = y;
        this.widthRadius = (float)0.05;//1/20
        this.heightRadius = (float)0.05;//1/20
        this.ship = v;
    }

    // Location variables
    // Only need to set X because the ships move side to side
    public void setX(float x) {
        this.x = x;
    }
    public float getX(){
        return this.x;
    }
    public float getY() {
        return this.y;
    }

    // Size variables
    public float getWidthRadius() { return widthRadius; }
    public float getHeightRadius() { return heightRadius; }

    // Hit detection algorithms in order let ships get hit by bullets
    public boolean isHit(Bullet b)
    {
        if(b.getX() > x - widthRadius - b.getWidthRadius()
                && b.getX() < x + widthRadius + b.getWidthRadius()
                && b.getY() > y - heightRadius - b.getHeightRadius()
                && b.getY() < y + heightRadius - (b.getHeightRadius()/2)){//don't know why we need to subtract and use /2 here, but it looks the best
            return true;
        }
        return false;
    }
    public boolean canHit() {
        return canHit;
    }
    public void setHit(boolean bool) {
        canHit = bool;
    }

    // Lives system so we know who wins
    public void hit()
    {
        lives = lives - 1;
    }
    public boolean isAlive() {
        return lives > 0;
    }

    // Image variables to set our on screen picture
    public void setSource(int id)
    {
        ship.setImageResource(id);
    }
    public ImageView image() {return this.ship;}
}

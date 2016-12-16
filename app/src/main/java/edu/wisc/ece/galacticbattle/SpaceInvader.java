package edu.wisc.ece.galacticbattle;

import android.widget.ImageView;

/**
 * Created by Grego on 11/10/2016.
 * This is the class for the space invader, which are the enemies for the campaign mode, and can
 * be shot in the 2 player mode.  Mainly in campagin, they can move around and shoot at the player,
 * and the only way to win is to kill them all.
 */

public class SpaceInvader {
    // Class fields
    private float x;
    private float y;
    private float widthRadius;
    private float heightRadius;
    private int lives = 1;
    private boolean right;
    private ImageView spaceInvader;

    // Constructor
    public SpaceInvader(float x, float y, ImageView v, boolean right) {
        this.x = x;
        this.y = y;
        this.widthRadius = (float)0.05;
        this.heightRadius = (float)0.05;
        this.spaceInvader = v;
        this.right = right;
    }

    // Location variables
    public float getX(){
        return this.x;
    }
    public float getY() {
        return this.y;
    }

    // Size variables
    public float getWidthRadius() { return widthRadius; }
    public float getHeightRadius() { return heightRadius; }

    // Hit detection algorithm
    public boolean isHit(Bullet b)
    {
        if(b.getX() > x - widthRadius - b.getWidthRadius()
                && b.getX() < x + widthRadius + b.getWidthRadius()
                && b.getY() > y - heightRadius - b.getHeightRadius()
                && b.getY() < y + heightRadius - b.getHeightRadius()){//don't know why we need to subtract here
            return true;
        }
        return false;
    }

    // Check if the space invader is alive
    public boolean isAlive() {
        return lives > 0;
    }
    public void hit()
    {
        lives = lives - 1;
    }

    // Create the image on the screen
    public void setSource(int id)
    {
        spaceInvader.setImageResource(id);
    }
    public ImageView image(){return this.spaceInvader;}

    // Movement algorithms
    public void moveLeft() {
        if (this.x >= 0.05)
        {
            this.x = this.x - (float) 0.00025;
        }
        else
        {
            right = true;
        }
    }
    public void moveRight() {
        if (this.x <= 0.95)
        {
            this.x = this.x + (float) 0.00025;
        }
        else
        {
            right = false;
        }
    }

    public boolean getRight() { return right; }
}


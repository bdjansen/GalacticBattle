package edu.wisc.ece.galacticbattle;

import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Blake on 10/17/2016.
 */

public class Spaceship {
    private float x;
    private float y;
    private float widthRadius;
    private float heightRadius;
    private int lives = 3;
    private boolean canHit = true;

    public ImageView ship;

    public Spaceship(float x, float y, ImageView v) {
        this.x = x;
        this.y = y;
        this.widthRadius = (float)0.05;//1/20
        this.heightRadius = (float)0.05;//1/20
        this.ship = v;
    }

    public void setX(float x) {
        this.x = x;
    }

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

    public float getX(){
        return this.x;
    }

    public void hit()
    {
        lives = lives - 1;
    }

    public float getY() {
        return this.y;
    }

    public float getWidthRadius() { return widthRadius; }

    public float getHeightRadius() { return heightRadius; }

    public boolean isAlive() {
        return lives > 0;
    }

    public void setSource(int id)
    {
        ship.setImageResource(id);
    }

    public ImageView image() {return this.ship;}

    public boolean canHit() {
        return canHit;
    }

    public void setHit(boolean bool) {
        canHit = bool;
    }
}

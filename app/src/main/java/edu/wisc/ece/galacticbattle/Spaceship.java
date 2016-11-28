package edu.wisc.ece.galacticbattle;

import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Blake on 10/17/2016.
 */
public class Spaceship {
    private int x;
    private int y;
    private int widthRadius;
    private int heightRadius;
    private int lives = 3;
    private boolean canHit = true;

    public ImageView ship;

    public Spaceship(int x, int y, ImageView v) {
        this.x = x;
        this.y = y;
        this.widthRadius = 150;
        this.heightRadius = 150;
        this.ship = v;
    }

    public void setX(int x) {
        this.x = x - 15;
        ship.setX(x);
    }

    public boolean isHit(Bullet b)
    {
        if(b.getX() > x - b.getWidthRadius()
                && b.getX() < x + 2*widthRadius + b.getWidthRadius()
                && b.getY() > y - b.getHeightRadius()
                && b.getY() < y + heightRadius + b.getHeightRadius()){
            return true;
        }
        return false;
    }

    public float getX(){
        return ship.getX();
    }

    public void hit()
    {
        lives = lives - 1;
    }

    public float getY() {
        return ship.getY();
    }

    public int getWidthRadius() { return widthRadius; }

    public int getHeightRadius() { return heightRadius; }

    public boolean isAlive() {
        return lives > 0;
    }

    public void setSource(int id)
    {
        ship.setImageResource(id);
    }

    public boolean canHit() {
        return canHit;
    }

    public void setHit(boolean bool) {
        canHit = bool;
    }
}

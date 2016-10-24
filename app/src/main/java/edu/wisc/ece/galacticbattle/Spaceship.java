package edu.wisc.ece.galacticbattle;

import android.view.View;

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

    public View ship;

    public Spaceship(int x, int y, View v) {
        this.x = x;
        this.y = y;
        this.ship = v;
    }

    public void setX(int x) {
        this.x = x;
        ship.setX(x);
    }

    public boolean isHit(Bullet b)
    {
        if(b.getX() > x - widthRadius - b.getWidthRadius()
                && b.getX() < x + widthRadius + b.getWidthRadius()
                && b.getY() > y - heightRadius - b.getHeightRadius()
                && b.getY() < y + heightRadius + b.getHeightRadius()){
            return true;
        }

        return false;
    }

    public int getX(){
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidthRadius() { return widthRadius; }

    public int getHeightRadius() { return heightRadius; }

    public boolean isAlive() {
        return lives > 0;
    }
}

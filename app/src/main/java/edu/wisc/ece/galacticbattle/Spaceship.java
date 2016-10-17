package edu.wisc.ece.galacticbattle;

/**
 * Created by Blake on 10/17/2016.
 */
public class Spaceship {
    private int x;
    private int y;

    public Spaceship(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX(){
        return x;
    }

    public int getY() {
        return y;
    }
}

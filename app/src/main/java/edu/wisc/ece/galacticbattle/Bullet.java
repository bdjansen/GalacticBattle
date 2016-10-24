package edu.wisc.ece.galacticbattle;

/**
 * Created by Blake on 10/17/2016.
 */
public class Bullet {
    private int x;
    private int y;
    private int widthRadius;
    private int heightRadius;

    public Bullet(int x, int y, int halfWidth, int halfHeight) {
        this.x = x;
        this.y = y;
        this.widthRadius = halfWidth;
        this.heightRadius = halfHeight;
    }

    public int setY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidthRadius() { return widthRadius; }

    public int getHeightRadius() { return heightRadius; }
}

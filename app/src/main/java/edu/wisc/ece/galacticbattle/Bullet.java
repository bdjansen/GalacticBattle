package edu.wisc.ece.galacticbattle;

import android.widget.ImageView;

/**
 * Created by Blake on 10/17/2016.
 */
public class Bullet {
    private int x;
    private int y;
    private int widthRadius;
    private int heightRadius;

    public ImageView bullet;

    public Bullet(int x, int y, int halfWidth, int halfHeight, ImageView bullet) {
        this.x = x;
        this.y = y;
        this.widthRadius = halfWidth;
        this.heightRadius = halfHeight;
        this.bullet = bullet;
        bullet.setX(x);
        bullet.setY(y);
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

    public void move() { y++; bullet.setY(y); };

    public void setSource() { bullet.setImageResource(R.drawable.laserBullet); }

    public ImageView image() { return bullet; }
}

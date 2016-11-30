package edu.wisc.ece.galacticbattle;

import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by Blake on 10/17/2016.
 */
public class Bullet {
    private float x;
    private float y;
    private float widthRadius;
    private float heightRadius;

    private ImageView bullet;

    public Bullet(float x, float y, ImageView bullet) {
        this.x = x;
        this.y = y;
        this.widthRadius = (float)0.00625;//1/160
        this.heightRadius = (float)0.0375;//6 times as tall
        this.bullet = bullet;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidthRadius() { return widthRadius; }

    public float getHeightRadius() { return heightRadius; }

    public void move() {
        this.y = this.y - (float)0.0005;
        /*
        bullet.setY(bullet.getY() - 1);
        y = bullet.getY();
        */
    }

    public void setSource() { bullet.setImageResource(R.drawable.laser_bullet); }

    public ImageView image() { return bullet; }
}

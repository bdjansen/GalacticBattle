package edu.wisc.ece.galacticbattle;

import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by Blake on 10/17/2016.
 */
public class Bullet {
    private int x;
    private int y;
    private int widthRadius;
    private int heightRadius;

    private ImageView bullet;

    public Bullet(int x, int y, int halfWidth, int halfHeight, ImageView bullet) {
        this.x = x;
        this.y = y;
        this.widthRadius = halfWidth;
        this.heightRadius = halfHeight;
        this.bullet = bullet;
        bullet.setX(x);
        bullet.setY(y);
        bullet.setLayoutParams(new RelativeLayout.LayoutParams(widthRadius*2,
                heightRadius*2));
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

    public void move() { y--; bullet.setY(y); };

    public void setSource() { bullet.setImageResource(R.drawable.laser_bullet); }

    public ImageView image() { return bullet; }
}

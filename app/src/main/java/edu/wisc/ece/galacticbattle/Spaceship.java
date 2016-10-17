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

    public boolean isHit(Bullet bullet)
    {
        int bulletX = bullet.getX();
        int bulletY = bullet.getY();
        int range = 5;

        if ((Math.abs(x - bulletX) <= range) && (Math.abs(y - bulletY) <= range))
        {
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
}

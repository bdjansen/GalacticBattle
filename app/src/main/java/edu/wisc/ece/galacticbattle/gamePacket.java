package edu.wisc.ece.galacticbattle;

/**
 * Created by Blake on 11/30/2016.
 * UNUSED
 * We originally created this file back when we started bluetooth, but ended up scrapping it
 */
public class gamePacket implements java.io.Serializable {
    private float x = 0.5f;
    private float bulletX = -1;
    private int hit = 0;
    private int lost = 0;

    public gamePacket() {
    }

    public void setX(float f) {
        x = f;
    }

    public void setBulletX(float f) {
        bulletX = f;
    }

    public void setHit(int i) {
        hit = i;
    }

    public void setLost(int i) {
        lost = i;
    }

    public float getX() {
        return x;
    }

    public float getBulletX() {
        return bulletX;
    }

    public int getHit() {
        return hit;
    }

    public int getLost() {
        return lost;
    }
}

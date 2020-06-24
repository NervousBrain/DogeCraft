package net.waals.dogecraft.models;

public class Point {

    private int x;
    private int z;
    private int y;

    public Point(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public int getY() {
        return this.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public boolean isEqual(Point p) {
        return (p.getX() == this.x && p.getZ() == this.z && p.getY() == this.y);
    }

}

package net.waals.dogecraft.models;


import static java.lang.Math.abs;

public class Area {

    private Point p1;
    private Point p2;

    public Point getDistance() {
        Point p = new Point(abs(p2.getX() - p1.getX()), abs(p2.getZ() - p1.getZ()));
        return p;
    }

    public Area(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Point getP1() {
        return this.p1;
    }

    public Point getP2() {
        return this.p2;
    }

    public void setP1(Point point) {
        this.p1 = p1;
    }

    public void setP2(Point point) {
        this.p2 = point;
    }

    public boolean isEqual(Area area) {
        return this.p1.isEqual(area.getP1()) && this.p2.isEqual(area.getP2());
    }

}

package hlt;

import com.github.davidmoten.rtree.geometry.Circle;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

public class Entity extends Position {

    private final int owner;
    private final int id;
    private final int health;
    private final double radius;
    private final Circle circle;

    public Entity(final int owner, final int id, final double xPos, final double yPos, final int health, final double radius) {
        super(xPos, yPos);
        this.owner = owner;
        this.id = id;
        this.health = health;
        this.radius = radius;
        this.circle = Geometries.circle(xPos, yPos, radius);
    }

    public int getOwner() {
        return owner;
    }

    public int getId() {
        return id;
    }

    public int getHealth() {
        return health;
    }

    public double getRadius() {
        return radius;
    }

    public Circle getCircle() { return circle;}

    public Point getPoint() {
        return
    }

    @Override
    public String toString() {
        return "Entity[" +
                super.toString() +
                ", owner=" + owner +
                ", id=" + id +
                ", health=" + health +
                ", radius=" + radius +
                "]";
    }
}

import java.util.List;
import java.util.Optional;

public final class Point
{
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public boolean equals(Object other) {
        return other instanceof Point && ((Point)other).x == this.x
                && ((Point)other).y == this.y;
    }

    public int hashCode() {
        int result = 17;
        result = result * 31 + x;
        result = result * 31 + y;
        return result;
    }
    public  Optional<Entity> nearestEntity(List<Entity> entities)
    {
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        else {
            Entity nearest = entities.get(0);
            int nearestDistance = distanceSquared(nearest.getPosition());

            for (Entity other : entities) {
                int otherDistance = distanceSquared(other.getPosition());

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    public  int distanceSquared( Point other) {
        int deltaX = other.x - this.x;
        int deltaY = other.y - this.y;

        return deltaX * deltaX + deltaY * deltaY;
    }

    public  boolean adjacent( Point target) {
        return (this.x == target.x && Math.abs(this.y - target.y) == 1) || (this.y == target.y
                && Math.abs(this.x - target.x) == 1);
    }

}

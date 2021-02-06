import java.util.Optional;

public interface Movable extends Entity {
    boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler);
    Point nextPosition(WorldModel world,Point pos);

}

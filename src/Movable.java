import java.util.Optional;

public interface Movable  {
    boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler);
    Point nextPosition(WorldModel world,Point pos);

}

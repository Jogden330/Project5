import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public abstract class Movable extends Animated{

    private PathingStrategy pathStrat= new SingleStepPathingStrategy();


    public Movable(String id, Point position, List<PImage> images, int actionPeriod, int animationPeriod, int repeatCount) {
        super(id, position, images, actionPeriod, animationPeriod, repeatCount);
    }



    abstract boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler);

    public  Point nextPosition(WorldModel world, Point destPos)
    {

        List<Point> Path = pathStrat.computePath(getPosition(), destPos,
                point -> world.withinBounds(point) && !world.isOccupied(point),
                (p1, p2) -> p1.adjacent(p2),
                PathingStrategy.CARDINAL_NEIGHBORS);

        return Path.isEmpty() ? getPosition() : Path.get(0);

    }


}

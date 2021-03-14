import processing.core.PImage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Dynamite extends Animated {

    private static final int DYNAMITE_ACTION_PERIOD = 10000;
    private static final int DYNAMITE_ANIMATION_PERIOD = 600;
    private static final int DYNAMITE_ANIMATION_REPEAT_COUNT = 0;
    public static final String SAND_KEY = "sand";

    public Dynamite(
            String id,
            Point position,
            List<PImage> images)
    {

        super(id, position, images, DYNAMITE_ACTION_PERIOD, DYNAMITE_ANIMATION_PERIOD, DYNAMITE_ANIMATION_REPEAT_COUNT);

    }


    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        List<Point> neighbors = PathingStrategy.ALL_NEIGHBORS_AND_SELF.apply(this.getPosition())
                .filter(point -> world.withinBounds(point))
                .collect(Collectors.toList());
        Background sand = new Background(SAND_KEY,imageStore.getImageList( SAND_KEY));

        for(Point p: neighbors) {


            sand.setBackgroundCell( p, world);
        }

        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);



    }

}

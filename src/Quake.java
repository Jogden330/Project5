import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Quake extends Animated {

    private static final int QUAKE_ACTION_PERIOD = 1100;
    private static final int QUAKE_ANIMATION_PERIOD = 100;
    private static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;


    public Quake(
            String id,
            Point position,
            List<PImage> images)
    {

        super(id, position, images, QUAKE_ACTION_PERIOD, QUAKE_ANIMATION_PERIOD, QUAKE_ANIMATION_REPEAT_COUNT);

    }


    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
    }

}

import processing.core.PImage;

import java.util.List;

public class Dynamite extends Animated {

    private static final int DYNAMITE_ACTION_PERIOD = 10000;
    private static final int DYNAMITE_ANIMATION_PERIOD = 600;
    private static final int DYNAMITE_ANIMATION_REPEAT_COUNT = 0;


    public Dynamite(
            String id,
            Point position,
            List<PImage> images)
    {

        super(id, position, images, DYNAMITE_ACTION_PERIOD, DYNAMITE_ANIMATION_PERIOD, DYNAMITE_ANIMATION_REPEAT_COUNT);

    }


    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);

    }

}

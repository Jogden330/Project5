import processing.core.PImage;

import java.util.List;
import java.util.Optional;

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
        Optional<Entity> DynamiteTarget = world.findNearest(Miner.class, getPosition());

//        if(DynamiteTarget.isPresent() && getPosition().adjacent(DynamiteTarget.get().getPosition())) {
//
//        DynamiteTarget.setAblaze(world, scheduler, imageStore);
//        }
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);

    }

}

import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Quake implements Animated, Entity, HasAction {

    private static final int QUAKE_ACTION_PERIOD = 1100;
    private static final int QUAKE_ANIMATION_PERIOD = 100;
    private static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;


    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int animationPeriod;

    public Quake(

            String id,
            Point position,
            List<PImage> images)
    {

        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.animationPeriod = QUAKE_ANIMATION_PERIOD;
    }

    public int getAnimationPeriod() {return animationPeriod; }

    public void nextImage() {
        imageIndex = (imageIndex + 1) % images.size();
    }

    public  PImage getCurrentImage() { return images.get(imageIndex); }

    public String getId() {
        return id;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }


    public void scheduleActions(
            EventScheduler scheduler,
            WorldModel world,
            ImageStore imageStore)
    {

                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        QUAKE_ACTION_PERIOD);
                scheduler.scheduleEvent(this, EntityFactory.createAnimationAction(this,
                        QUAKE_ANIMATION_REPEAT_COUNT),
                        animationPeriod);

    }

    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
    }



}

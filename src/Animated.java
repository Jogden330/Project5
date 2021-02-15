import processing.core.PImage;

import java.util.List;

public abstract class Animated extends HasAction {
    protected int repeatCount;
    protected int animationPeriod;

    public Animated(String id, Point position,
                          List<PImage> images, int actionPeriod,
                          int animationPeriod, int repeatCount) {
        super(id, position, images, actionPeriod);
        this.animationPeriod = animationPeriod;
        this.repeatCount = repeatCount;

    }

    public int getAnimationPeriod() {return animationPeriod; }

    public void scheduleActions(
            EventScheduler scheduler,
            WorldModel world,
            ImageStore imageStore)
    {

        scheduler.scheduleEvent(this,
                EntityFactory.createActivityAction(this, world, imageStore),
                actionPeriod);
        scheduler.scheduleEvent(this,
                EntityFactory.createAnimationAction(this, 0),
                animationPeriod);

    }

    public void nextImage() {
        setImageIndex((getImageIndex() + 1) % getimages().size());
    }
}

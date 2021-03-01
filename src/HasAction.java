import processing.core.PImage;

import java.util.List;

public abstract class HasAction extends Entity{

    protected int actionPeriod;

    public HasAction(String id, Point position,
                        List<PImage> images, int actionPeriod)
    {
        super(id, position, images);
        this.actionPeriod = actionPeriod;
    }



    public int getActionPeriod() {
        return actionPeriod;
    }

    public void scheduleActions(EventScheduler scheduler,  WorldModel world, ImageStore imageStore)
    {
        scheduler.scheduleEvent(this,
                EntityFactory.createActivityAction(this, world, imageStore),
                actionPeriod);
    }

    protected abstract void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler);
}

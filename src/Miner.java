import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public abstract class Miner extends  Movable{

    protected int resourceCount;
    protected int resourceLimit;

    public Miner(String id, Point position,
                 List<PImage> images, int resourceLimit,
                 int resourceCount, int actionPeriod,
                 int animationPeriod, int repeatCount)
    {
        super(id, position, images,actionPeriod, animationPeriod, 0);
        this.resourceCount=resourceCount;
        this.resourceLimit=resourceLimit;

    }

    public boolean moveTo(

            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if(_Movehelper(world, target, scheduler)){
            return true;
        }
        else {
            Point nextPos = computePath(world, target.getPosition());

            if (!getPosition().equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant( nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents( occupant.get());
                }

                world.moveEntity(this,  nextPos);
            }
            return false;
        }
    }


    abstract boolean _Movehelper(
            WorldModel world,
            Entity target,
            EventScheduler scheduler);

    public boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore, Miner miner) {



        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        miner.scheduleActions(scheduler, world, imageStore);
        return true;

    }


    public int getResourceLimit() {
        return resourceLimit;
    }

    public int getResourceCount() {
        return resourceCount;
    }
}

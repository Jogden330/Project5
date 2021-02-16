import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MinerNotFull extends  Miner {



    public MinerNotFull (

            String id,
            Point position,
            List<PImage> images,
            int resourceLimit,
            int resourceCount,
            int actionPeriod,
            int animationPeriod)
    {

        super(id, position, images, resourceLimit, resourceCount, actionPeriod, animationPeriod, 0);
    }



    public boolean transform(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        if (resourceCount >= resourceLimit) {
            MinerFull miner = EntityFactory.createMinerFull(getId(), getResourceLimit(), getPosition(), getActionPeriod(), getAnimationPeriod(), getimages());

            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            miner.scheduleActions(scheduler,  world, imageStore);

            return true;
        }

        return false;
    }

    public boolean moveTo(

            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (getPosition().adjacent( target.getPosition())) {
            resourceCount += 1;
            world.removeEntity(target);
            scheduler.unscheduleAllEvents( target);

            return true;
        }
        else {
            Point nextPos = nextPosition(world, target.getPosition());

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

    public void executeActivity(WorldModel world,
                                            ImageStore imageStore,
                                            EventScheduler scheduler)
    {
        Optional<Entity> notFullTarget =
                world.findNearest(Ore.class, getPosition());

        if (!notFullTarget.isPresent() || !moveTo( world, notFullTarget.get(), scheduler)
                || !transform( world, scheduler, imageStore))
        {
            scheduler.scheduleEvent(this,
                    EntityFactory.createActivityAction(this, world, imageStore),
                    getActionPeriod());
        }
    }


}

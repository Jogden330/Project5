import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MinerFull extends  Miner{



    public MinerFull(
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



    public boolean transform( WorldModel world, EventScheduler scheduler, ImageStore imageStore)
    {
        MinerNotFull miner = EntityFactory.createMinerNotFull(getId(), getResourceLimit(), getPosition(), getActionPeriod(), getAnimationPeriod(), getimages());
        super.transform(world, scheduler, imageStore, miner);
        return false;
    }


    public boolean _Movehelper(
            WorldModel world,
            Entity target,
            EventScheduler scheduler){
        return  (getPosition().adjacent(target.getPosition()));

    }


    public void executeActivity(WorldModel world,
                                         ImageStore imageStore,
                                         EventScheduler scheduler)
    {
        Optional<Entity> fullTarget =
                world.findNearest(BlackSmith.class, getPosition());

        if (fullTarget.isPresent() && moveTo(world,fullTarget.get(), scheduler))
        {
            transform( world, scheduler, imageStore);
        }
        else {
            scheduler.scheduleEvent( this,
                    EntityFactory.createActivityAction(this, world, imageStore),
                    getActionPeriod());
        }
    }



}

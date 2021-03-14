import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Robot extends Movable{

    private static final String SPROUT_KEY = "sprout";

    public Robot(

            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod)
    {
        super(id, position, images, actionPeriod, animationPeriod, 0);
    }



    public  boolean  _Movehelper(
            WorldModel world,
            Entity target,
            EventScheduler scheduler) {
        if (getPosition().adjacent(target.getPosition())) {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        }
        else return false;
    }


    public void executeActivity(WorldModel world,
                                ImageStore imageStore,
                                EventScheduler scheduler)
    {
        Optional<Entity> robotTarget = world.findNearest(BlackSmith.class, getPosition());

        if (robotTarget.isPresent()) {
            Point tgtPos = robotTarget.get().getPosition();

            if (moveTo(world, robotTarget.get(), scheduler)) {
                Sprout sprout = EntityFactory.createSprout(SPROUT_KEY, tgtPos, imageStore.getImageList(SPROUT_KEY));

                world.addEntity(sprout);

            }
            else {
                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        getActionPeriod());
            }

        }

    }


    public  boolean _nextPositionHelper(WorldModel world, Point nextPos){
        Optional<Entity> occupant = world.getOccupant(nextPos);

        return  (((occupant.isPresent() && !(occupant.get() instanceof OreBlob))));

    }

}

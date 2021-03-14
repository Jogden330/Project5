import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Fire extends Movable {



    public Fire(String id, Point position,
                 List<PImage> images, int actionPeriod,
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

    public  boolean _nextPositionHelper(WorldModel world, Point nextPos) {

        return world.isOccupied(nextPos);

//        Background background = world.getBackground()[this.getPosition().x][this.getPosition().y];
//
//        return (world.isOccupied(nextPos) && (background.getID() != "dirt"));
    }

    public void executeActivity(WorldModel world,
                                ImageStore imageStore,
                                EventScheduler scheduler) {
        Background sand = new Background(Dynamite.SAND_KEY,imageStore.getImageList( Dynamite.SAND_KEY));

        sand.setBackgroundCell( this.getPosition(), world);

        Optional<Entity> fireTarget = world.findNearest(BlackSmith.class, getPosition());

        if (fireTarget.isPresent()) {
            Point tgtPos = fireTarget.get().getPosition();

            if (moveTo(world, fireTarget.get(), scheduler)) {
                Fire fire = EntityFactory.createFire(getId(), tgtPos, getActionPeriod(), getAnimationPeriod(), getimages());

                world.addEntity(fire);
                fire.scheduleActions(scheduler, world, imageStore);
            }
            else {
                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        getActionPeriod());
            }

        }
    }
}
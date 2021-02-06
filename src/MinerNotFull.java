import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MinerNotFull implements Animated, Entity, HasAction, Movable, Miner {

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int resourceLimit;
    private int resourceCount;
    private int actionPeriod;
    private int animationPeriod;

    public MinerNotFull (

            String id,
            Point position,
            List<PImage> images,
            int resourceLimit,
            int resourceCount,
            int actionPeriod,
            int animationPeriod)
    {

        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public int getAnimationPeriod() {
                return animationPeriod;
    }



    public void nextImage() {
        imageIndex = (imageIndex + 1) % images.size();
    }


    public  PImage getCurrentImage() { return images.get(imageIndex);  }

    public boolean transform(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        if (resourceCount >= resourceLimit) {
            Entity miner = EntityFactory.createMinerFull(id, resourceLimit,
                    position, actionPeriod,
                    animationPeriod,
                    images);

            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            ((HasAction)miner).scheduleActions(scheduler,  world, imageStore);

            return true;
        }

        return false;
    }
    public  Point nextPosition(WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.x - position.x);
        Point newPos = new Point(position.x + horiz, position.y);

        if (horiz == 0 || world.isOccupied(newPos)) {
            int vert = Integer.signum(destPos.y - position.y);
            newPos = new Point(position.x, position.y + vert);

            if (vert == 0 || world.isOccupied(newPos)) {
                newPos = position;
            }
        }

        return newPos;
    }


    public boolean moveTo(

            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (position.adjacent( target.getPosition())) {
            resourceCount += 1;
            world.removeEntity(target);
            scheduler.unscheduleAllEvents( target);

            return true;
        }
        else {
            Point nextPos = nextPosition(world, target.getPosition());

            if (!position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant( nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents( occupant.get());
                }

                world.moveEntity(this,  nextPos);
            }
            return false;
        }
    }

    public String getId() {
        return id;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getActionPeriod() {
        return actionPeriod;
    }

    public void scheduleActions(
            EventScheduler scheduler,
            WorldModel world,
            ImageStore imageStore)
    {

                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent( this,
                        EntityFactory.createAnimationAction(this, 0),
                        animationPeriod);

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

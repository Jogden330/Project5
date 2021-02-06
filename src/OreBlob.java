import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class OreBlob implements Animated, Entity, Movable, HasAction{


    private  final String QUAKE_KEY = "quake";

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int actionPeriod;
    private int animationPeriod;

    public OreBlob(

            String id,
            Point position,
            List<PImage> images,
            int actionPeriod,
            int animationPeriod)
    {

        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public int getAnimationPeriod() { return animationPeriod;  }

    public void nextImage() {
        imageIndex = (imageIndex + 1) % images.size();
    }

    public  PImage getCurrentImage() { return images.get(imageIndex); }

    public  boolean moveTo(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (position.adjacent(target.getPosition())) {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        }
        else {
            Point nextPos = nextPosition(world, target.getPosition());

            if (!position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents( occupant.get());
                }

                world.moveEntity(this,  nextPos);
            }
            return false;
        }
    }
    public Point nextPosition(WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.x - position.x);
        Point newPos = new Point(position.x + horiz, position.y);

        Optional<Entity> occupant = world.getOccupant( newPos);

        if (horiz == 0 || (occupant.isPresent() && !(occupant.get() instanceof Ore)))
        {
            int vert = Integer.signum(destPos.y - position.y);
            newPos = new Point(position.x, position.y + vert);
            occupant = world.getOccupant( newPos);

            if (vert == 0 || (occupant.isPresent() && !(occupant.get() instanceof Ore)))
            {
                newPos = position;
            }
        }

        return newPos;
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
                scheduler.scheduleEvent(this,
                        EntityFactory.createAnimationAction(this, 0),
                        animationPeriod);

    }

    public void executeActivity(WorldModel world,
                                       ImageStore imageStore,
                                       EventScheduler scheduler)
    {
        Optional<Entity> blobTarget = world.findNearest(Vein.class, getPosition());
        long nextPeriod = getActionPeriod();

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().getPosition() ;

            if (moveTo( world, blobTarget.get(), scheduler)) {
                Entity quake = EntityFactory.createQuake(tgtPos,
                        imageStore.getImageList(QUAKE_KEY));

                world.addEntity(quake);
                nextPeriod += getActionPeriod();
                ((HasAction)quake).scheduleActions(scheduler, world, imageStore);
            }
        }

        scheduler.scheduleEvent( this,
                EntityFactory.createActivityAction(this, world, imageStore),
                nextPeriod);
    }




}

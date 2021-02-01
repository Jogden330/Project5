import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public final class Entity
{

    private  final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

    private EntityKind kind;
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int resourceLimit;
    private int resourceCount;
    private int actionPeriod;
    private int animationPeriod;

    public Entity(
            EntityKind kind,
            String id,
            Point position,
            List<PImage> images,
            int resourceLimit,
            int resourceCount,
            int actionPeriod,
            int animationPeriod)
    {
        this.kind = kind;
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
        switch (kind) {
            case MINER_FULL:
            case MINER_NOT_FULL:
            case ORE_BLOB:
            case QUAKE:
                return animationPeriod;
            default:
                throw new UnsupportedOperationException(
                        String.format("getAnimationPeriod not supported for %s", kind));
        }
    }



    public void nextImage() {
        imageIndex = (imageIndex + 1) % images.size();
    }




    public  PImage getCurrentImage() {

            return images.get(imageIndex);


    }

    public  void transformFull( WorldModel world, EventScheduler scheduler, ImageStore imageStore)
    {
        Entity miner = Functions.createMinerNotFull(id, resourceLimit, position, actionPeriod, animationPeriod, images);

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        miner.scheduleActions(scheduler, world, imageStore);
    }

    public boolean transformNotFull(
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        if (resourceCount >= resourceLimit) {
            Entity miner = Functions.createMinerFull(id, resourceLimit,
                    position, actionPeriod,
                    animationPeriod,
                    images);

            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            miner.scheduleActions(scheduler,  world, imageStore);

            return true;
        }

        return false;
    }
    public  Point nextPositionMiner(WorldModel world, Point destPos)
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


    public  boolean moveToOreBlob(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (position.adjacent(target.position)) {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        }
        else {
            Point nextPos = nextPositionOreBlob(world, target.position);

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

    public Point nextPositionOreBlob(WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.x - position.x);
        Point newPos = new Point(position.x + horiz, position.y);

        Optional<Entity> occupant = world.getOccupant( newPos);

        if (horiz == 0 || (occupant.isPresent() && !(occupant.get().kind
                == EntityKind.ORE)))
        {
            int vert = Integer.signum(destPos.y - position.y);
            newPos = new Point(position.x, position.y + vert);
            occupant = world.getOccupant( newPos);

            if (vert == 0 || (occupant.isPresent() && !(occupant.get().kind
                    == EntityKind.ORE)))
            {
                newPos = position;
            }
        }

        return newPos;
    }

    public boolean moveToNotFull(

            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (position.adjacent( target.position)) {
            resourceCount += 1;
            world.removeEntity(target);
            scheduler.unscheduleAllEvents( target);

            return true;
        }
        else {
            Point nextPos = nextPositionMiner(world, target.position);

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


    public boolean moveToFull(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (position.adjacent(target.position)) {
            return true;
        }
        else {
            Point nextPos = nextPositionMiner(world, target.position);

            if (!position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant( nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }



    public EntityKind getKind() {
        return kind;
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
        switch (kind) {
            case MINER_FULL:
                scheduler.scheduleEvent(this,
                        Functions.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent(this,
                        Functions.createAnimationAction(this, 0),
                        animationPeriod);
                break;

            case MINER_NOT_FULL:
                scheduler.scheduleEvent(this,
                        Functions.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent( this,
                        Functions.createAnimationAction(this, 0),
                        animationPeriod);
                break;

            case ORE:
                scheduler.scheduleEvent(this,
                        Functions.createActivityAction(this, world, imageStore),
                        actionPeriod);
                break;

            case ORE_BLOB:
                scheduler.scheduleEvent(this,
                        Functions.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent(this,
                        Functions.createAnimationAction(this, 0),
                        animationPeriod);
                break;

            case QUAKE:
                scheduler.scheduleEvent(this,
                        Functions.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent(this, Functions.createAnimationAction(this,
                        QUAKE_ANIMATION_REPEAT_COUNT),
                        animationPeriod);
                break;

            case VEIN:
                scheduler.scheduleEvent(this,
                        Functions.createActivityAction(this, world, imageStore),
                        actionPeriod);
                break;

            default:
        }
    }

    public void executeMinerFullActivity(WorldModel world,
                                         ImageStore imageStore,
                                         EventScheduler scheduler)
    {
        Optional<Entity> fullTarget =
                findNearest(world, EntityKind.BLACKSMITH);

        if (fullTarget.isPresent() && moveToFull(world,
                fullTarget.get(), scheduler))
        {
            transformFull( world, scheduler, imageStore);
        }
        else {
            scheduler.scheduleEvent( this,
                    Functions.createActivityAction(this, world, imageStore),
                    getActionPeriod());
        }
    }

    public void executeMinerNotFullActivity(WorldModel world,
                                              ImageStore imageStore,
                                              EventScheduler scheduler)
    {
        Optional<Entity> notFullTarget =
                findNearest(world, EntityKind.ORE);

        if (!notFullTarget.isPresent() || !moveToNotFull( world, notFullTarget.get(), scheduler)
                || !transformNotFull( world, scheduler, imageStore))
        {
            scheduler.scheduleEvent(this,
                    Functions.createActivityAction(this, world, imageStore),
                    getActionPeriod());
        }
    }



    private  Optional<Entity> findNearest( WorldModel world, EntityKind kind)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : world.getEntities()) {
            if (entity.getKind() == kind) {
                ofType.add(entity);
            }
        }

        return getPosition().nearestEntity(ofType);
    }
}

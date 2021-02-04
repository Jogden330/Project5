import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MinerNotFull implements Animated {
    private  final int QUAKE_ANIMATION_REPEAT_COUNT = 10;
    private final String BLOB_KEY = "blob";
    private final String BLOB_ID_SUFFIX = " -- blob";
    private final int BLOB_PERIOD_SCALE = 4;
    private final int BLOB_ANIMATION_MIN = 50;
    private final int BLOB_ANIMATION_MAX = 150;

    private final String ORE_ID_PREFIX = "ore -- ";
    private final int ORE_CORRUPT_MIN = 20000;
    private final int ORE_CORRUPT_MAX = 30000;

    private  final String QUAKE_KEY = "quake";
    private  final String ORE_KEY = "ore";


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


    public  PImage getCurrentImage() {

        return images.get(imageIndex);


    }

    public  void transformFull( WorldModel world, EventScheduler scheduler, ImageStore imageStore)
    {
        Entity miner = EntityFactory.createMinerNotFull(id, resourceLimit, position, actionPeriod, animationPeriod, images);

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
            Entity miner = EntityFactory.createMinerFull(id, resourceLimit,
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
                        EntityFactory.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent(this,
                        EntityFactory.createAnimationAction(this, 0),
                        animationPeriod);
                break;

            case MINER_NOT_FULL:
                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent( this,
                        EntityFactory.createAnimationAction(this, 0),
                        animationPeriod);
                break;

            case ORE:
                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        actionPeriod);
                break;

            case ORE_BLOB:
                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent(this,
                        EntityFactory.createAnimationAction(this, 0),
                        animationPeriod);
                break;

            case QUAKE:
                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        actionPeriod);
                scheduler.scheduleEvent(this, EntityFactory.createAnimationAction(this,
                        QUAKE_ANIMATION_REPEAT_COUNT),
                        animationPeriod);
                break;

            case VEIN:
                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
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
                    EntityFactory.createActivityAction(this, world, imageStore),
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
                    EntityFactory.createActivityAction(this, world, imageStore),
                    getActionPeriod());
        }
    }

    public void executeOreActivity(WorldModel world,
                                   ImageStore imageStore,
                                   EventScheduler scheduler)
    {
        Point pos = getPosition();

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        Entity blob = EntityFactory.createOreBlob(getId() + BLOB_ID_SUFFIX, pos,
                getActionPeriod() / BLOB_PERIOD_SCALE,
                BLOB_ANIMATION_MIN + EntityFactory.rand.nextInt(
                        BLOB_ANIMATION_MAX
                                - BLOB_ANIMATION_MIN),
                imageStore.getImageList(BLOB_KEY));

        world.addEntity(blob);
        blob.scheduleActions(scheduler,  world, imageStore);
    }

    public void executeOreBlobActivity(WorldModel world,
                                       ImageStore imageStore,
                                       EventScheduler scheduler)
    {
        Optional<Entity> blobTarget =
                findNearest(world, EntityKind.VEIN);
        long nextPeriod = getActionPeriod();

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().getPosition() ;

            if (moveToOreBlob( world, blobTarget.get(), scheduler)) {
                Entity quake = EntityFactory.createQuake(tgtPos,
                        imageStore.getImageList(QUAKE_KEY));

                world.addEntity(quake);
                nextPeriod += getActionPeriod();
                quake.scheduleActions(scheduler, world, imageStore);
            }
        }

        scheduler.scheduleEvent( this,
                EntityFactory.createActivityAction(this, world, imageStore),
                nextPeriod);
    }

    public void executeQuakeActivity(WorldModel world, EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
    }

    public void executeVeinActivity(WorldModel world,
                                    ImageStore imageStore,
                                    EventScheduler scheduler)
    {
        Optional<Point> openPt = world.findOpenAround(getPosition());

        if (openPt.isPresent()) {
            Entity ore = EntityFactory.createOre(ORE_ID_PREFIX + getId(), openPt.get(),
                    ORE_CORRUPT_MIN + EntityFactory.rand.nextInt(
                            ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
                    imageStore.getImageList(ORE_KEY));
            world.addEntity(ore);
            ore.scheduleActions(scheduler, world, imageStore);
        }

        scheduler.scheduleEvent( this,
                EntityFactory.createActivityAction(this, world, imageStore),
                getActionPeriod());
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

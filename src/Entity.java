import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public final class Entity
{
    public static final String BLOB_KEY = "blob";
    public static final String BLOB_ID_SUFFIX = " -- blob";
    public static final int BLOB_PERIOD_SCALE = 4;
    public static final int BLOB_ANIMATION_MIN = 50;
    public static final int BLOB_ANIMATION_MAX = 150;

    public static final String ORE_ID_PREFIX = "ore -- ";
    public static final int ORE_CORRUPT_MIN = 20000;
    public static final int ORE_CORRUPT_MAX = 30000;
    public static final int ORE_REACH = 1;

    public static final String QUAKE_KEY = "quake";
    public static final String QUAKE_ID = "quake";
    public static final int QUAKE_ACTION_PERIOD = 1100;
    public static final int QUAKE_ANIMATION_PERIOD = 100;
    public static final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

    public static final int COLOR_MASK = 0xffffff;
    public static final int KEYED_IMAGE_MIN = 5;
    public static final int KEYED_RED_IDX = 2;
    public static final int KEYED_GREEN_IDX = 3;
    public static final int KEYED_BLUE_IDX = 4;

    public static final int PROPERTY_KEY = 0;

    public static final String MINER_KEY = "miner";
    public static final int MINER_NUM_PROPERTIES = 7;
    public static final int MINER_ID = 1;
    public static final int MINER_COL = 2;
    public static final int MINER_ROW = 3;
    public static final int MINER_LIMIT = 4;
    public static final int MINER_ACTION_PERIOD = 5;
    public static final int MINER_ANIMATION_PERIOD = 6;

    public static final String OBSTACLE_KEY = "obstacle";
    public static final int OBSTACLE_NUM_PROPERTIES = 4;
    public static final int OBSTACLE_ID = 1;
    public static final int OBSTACLE_COL = 2;
    public static final int OBSTACLE_ROW = 3;

    public static final String ORE_KEY = "ore";
    public static final int ORE_NUM_PROPERTIES = 5;
    public static final int ORE_ID = 1;
    public static final int ORE_COL = 2;
    public static final int ORE_ROW = 3;
    public static final int ORE_ACTION_PERIOD = 4;

    public static final String SMITH_KEY = "blacksmith";
    public static final int SMITH_NUM_PROPERTIES = 4;
    public static final int SMITH_ID = 1;
    public static final int SMITH_COL = 2;
    public static final int SMITH_ROW = 3;

    public static final String VEIN_KEY = "vein";
    public static final int VEIN_NUM_PROPERTIES = 5;
    public static final int VEIN_ID = 1;
    public static final int VEIN_COL = 2;
    public static final int VEIN_ROW = 3;
    public static final int VEIN_ACTION_PERIOD = 4;

    public EntityKind kind;
    public String id;
    public Point position;
    public List<PImage> images;
    public int imageIndex;
    public int resourceLimit;
    public int resourceCount;
    public int actionPeriod;
    public int animationPeriod;

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
    public void addEntity(WorldModel world) {
        if (world.withinBounds(position)) {
            world.setOccupancyCell( position, this);
            world.entities.add(this);
        }
    }

    public  void moveEntity(WorldModel world,  Point pos) {
        Point oldPos = position;
        if (world.withinBounds( pos) && !pos.equals(oldPos)) {
            world.setOccupancyCell(oldPos, null);
            removeEntityAt(world, pos);
            world.setOccupancyCell(pos,this);
            position = pos;
        }
    }

    public  void removeEntity(WorldModel world ) {
        removeEntityAt(world, position);
    }

    private void removeEntityAt(WorldModel world, Point pos) {
        if (world.withinBounds(pos) && world.getOccupancyCell(pos) != null) {
            Entity entity = world.getOccupancyCell(pos);

            /* This moves the entity just outside of the grid for
             * debugging purposes. */
            entity.position = new Point(-1, -1);
            world.entities.remove(entity);
            world.setOccupancyCell( pos, null);
        }
    }

    public  PImage getCurrentImage() {

            return images.get(imageIndex);


    }

    public  void transformFull( WorldModel world, EventScheduler scheduler, ImageStore imageStore)
    {
        Entity miner = Functions.createMinerNotFull(id, resourceLimit, position, actionPeriod, animationPeriod, images);

        removeEntity(world);
        scheduler.unscheduleAllEvents(this);

        miner.addEntity(world);
        scheduler.scheduleActions(miner, world, imageStore);
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

            removeEntity(world);
            scheduler.unscheduleAllEvents(this);

            miner.addEntity(world);
            scheduler.scheduleActions(miner,  world, imageStore);

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
        if (Functions.adjacent(position, target.position)) {
            target.removeEntity(world);
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

                moveEntity(world,  nextPos);
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
        if (Functions.adjacent(position, target.position)) {
            resourceCount += 1;
            target.removeEntity(world);
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

                moveEntity(world,  nextPos);
            }
            return false;
        }
    }


    public boolean moveToFull(

            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (Functions.adjacent(position, target.position)) {
            return true;
        }
        else {
            Point nextPos = nextPositionMiner(world, target.position);

            if (!position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant( nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                moveEntity(world, nextPos);
            }
            return false;
        }
    }
}

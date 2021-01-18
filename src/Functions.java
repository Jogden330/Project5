import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

import processing.core.PImage;
import processing.core.PApplet;

public final class Functions
{
    public static final Random rand = new Random();



    public static PImage getCurrentImage(Object entity) {
        if (entity instanceof Background) {
            return ((Background)entity).images.get(
                    ((Background)entity).imageIndex);
        }
        else if (entity instanceof Entity) {
            return ((Entity)entity).images.get(((Entity)entity).imageIndex);
        }
        else {
            throw new UnsupportedOperationException(
                    String.format("getCurrentImage not supported for %s",
                                  entity));
        }
    }


    public static boolean transformNotFull(
            Entity entity,
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        if (entity.resourceCount >= entity.resourceLimit) {
            Entity miner = createMinerFull(entity.id, entity.resourceLimit,
                                           entity.position, entity.actionPeriod,
                                           entity.animationPeriod,
                                           entity.images);

            entity.removeEntity(world);
            scheduler.unscheduleAllEvents(entity);

            miner.addEntity(world);
            scheduler.scheduleActions(miner,  world, imageStore);

            return true;
        }

        return false;
    }

    public static void transformFull(
            Entity entity,
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        Entity miner = createMinerNotFull(entity.id, entity.resourceLimit,
                                          entity.position, entity.actionPeriod,
                                          entity.animationPeriod,
                                          entity.images);

        entity.removeEntity(world);
        scheduler.unscheduleAllEvents(entity);

        miner.addEntity(world);
        scheduler.scheduleActions(miner, world, imageStore);
    }

    public static boolean moveToNotFull(
            Entity miner,
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (adjacent(miner.position, target.position)) {
            miner.resourceCount += 1;
            target.removeEntity(world);
            scheduler.unscheduleAllEvents( target);

            return true;
        }
        else {
            Point nextPos = nextPositionMiner(miner, world, target.position);

            if (!miner.position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant( nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents( occupant.get());
                }

                miner.moveEntity(world,  nextPos);
            }
            return false;
        }
    }

    public static boolean moveToFull(
            Entity miner,
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (adjacent(miner.position, target.position)) {
            return true;
        }
        else {
            Point nextPos = nextPositionMiner(miner, world, target.position);

            if (!miner.position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant( nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                miner.moveEntity(world, nextPos);
            }
            return false;
        }
    }

    public static boolean moveToOreBlob(
            Entity blob,
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (adjacent(blob.position, target.position)) {
            target.removeEntity(world);
            scheduler.unscheduleAllEvents(target);
            return true;
        }
        else {
            Point nextPos = nextPositionOreBlob(blob, world, target.position);

            if (!blob.position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents( occupant.get());
                }

                blob.moveEntity(world,  nextPos);
            }
            return false;
        }
    }

    public static Point nextPositionMiner(
            Entity entity, WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.x - entity.position.x);
        Point newPos = new Point(entity.position.x + horiz, entity.position.y);

        if (horiz == 0 || isOccupied(world, newPos)) {
            int vert = Integer.signum(destPos.y - entity.position.y);
            newPos = new Point(entity.position.x, entity.position.y + vert);

            if (vert == 0 || isOccupied(world, newPos)) {
                newPos = entity.position;
            }
        }

        return newPos;
    }

    public static Point nextPositionOreBlob(
            Entity entity, WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.x - entity.position.x);
        Point newPos = new Point(entity.position.x + horiz, entity.position.y);

        Optional<Entity> occupant = world.getOccupant( newPos);

        if (horiz == 0 || (occupant.isPresent() && !(occupant.get().kind
                == EntityKind.ORE)))
        {
            int vert = Integer.signum(destPos.y - entity.position.y);
            newPos = new Point(entity.position.x, entity.position.y + vert);
            occupant = world.getOccupant( newPos);

            if (vert == 0 || (occupant.isPresent() && !(occupant.get().kind
                    == EntityKind.ORE)))
            {
                newPos = entity.position;
            }
        }

        return newPos;
    }

    public static boolean adjacent(Point p1, Point p2) {
        return (p1.x == p2.x && Math.abs(p1.y - p2.y) == 1) || (p1.y == p2.y
                && Math.abs(p1.x - p2.x) == 1);
    }

    public static Optional<Point> findOpenAround(WorldModel world, Point pos) {
        for (int dy = -Entity.ORE_REACH; dy <= Entity.ORE_REACH; dy++) {
            for (int dx = -Entity.ORE_REACH; dx <= Entity.ORE_REACH; dx++) {
                Point newPt = new Point(pos.x + dx, pos.y + dy);
                if (world.withinBounds( newPt) && !isOccupied(world, newPt)) {
                    return Optional.of(newPt);
                }
            }
        }

        return Optional.empty();
    }

    public static List<PImage> getImageList(ImageStore imageStore, String key) {
        return imageStore.images.getOrDefault(key, imageStore.defaultImages);
    }

    public static void loadImages(
            Scanner in, ImageStore imageStore, PApplet screen)
    {
        int lineNumber = 0;
        while (in.hasNextLine()) {
            try {
                processImageLine(imageStore.images, in.nextLine(), screen);
            }
            catch (NumberFormatException e) {
                System.out.println(
                        String.format("Image format error on line %d",
                                      lineNumber));
            }
            lineNumber++;
        }
    }

    public static void processImageLine(
            Map<String, List<PImage>> images, String line, PApplet screen)
    {
        String[] attrs = line.split("\\s");
        if (attrs.length >= 2) {
            String key = attrs[0];
            PImage img = screen.loadImage(attrs[1]);
            if (img != null && img.width != -1) {
                List<PImage> imgs = getImages(images, key);
                imgs.add(img);

                if (attrs.length >= Entity.KEYED_IMAGE_MIN) {
                    int r = Integer.parseInt(attrs[Entity.KEYED_RED_IDX]);
                    int g = Integer.parseInt(attrs[Entity.KEYED_GREEN_IDX]);
                    int b = Integer.parseInt(attrs[Entity.KEYED_BLUE_IDX]);
                    setAlpha(img, screen.color(r, g, b), 0);
                }
            }
        }
    }

    public static List<PImage> getImages(
            Map<String, List<PImage>> images, String key)
    {
        List<PImage> imgs = images.get(key);
        if (imgs == null) {
            imgs = new LinkedList<>();
            images.put(key, imgs);
        }
        return imgs;
    }

    /*
      Called with color for which alpha should be set and alpha value.
      setAlpha(img, color(255, 255, 255), 0));
    */
    public static void setAlpha(PImage img, int maskColor, int alpha) {
        int alphaValue = alpha << 24;
        int nonAlpha = maskColor & Entity.COLOR_MASK;
        img.format = PApplet.ARGB;
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++) {
            if ((img.pixels[i] & Entity.COLOR_MASK) == nonAlpha) {
                img.pixels[i] = alphaValue | nonAlpha;
            }
        }
        img.updatePixels();
    }




    public static void load(
            Scanner in, WorldModel world, ImageStore imageStore)
    {
        int lineNumber = 0;
        while (in.hasNextLine()) {
            try {
                if (!processLine(in.nextLine(), world, imageStore)) {
                    System.err.println(String.format("invalid entry on line %d",
                                                     lineNumber));
                }
            }
            catch (NumberFormatException e) {
                System.err.println(
                        String.format("invalid entry on line %d", lineNumber));
            }
            catch (IllegalArgumentException e) {
                System.err.println(
                        String.format("issue on line %d: %s", lineNumber,
                                      e.getMessage()));
            }
            lineNumber++;
        }
    }

    public static boolean processLine(
            String line, WorldModel world, ImageStore imageStore)
    {
        String[] properties = line.split("\\s");
        if (properties.length > 0) {
            switch (properties[Entity.PROPERTY_KEY]) {
                case Background.BGND_KEY:
                    return parseBackground(properties, world, imageStore);
                case Entity.MINER_KEY:
                    return parseMiner(properties, world, imageStore);
                case Entity.OBSTACLE_KEY:
                    return parseObstacle(properties, world, imageStore);
                case Entity.ORE_KEY:
                    return parseOre(properties, world, imageStore);
                case Entity.SMITH_KEY:
                    return parseSmith(properties, world, imageStore);
                case Entity.VEIN_KEY:
                    return parseVein(properties, world, imageStore);
            }
        }

        return false;
    }

    public static boolean parseBackground(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Background.BGND_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Background.BGND_COL]),
                                 Integer.parseInt(properties[Background.BGND_ROW]));
            String id = properties[Background.BGND_ID];
            world.setBackground( pt,
                          new Background(id, getImageList(imageStore, id)));
        }

        return properties.length == Background.BGND_NUM_PROPERTIES;
    }

    public static boolean parseMiner(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Entity.MINER_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.MINER_COL]),
                                 Integer.parseInt(properties[Entity.MINER_ROW]));
            Entity entity = createMinerNotFull(properties[Entity.MINER_ID],
                                               Integer.parseInt(
                                                       properties[Entity.MINER_LIMIT]),
                                               pt, Integer.parseInt(
                            properties[Entity.MINER_ACTION_PERIOD]), Integer.parseInt(
                            properties[Entity.MINER_ANIMATION_PERIOD]),
                                               getImageList(imageStore,
                                                       Entity.MINER_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Entity.MINER_NUM_PROPERTIES;
    }

    public static boolean parseObstacle(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Entity.OBSTACLE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.OBSTACLE_COL]),
                                 Integer.parseInt(properties[Entity.OBSTACLE_ROW]));
            Entity entity = createObstacle(properties[Entity.OBSTACLE_ID], pt,
                                           getImageList(imageStore,
                                                   Entity.OBSTACLE_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Entity.OBSTACLE_NUM_PROPERTIES;
    }

    public static boolean parseOre(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Entity.ORE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.ORE_COL]),
                                 Integer.parseInt(properties[Entity.ORE_ROW]));
            Entity entity = createOre(properties[Entity.ORE_ID], pt, Integer.parseInt(
                    properties[Entity.ORE_ACTION_PERIOD]),
                                      getImageList(imageStore, Entity.ORE_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Entity.ORE_NUM_PROPERTIES;
    }

    public static boolean parseSmith(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Entity.SMITH_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.SMITH_COL]),
                                 Integer.parseInt(properties[Entity.SMITH_ROW]));
            Entity entity = createBlacksmith(properties[Entity.SMITH_ID], pt,
                                             getImageList(imageStore,
                                                     Entity.SMITH_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Entity.SMITH_NUM_PROPERTIES;
    }

    public static boolean parseVein(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Entity.VEIN_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.VEIN_COL]),
                                 Integer.parseInt(properties[Entity.VEIN_ROW]));
            Entity entity = createVein(properties[Entity.VEIN_ID], pt,
                                       Integer.parseInt(
                                               properties[Entity.VEIN_ACTION_PERIOD]),
                                       getImageList(imageStore, Entity.VEIN_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Entity.VEIN_NUM_PROPERTIES;
    }

    public static void tryAddEntity(WorldModel world, Entity entity) {
        if (isOccupied(world, entity.position)) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        entity.addEntity(world);
    }



    public static boolean isOccupied(WorldModel world, Point pos) {
        return world.withinBounds( pos) && world.getOccupancyCell( pos) != null;
    }

    public static Optional<Entity> nearestEntity(
            List<Entity> entities, Point pos)
    {
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        else {
            Entity nearest = entities.get(0);
            int nearestDistance = distanceSquared(nearest.position, pos);

            for (Entity other : entities) {
                int otherDistance = distanceSquared(other.position, pos);

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    public static int distanceSquared(Point p1, Point p2) {
        int deltaX = p1.x - p2.x;
        int deltaY = p1.y - p2.y;

        return deltaX * deltaX + deltaY * deltaY;
    }


    /*
       Assumes that there is no entity currently occupying the
       intended destination cell.
    */




    public static Action createActivityAction(
            Entity entity, WorldModel world, ImageStore imageStore)
    {
        return new Action(ActionKind.ACTIVITY, entity, world, imageStore, 0);
    }

    public static Entity createBlacksmith(
            String id, Point position, List<PImage> images)
    {
        return new Entity(EntityKind.BLACKSMITH, id, position, images, 0, 0, 0,
                          0);
    }

    public static Entity createMinerFull(
            String id,
            int resourceLimit,
            Point position,
            int actionPeriod,
            int animationPeriod,
            List<PImage> images)
    {
        return new Entity(EntityKind.MINER_FULL, id, position, images,
                          resourceLimit, resourceLimit, actionPeriod,
                          animationPeriod);
    }

    public static Entity createMinerNotFull(
            String id,
            int resourceLimit,
            Point position,
            int actionPeriod,
            int animationPeriod,
            List<PImage> images)
    {
        return new Entity(EntityKind.MINER_NOT_FULL, id, position, images,
                          resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public static Entity createObstacle(
            String id, Point position, List<PImage> images)
    {
        return new Entity(EntityKind.OBSTACLE, id, position, images, 0, 0, 0,
                          0);
    }

    public static Entity createOre(
            String id, Point position, int actionPeriod, List<PImage> images)
    {
        return new Entity(EntityKind.ORE, id, position, images, 0, 0,
                          actionPeriod, 0);
    }

    public static Entity createOreBlob(
            String id,
            Point position,
            int actionPeriod,
            int animationPeriod,
            List<PImage> images)
    {
        return new Entity(EntityKind.ORE_BLOB, id, position, images, 0, 0,
                          actionPeriod, animationPeriod);
    }

    public static Entity createQuake(
            Point position, List<PImage> images)
    {
        return new Entity(EntityKind.QUAKE, Entity.QUAKE_ID, position, images, 0, 0,
                Entity.QUAKE_ACTION_PERIOD, Entity.QUAKE_ANIMATION_PERIOD);
    }

    public static Entity createVein(
            String id, Point position, int actionPeriod, List<PImage> images)
    {
        return new Entity(EntityKind.VEIN, id, position, images, 0, 0,
                          actionPeriod, 0);
    }


}

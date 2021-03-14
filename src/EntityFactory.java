import processing.core.PImage;



import java.util.List;
import java.util.Random;

public class EntityFactory {

     public static final String QUAKE_ID = "quake";
     public static final String DYNAMITE_ID = "dynamite";

    public static Action createActivityAction(
            HasAction entity, WorldModel world, ImageStore imageStore)
    {
        return new Activity(entity, world, imageStore);
    }

    public static BlackSmith createBlacksmith(
            String id, Point position, List<PImage> images)
    {
        return new BlackSmith( id, position, images);
    }

    public static Sprout createSprout(
            String id, Point position, List<PImage> images)
    {
        return new Sprout( id, position, images);
    }

    public static Fire createFire(
            String id,
            Point position,
            int actionPeriod,
            int animationPeriod,
            List<PImage> images)
    {
        return new Fire(id, position, images,  actionPeriod, animationPeriod);
    }

    public static MinerFull createMinerFull(
            String id,
            int resourceLimit,
            Point position,
            int actionPeriod,
            int animationPeriod,
            List<PImage> images)
    {
        return new MinerFull(id, position, images, resourceLimit, resourceLimit, actionPeriod, animationPeriod);
    }

    public static MinerNotFull createMinerNotFull(
            String id,
            int resourceLimit,
            Point position,
            int actionPeriod,
            int animationPeriod,
            List<PImage> images)
    {
        return new MinerNotFull( id, position, images, resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public static Entity createObstacle(
            String id, Point position, List<PImage> images)
    {
        return new Obstacle(id, position, images);
    }

    public static Ore createOre(
            String id, Point position, int actionPeriod, List<PImage> images)
    {
        return new Ore(id, position, images, actionPeriod);
    }

    public static OreBlob createOreBlob(
            String id,
            Point position,
            int actionPeriod,
            int animationPeriod,
            List<PImage> images)
    {
        return new OreBlob(id, position, images,  actionPeriod, animationPeriod);
    }

    public static Robot createRobot(
            String id,
            Point position,
            int actionPeriod,
            int animationPeriod,
            List<PImage> images)
    {
        return new Robot(id, position, images,  actionPeriod, animationPeriod);
    }

    public static Quake createQuake(
            Point position, List<PImage> images)
    {
        return new Quake(QUAKE_ID, position, images);
    }

    public static Dynamite createDynamite(
            Point position, List<PImage> images)
    {
        return new Dynamite(DYNAMITE_ID, position, images);
    }

    public static Entity createVein(
            String id, Point position, int actionPeriod, List<PImage> images)
    {
        return new Vein(id, position, images,  actionPeriod);
    }
    public static Action createAnimationAction(Animated entity, int repeatCount) {
        return new Animation(entity,  repeatCount);
    }
}

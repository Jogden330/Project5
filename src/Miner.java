import processing.core.PImage;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class Miner extends  Movable{

    protected int resourceCount;
    protected int resourceLimit;
    private static final Random rand = new Random();

    private static final String FIRE_KEY = "fire";
    private final String FIRE_ID_SUFFIX = " -- fire";
    private final int FIRE_PERIOD_SCALE = 4;
    private final int FIRE_ANIMATION_MIN = 50;
    private final int FIRE_ANIMATION_MAX = 150;

    public Miner(String id, Point position,
                 List<PImage> images, int resourceLimit,
                 int resourceCount, int actionPeriod,
                 int animationPeriod, int repeatCount)
    {
        super(id, position, images,actionPeriod, animationPeriod, repeatCount,  new AStarPathingStrategy());
        this.resourceCount=resourceCount;
        this.resourceLimit=resourceLimit;

    }


    public boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore, Miner miner) {

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        miner.scheduleActions(scheduler, world, imageStore);
        return true;

    }

    public boolean setAblaze(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {

        Point pos = getPosition();

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        Fire fire = EntityFactory.createFire(
                getId()+FIRE_ID_SUFFIX,
                pos,
                getActionPeriod() * FIRE_PERIOD_SCALE,
                FIRE_ANIMATION_MIN + rand.nextInt(
                        FIRE_ANIMATION_MAX
                                - FIRE_ANIMATION_MIN),
                imageStore.getImageList(FIRE_KEY));

        world.addEntity(fire);
        fire.scheduleActions(scheduler, world, imageStore);
        return true;
    }

    public  boolean _nextPositionHelper(WorldModel world, Point nextPos) {
        return world.isOccupied(nextPos);
    }

    public int getResourceLimit() {
        return resourceLimit;
    }

    public int getResourceCount() {
        return resourceCount;
    }
}

import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Ore extends HasAction {

    private final String BLOB_KEY = "dynamite";
    private final String BLOB_ID_SUFFIX = " -- dynamite";
    private final int BLOB_PERIOD_SCALE = 4;
    private final int BLOB_ANIMATION_MIN = 50;
    private final int BLOB_ANIMATION_MAX = 150;

    private static final Random rand = new Random();




    public Ore(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod)
    {

        super(id,position,images,actionPeriod);

    }


    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        Point pos = getPosition();

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        OreBlob blob = EntityFactory.createOreBlob(getId() + BLOB_ID_SUFFIX, pos,
                getActionPeriod() / BLOB_PERIOD_SCALE,
                BLOB_ANIMATION_MIN + rand.nextInt(
                        BLOB_ANIMATION_MAX
                                - BLOB_ANIMATION_MIN),
                imageStore.getImageList(BLOB_KEY));

        world.addEntity(blob);
        blob.scheduleActions(scheduler,  world, imageStore);
    }


}

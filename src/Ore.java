import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Ore implements Entity, HasAction {

    private final String BLOB_KEY = "blob";
    private final String BLOB_ID_SUFFIX = " -- blob";
    private final int BLOB_PERIOD_SCALE = 4;
    private final int BLOB_ANIMATION_MIN = 50;
    private final int BLOB_ANIMATION_MAX = 150;



    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int actionPeriod;


    public Ore(
            String id,
            Point position,
            List<PImage> images,
            int actionPeriod)
    {

        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;

    }

    public  PImage getCurrentImage() {return images.get(imageIndex); }

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

    public void scheduleActions(EventScheduler scheduler,  WorldModel world, ImageStore imageStore)
    {
                scheduler.scheduleEvent(this,
                        EntityFactory.createActivityAction(this, world, imageStore),
                        actionPeriod);
    }



    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
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
        ((HasAction)blob).scheduleActions(scheduler,  world, imageStore);
    }


}

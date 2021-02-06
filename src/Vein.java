import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Vein implements Entity, HasAction{

    private static final String ORE_KEY = "ore";
    private static final String ORE_ID_PREFIX = "ore -- ";
    private static final int ORE_CORRUPT_MIN = 20000;
    private static final int ORE_CORRUPT_MAX = 30000;

    private static final Random rand = new Random();

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private int actionPeriod;


    public Vein(

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

    public  PImage getCurrentImage() { return images.get(imageIndex);  }

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

    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {


            scheduler.scheduleEvent(this,
                    EntityFactory.createActivityAction(this, world, imageStore),
                    actionPeriod);

        }
    public void executeActivity (WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        Optional<Point> openPt = world.findOpenAround(this.position);

        if (openPt.isPresent()) {
            Ore ore = EntityFactory.createOre(ORE_ID_PREFIX + this.id, openPt.get(), ORE_CORRUPT_MIN + rand.nextInt(ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
                    imageStore.getImageList(ORE_KEY));
            world.addEntity(ore);
            ore.scheduleActions(scheduler, world, imageStore);
        }

        scheduler.scheduleEvent(this, EntityFactory.createActivityAction(this, world, imageStore),
                    this.actionPeriod);
    }

}

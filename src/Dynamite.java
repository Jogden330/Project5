import processing.core.PImage;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Dynamite extends Animated {

    private static final int DYNAMITE_ACTION_PERIOD = 10000;
    private static final int DYNAMITE_ANIMATION_PERIOD = 600;
    private static final int DYNAMITE_ANIMATION_REPEAT_COUNT = 0;

    private final String ROBOT_KEY = "robot";
    private final String ROBOT_ID_SUFFIX = " -- robot";
    private final int ROBOT_PERIOD_SCALE = 4;
    private final int ROBOT_ANIMATION_MIN = 50;
    private final int ROBOT_ANIMATION_MAX = 150;
    public static final String SAND_KEY = "sand";

    private static final Random rand = new Random();



    public Dynamite(
            String id,
            Point position,
            List<PImage> images)
    {

        super(id, position, images, DYNAMITE_ACTION_PERIOD, DYNAMITE_ANIMATION_PERIOD, DYNAMITE_ANIMATION_REPEAT_COUNT);

    }


    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        List<Point> neighbors = PathingStrategy.ALL_NEIGHBORS_AND_SELF.apply(this.getPosition())
                .filter(point -> world.withinBounds(point))
                .collect(Collectors.toList());
        Background sand = new Background(SAND_KEY,imageStore.getImageList( SAND_KEY));

        for(Point p: neighbors) {


            sand.setBackgroundCell( p, world);
        }


        Robot robot = EntityFactory.createRobot(getId() + ROBOT_ID_SUFFIX, this.getPosition(),
                getActionPeriod() / ROBOT_PERIOD_SCALE,
                ROBOT_ANIMATION_MIN + rand.nextInt(
                        ROBOT_ANIMATION_MAX
                                - ROBOT_ANIMATION_MIN),
                imageStore.getImageList(ROBOT_KEY));

//        Robot robot = EntityFactory.createRobot( "robot", this.getPosition(), getActionPeriod() / BLOB_PERIOD_SCALE
//                imageStore.getImageList( "robot"));
//        world.tryAddEntity(robot);
//        robot.scheduleActions(scheduler,  world, imageStore);

        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);


        world.tryAddEntity(robot);
        robot.scheduleActions(scheduler,  world, imageStore);


    }

}

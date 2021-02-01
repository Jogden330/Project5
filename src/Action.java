import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class Action
{
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

    private ActionKind kind;
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    public Action(
            ActionKind kind,
            Entity entity,
            WorldModel world,
            ImageStore imageStore,
            int repeatCount)
    {
        this.kind = kind;
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = repeatCount;
    }

    public void executeAction( EventScheduler scheduler) {
        switch (kind) {
            case ACTIVITY:
                executeActivityAction( scheduler);
                break;

            case ANIMATION:
                executeAnimationAction( scheduler);
                break;
        }
    }
    private void executeActivityAction(EventScheduler scheduler)
    {
        switch (entity.getKind()) {
            case MINER_FULL:
                entity.executeMinerFullActivity(world, imageStore, scheduler);
                break;

            case MINER_NOT_FULL:
                executeMinerNotFullActivity(scheduler);
                break;

            case ORE:
                executeOreActivity(scheduler);
                break;

            case ORE_BLOB:
                executeOreBlobActivity(scheduler);
                break;

            case QUAKE:
                executeQuakeActivity(scheduler);
                break;

            case VEIN:
                executeVeinActivity(scheduler);
                break;

            default:
                throw new UnsupportedOperationException(String.format(
                        "executeActivityAction not supported for %s",
                        entity.getKind()));
        }
    }

    private void executeAnimationAction(EventScheduler scheduler)
    {
        entity.nextImage();

        if (repeatCount != 1) {
            scheduler.scheduleEvent( entity, Functions.createAnimationAction(entity,  Math.max(repeatCount - 1, 0)),  entity.getAnimationPeriod());
        }
    }

//    public void executeMinerFullActivity(EventScheduler scheduler)
//    {
//        Optional<Entity> fullTarget =
//                findNearest(EntityKind.BLACKSMITH);
//
//        if (fullTarget.isPresent() && entity.moveToFull(world,
//                fullTarget.get(), scheduler))
//        {
//            entity.transformFull( world, scheduler, imageStore);
//        }
//        else {
//            scheduler.scheduleEvent( entity,
//                    Functions.createActivityAction(entity, world, imageStore),
//                    entity.getActionPeriod());
//        }
//    }

    private  void executeMinerNotFullActivity(EventScheduler scheduler)
    {
        Optional<Entity> notFullTarget =
                findNearest( EntityKind.ORE);

        if (!notFullTarget.isPresent() || !entity.moveToNotFull( world, notFullTarget.get(), scheduler)
                || !entity.transformNotFull( world, scheduler, imageStore))
        {
            scheduler.scheduleEvent(entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.getActionPeriod());
        }
    }

    private void executeOreActivity(EventScheduler scheduler)
    {
        Point pos = entity.getPosition();

        world.removeEntity(entity);
        scheduler.unscheduleAllEvents( entity);

        Entity blob = Functions.createOreBlob(entity.getId() + BLOB_ID_SUFFIX, pos,
                entity.getActionPeriod() / BLOB_PERIOD_SCALE,
                BLOB_ANIMATION_MIN + Functions.rand.nextInt(
                        BLOB_ANIMATION_MAX
                                - BLOB_ANIMATION_MIN),
                imageStore.getImageList(BLOB_KEY));

        world.addEntity(blob);
        blob.scheduleActions(scheduler,  world, imageStore);
    }

    private void executeOreBlobActivity(EventScheduler scheduler)
    {
        Optional<Entity> blobTarget =
                findNearest( EntityKind.VEIN);
        long nextPeriod = entity.getActionPeriod();

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().getPosition() ;

            if (entity.moveToOreBlob( world, blobTarget.get(), scheduler)) {
                Entity quake = Functions.createQuake(tgtPos,
                        imageStore.getImageList(QUAKE_KEY));

                world.addEntity(quake);
                nextPeriod += entity.getActionPeriod();
                quake.scheduleActions(scheduler, world, imageStore);
            }
        }

        scheduler.scheduleEvent( entity,
                Functions.createActivityAction(entity, world, imageStore),
                nextPeriod);
    }

    private void executeQuakeActivity(EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(entity);
        world.removeEntity(entity);
    }

    private void executeVeinActivity(EventScheduler scheduler)
    {
        Optional<Point> openPt = world.findOpenAround(entity.getPosition());

        if (openPt.isPresent()) {
            Entity ore = Functions.createOre(ORE_ID_PREFIX + entity.getId(), openPt.get(),
                    ORE_CORRUPT_MIN + Functions.rand.nextInt(
                            ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
                    imageStore.getImageList(ORE_KEY));
            world.addEntity(ore);
            ore.scheduleActions(scheduler, world, imageStore);
        }

        scheduler.scheduleEvent( entity,
                Functions.createActivityAction(entity, world, imageStore),
                entity.getActionPeriod());
    }


    private  Optional<Entity> findNearest( EntityKind kind)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : world.getEntities()) {
            if (entity.getKind() == kind) {
                ofType.add(entity);
            }
        }

        return entity.getPosition().nearestEntity(ofType);
    }




}

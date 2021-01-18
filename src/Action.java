import java.util.Optional;

public final class Action
{
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
        switch (entity.kind) {
            case MINER_FULL:
                executeMinerFullActivity(scheduler);
                break;

            case MINER_NOT_FULL:
                executeMinerNotFullActivity( scheduler);
                break;

            case ORE:
                executeOreActivity( scheduler);
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
                        entity.kind));
        }
    }

    private void executeAnimationAction(EventScheduler scheduler)
    {
        entity.nextImage();

        if (repeatCount != 1) {
            Functions.scheduleEvent(scheduler, entity, Functions.createAnimationAction(entity,  Math.max(repeatCount - 1, 0)),  entity.getAnimationPeriod());
        }
    }

    private void executeMinerFullActivity(EventScheduler scheduler)
    {
        Optional<Entity> fullTarget =
                Functions.findNearest(world, entity.position, EntityKind.BLACKSMITH);

        if (fullTarget.isPresent() && Functions.moveToFull(entity, world,
                fullTarget.get(), scheduler))
        {
            Functions.transformFull(entity, world, scheduler, imageStore);
        }
        else {
            Functions.scheduleEvent(scheduler, entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
        }
    }

    private   void executeMinerNotFullActivity(EventScheduler scheduler)
    {
        Optional<Entity> notFullTarget =
                Functions.findNearest(world, entity.position, EntityKind.ORE);

        if (!notFullTarget.isPresent() || !Functions.moveToNotFull(entity, world,
                notFullTarget.get(),
                scheduler)
                || !Functions.transformNotFull(entity, world, scheduler, imageStore))
        {
            Functions.scheduleEvent(scheduler, entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
        }
    }

    private void executeOreActivity(EventScheduler scheduler)
    {
        Point pos = entity.position;

        Functions.removeEntity(world, entity);
        Functions.unscheduleAllEvents(scheduler, entity);

        Entity blob = Functions.createOreBlob(entity.id + Functions.BLOB_ID_SUFFIX, pos,
                entity.actionPeriod / Functions.BLOB_PERIOD_SCALE,
                Functions.BLOB_ANIMATION_MIN + Functions.rand.nextInt(
                        Functions.BLOB_ANIMATION_MAX
                                - Functions.BLOB_ANIMATION_MIN),
                Functions.getImageList(imageStore, Functions.BLOB_KEY));

        Functions.addEntity(world, blob);
        Functions.scheduleActions(blob, scheduler, world, imageStore);
    }

    private void executeOreBlobActivity(EventScheduler scheduler)
    {
        Optional<Entity> blobTarget =
                Functions.findNearest(world, entity.position, EntityKind.VEIN);
        long nextPeriod = entity.actionPeriod;

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().position;

            if (Functions.moveToOreBlob(entity, world, blobTarget.get(), scheduler)) {
                Entity quake = Functions.createQuake(tgtPos,
                        Functions.getImageList(imageStore, Functions.QUAKE_KEY));

                Functions.addEntity(world, quake);
                nextPeriod += entity.actionPeriod;
                Functions.scheduleActions(quake, scheduler, world, imageStore);
            }
        }

        Functions.scheduleEvent(scheduler, entity,
                Functions.createActivityAction(entity, world, imageStore),
                nextPeriod);
    }

    private void executeQuakeActivity(EventScheduler scheduler)
    {
        Functions.unscheduleAllEvents(scheduler, entity);
        Functions.removeEntity(world, entity);
    }

    private void executeVeinActivity(EventScheduler scheduler)
    {
        Optional<Point> openPt =Functions.findOpenAround(world, entity.position);

        if (openPt.isPresent()) {
            Entity ore = Functions.createOre(Functions.ORE_ID_PREFIX + entity.id, openPt.get(),
                    Functions.ORE_CORRUPT_MIN + Functions.rand.nextInt(
                            Functions.ORE_CORRUPT_MAX - Functions.ORE_CORRUPT_MIN),
                    Functions.getImageList(imageStore, Functions.ORE_KEY));
            Functions.addEntity(world, ore);
            Functions.scheduleActions(ore, scheduler, world, imageStore);
        }

        Functions.scheduleEvent(scheduler, entity,
                Functions.createActivityAction(entity, world, imageStore),
                entity.actionPeriod);
    }
}

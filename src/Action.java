import java.util.LinkedList;
import java.util.List;
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
            scheduler.scheduleEvent( entity, createAnimationAction(entity,  Math.max(repeatCount - 1, 0)),  entity.getAnimationPeriod());
        }
    }

    private void executeMinerFullActivity(EventScheduler scheduler)
    {
        Optional<Entity> fullTarget =
                findNearest(  EntityKind.BLACKSMITH);

        if (fullTarget.isPresent() && Functions.moveToFull(entity, world,
                fullTarget.get(), scheduler))
        {
            Functions.transformFull(entity, world, scheduler, imageStore);
        }
        else {
            scheduler.scheduleEvent( entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
        }
    }

    private   void executeMinerNotFullActivity(EventScheduler scheduler)
    {
        Optional<Entity> notFullTarget =
                findNearest( EntityKind.ORE);

        if (!notFullTarget.isPresent() || !Functions.moveToNotFull(entity, world,
                notFullTarget.get(),
                scheduler)
                || !Functions.transformNotFull(entity, world, scheduler, imageStore))
        {
            scheduler.scheduleEvent(entity,
                    Functions.createActivityAction(entity, world, imageStore),
                    entity.actionPeriod);
        }
    }

    private void executeOreActivity(EventScheduler scheduler)
    {
        Point pos = entity.position;

        entity.removeEntity(world);
        scheduler.unscheduleAllEvents( entity);

        Entity blob = Functions.createOreBlob(entity.id + Entity.BLOB_ID_SUFFIX, pos,
                entity.actionPeriod / Entity.BLOB_PERIOD_SCALE,
                Entity.BLOB_ANIMATION_MIN + Functions.rand.nextInt(
                        Entity.BLOB_ANIMATION_MAX
                                - Entity.BLOB_ANIMATION_MIN),
                Functions.getImageList(imageStore, Entity.BLOB_KEY));

        blob.addEntity(world);
        scheduler.scheduleActions(blob,  world, imageStore);
    }

    private void executeOreBlobActivity(EventScheduler scheduler)
    {
        Optional<Entity> blobTarget =
                findNearest( EntityKind.VEIN);
        long nextPeriod = entity.actionPeriod;

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().position;

            if (Functions.moveToOreBlob(entity, world, blobTarget.get(), scheduler)) {
                Entity quake = Functions.createQuake(tgtPos,
                        Functions.getImageList(imageStore, Entity.QUAKE_KEY));

                quake.addEntity(world);
                nextPeriod += entity.actionPeriod;
                scheduler.scheduleActions(quake, world, imageStore);
            }
        }

        scheduler.scheduleEvent( entity,
                Functions.createActivityAction(entity, world, imageStore),
                nextPeriod);
    }

    private void executeQuakeActivity(EventScheduler scheduler)
    {
        scheduler.unscheduleAllEvents(entity);
        entity.removeEntity(world);
    }

    private void executeVeinActivity(EventScheduler scheduler)
    {
        Optional<Point> openPt =Functions.findOpenAround(world, entity.position);

        if (openPt.isPresent()) {
            Entity ore = Functions.createOre(Entity.ORE_ID_PREFIX + entity.id, openPt.get(),
                    Entity.ORE_CORRUPT_MIN + Functions.rand.nextInt(
                            Entity.ORE_CORRUPT_MAX - Entity.ORE_CORRUPT_MIN),
                    Functions.getImageList(imageStore, Entity.ORE_KEY));
            ore.addEntity(world);
            scheduler.scheduleActions(ore,  world, imageStore);
        }

        scheduler.scheduleEvent( entity,
                Functions.createActivityAction(entity, world, imageStore),
                entity.actionPeriod);
    }


    private  Optional<Entity> findNearest( EntityKind kind)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : world.entities) {
            if (entity.kind == kind) {
                ofType.add(entity);
            }
        }

        return Functions.nearestEntity(ofType, entity.position);
    }

    public static Action createAnimationAction(Entity entity, int repeatCount) {
        return new Action(ActionKind.ANIMATION, entity, null, null,
                repeatCount);
    }
}

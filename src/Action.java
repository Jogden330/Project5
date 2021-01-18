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
                Functions.executeMinerFullActivity(entity, world,
                        imageStore, scheduler);
                break;

            case MINER_NOT_FULL:
                Functions.executeMinerNotFullActivity(entity, world,
                        imageStore, scheduler);
                break;

            case ORE:
                Functions.executeOreActivity(entity, world,
                        imageStore, scheduler);
                break;

            case ORE_BLOB:
                Functions.executeOreBlobActivity(entity, world,
                        imageStore, scheduler);
                break;

            case QUAKE:
                Functions.executeQuakeActivity(entity, world,
                        imageStore, scheduler);
                break;

            case VEIN:
                Functions.executeVeinActivity(entity, world,
                        imageStore, scheduler);
                break;

            default:
                throw new UnsupportedOperationException(String.format(
                        "executeActivityAction not supported for %s",
                        entity.kind));
        }
    }

    private void executeAnimationAction(EventScheduler scheduler)
    {
        Functions.nextImage(entity);

        if (repeatCount != 1) {
            Functions.scheduleEvent(scheduler, entity,
                    Functions.createAnimationAction(entity,
                            Math.max(repeatCount - 1,
                                    0)),
                    Functions.getAnimationPeriod(entity));
        }
    }
}

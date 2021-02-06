public class Activity implements  Action{

    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;


    public Activity(

            Entity entity,
            WorldModel world,
            ImageStore imageStore)

    {

        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;

    }


    public void executeAction(EventScheduler scheduler)
    {
        switch (entity.getClass().getSimpleName()) {
            case MINER_FULL:
                entity.executeMinerFullActivity(world, imageStore, scheduler);
                break;

            case MINER_NOT_FULL:
                entity.executeMinerNotFullActivity(world, imageStore, scheduler);
                break;

            case ORE:
                entity.executeOreActivity(world, imageStore, scheduler);
                break;

            case ORE_BLOB:
                entity.executeOreBlobActivity(world, imageStore, scheduler);
                break;

            case QUAKE:
                entity.executeQuakeActivity(world,scheduler);
                break;

            case VEIN:
                entity.executeVeinActivity(world, imageStore, scheduler);
                break;

            default:
                throw new UnsupportedOperationException(String.format(
                        "executeActivityAction not supported for %s",
                        entity.getKind()));
        }
    }


}

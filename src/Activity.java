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
        entity.executeActivity(this.world, this.imageStore, scheduler);
    }


}

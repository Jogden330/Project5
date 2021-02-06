public class Activity implements  Action{

    private HasAction entity;
    private WorldModel world;
    private ImageStore imageStore;


    public Activity(

            HasAction entity,
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

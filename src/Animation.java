public class Animation implements Action{

    private Entity entity;
    private int repeatCount;

    public Animation(

            Entity entity,
            int repeatCount)
    {

        this.entity = entity;
        this.repeatCount = repeatCount;
    }


    public void executeAction(EventScheduler scheduler)
    {
        entity.nextImage();

        if (repeatCount != 1) {
            scheduler.scheduleEvent( entity, EntityFactory.createAnimationAction(entity,  Math.max(repeatCount - 1, 0)),  entity.getAnimationPeriod());
        }
    }

}

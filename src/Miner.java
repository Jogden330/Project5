public interface Miner extends Entity{
    boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore);
}

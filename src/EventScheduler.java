import java.util.*;

public final class EventScheduler
{
    private  final int QUAKE_ANIMATION_REPEAT_COUNT = 10;

    private PriorityQueue<Event> eventQueue;
    private Map<Entity, List<Event>> pendingEvents;
    private double timeScale;

    public EventScheduler(double timeScale) {
        this.eventQueue = new PriorityQueue<>(new EventComparator());
        this.pendingEvents = new HashMap<>();
        this.timeScale = timeScale;
    }

    public  void scheduleEvent(
            Entity entity,
            Action action,
            long afterPeriod)
    {
        long time = System.currentTimeMillis() + (long)(afterPeriod
                * timeScale);
        Event event = new Event(action, time, entity);

        eventQueue.add(event);

        // update list of pending events for the given entity
        List<Event> pending = pendingEvents.getOrDefault(entity,
                new LinkedList<>());
        pending.add(event);
        pendingEvents.put(entity, pending);
    }


    public void unscheduleAllEvents(Entity entity)
    {
        List<Event> pending = pendingEvents.remove(entity);

        if (pending != null) {
            for (Event event : pending) {
                eventQueue.remove(event);
            }
        }
    }
    public void scheduleActions(
            Entity entity,
            WorldModel world,
            ImageStore imageStore)
    {
        switch (entity.getKind()) {
            case MINER_FULL:
                scheduleEvent(entity,
                        Functions.createActivityAction(entity, world, imageStore),
                        entity.getActionPeriod());
               scheduleEvent( entity,
                        Functions.createAnimationAction(entity, 0),
                        entity.getAnimationPeriod());
                break;

            case MINER_NOT_FULL:
                scheduleEvent( entity,
                        Functions.createActivityAction(entity, world, imageStore),
                        entity.getActionPeriod());
                scheduleEvent( entity,
                        Functions.createAnimationAction(entity, 0),
                        entity.getAnimationPeriod());
                break;

            case ORE:
                scheduleEvent( entity,
                        Functions.createActivityAction(entity, world, imageStore),
                        entity.getActionPeriod());
                break;

            case ORE_BLOB:
                scheduleEvent(entity,
                        Functions.createActivityAction(entity, world, imageStore),
                        entity.getActionPeriod());
                scheduleEvent( entity,
                        Functions.createAnimationAction(entity, 0),
                        entity.getAnimationPeriod());
                break;

            case QUAKE:
                scheduleEvent(entity,
                        Functions.createActivityAction(entity, world, imageStore),
                        entity.getActionPeriod());
                scheduleEvent(entity,
                        Functions.createAnimationAction(entity,
                        QUAKE_ANIMATION_REPEAT_COUNT),
                        entity.getAnimationPeriod());
                break;

            case VEIN:
                scheduleEvent( entity,
                        Functions.createActivityAction(entity, world, imageStore),
                        entity.getActionPeriod());
                break;

            default:
        }
    }

    public  void updateOnTime(long time) {
        while (!eventQueue.isEmpty()
                && eventQueue.peek().getTime() < time) {
            Event next = eventQueue.poll();

            removePendingEvent(next);

            next.getAction().executeAction(this);
        }
    }

    public void removePendingEvent(Event event)
    {
        List<Event> pending = pendingEvents.get(event.getEntity());

        if (pending != null) {
            pending.remove(event);
        }
    }





}

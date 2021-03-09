import processing.core.PImage;

import java.util.*;

public final class WorldModel
{


    private  final int ORE_REACH = 1;



    private int numRows;
    private int numCols;
    private Background background[][];
    private Entity occupancy[][];
    private Set<Entity> entities;

    public WorldModel(int numRows, int numCols, Background defaultBackground) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.background = new Background[numRows][numCols];
        this.occupancy = new Entity[numRows][numCols];
        this.entities = new HashSet<>();

        for (int row = 0; row < numRows; row++) {
            Arrays.fill(this.background[row], defaultBackground);
        }
    }



    public  boolean withinBounds( Point pos) {
        return pos.y >= 0 && pos.y < numRows && pos.x >= 0
                && pos.x < numCols;
    }

    public Optional<PImage> getBackgroundImage(Point pos)
    {
        if (withinBounds(pos)) {
            return Optional.of(getBackgroundCell(pos).getCurrentImage());
        }
        else {
            return Optional.empty();
        }
    }

    private Background getBackgroundCell( Point pos) {
        return background[pos.y][pos.x];
    }

    public  Optional<Entity> getOccupant( Point pos) {
        if (isOccupied(pos)) {
            return Optional.of(getOccupancyCell( pos));
        }
        else {
            return Optional.empty();
        }
    }
    public  Entity getOccupancyCell( Point pos) {
        return occupancy[pos.y][pos.x];
    }

    public void setOccupancyCell(Point pos, Entity entity)
    {
        occupancy[pos.y][pos.x] = entity;
    }

    public void tryAddEntity(Entity entity) {
        if (isOccupied(entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

       addEntity(entity);
    }
    public boolean isOccupied(Point pos) {
        return withinBounds(pos) && getOccupancyCell(pos) != null;
    }

    public void load(
            Scanner in, ImageStore imageStore)
    {
        int lineNumber = 0;
        while (in.hasNextLine()) {
            try {
                if (!WorldFactory.processLine(in.nextLine(), this, imageStore)) {
                    System.err.println(String.format("invalid entry on line %d",
                            lineNumber));
                }
            }
            catch (NumberFormatException e) {
                System.err.println(
                        String.format("invalid entry on line %d", lineNumber));
            }
            catch (IllegalArgumentException e) {
                System.err.println(
                        String.format("issue on line %d: %s", lineNumber,
                                e.getMessage()));
            }
            lineNumber++;
        }
    }






    public  Optional<Point> findOpenAround( Point pos) {
        for (int dy = -ORE_REACH; dy <= ORE_REACH; dy++) {
            for (int dx = -ORE_REACH; dx <= ORE_REACH; dx++) {
                Point newPt = new Point(pos.x + dx, pos.y + dy);
                if (withinBounds( newPt) && !isOccupied(newPt)) {
                    return Optional.of(newPt);
                }
            }
        }

        return Optional.empty();
    }

    public  void moveEntity(Entity entity,  Point pos) {
        Point oldPos = entity.getPosition();
        if (withinBounds(pos) && !pos.equals(oldPos)) {
            setOccupancyCell(oldPos, null);
            removeEntityAt(pos);
            setOccupancyCell(pos,entity);
            entity.setPosition(pos);
        }
    }

    public  void removeEntity(Entity entity) { removeEntityAt(entity.getPosition());
    }

    public void addEntity(Entity entity) {
        if (withinBounds(entity.getPosition())) {
            setOccupancyCell( entity.getPosition(), entity);
            entities.add(entity);
        }
    }

    public void removeEntityAt(  Point pos) {
        if (withinBounds(pos) && getOccupancyCell(pos) != null) {
            Entity entity = getOccupancyCell(pos);

            /* This moves the entity just outside of the grid for
             * debugging purposes. */
            entity.setPosition(new Point(-1, -1));
            getEntities().remove(entity);
           setOccupancyCell( pos, null);
        }
    }

    public int getNumRows() {return numRows;}
    public int getNumCols() {return numCols;}

    public Background[][] getBackground() {
        return background;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public Optional<Entity> findNearest(Class kind, Point pos)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : getEntities()) {
            if (entity.getClass().equals(kind)) {
                ofType.add(entity);
            }
        }

        return pos.nearestEntity(ofType);
    }


}

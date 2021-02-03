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




//    public boolean parseBackground(String[] properties,  ImageStore imageStore)
//    {
//        if (properties.length == BGND_NUM_PROPERTIES) {
//            Point pt = new Point(Integer.parseInt(properties[BGND_COL]),
//                    Integer.parseInt(properties[BGND_ROW]));
//            String id = properties[BGND_ID];
//            new Background(id, imageStore.getImageList(id)).setBackground( pt,this);
//        }
//
//        return properties.length == BGND_NUM_PROPERTIES;
//    }
//
//    public boolean parseMiner(
//            String[] properties, ImageStore imageStore)
//    {
//        if (properties.length == MINER_NUM_PROPERTIES) {
//            Point pt = new Point(Integer.parseInt(properties[MINER_COL]),
//                    Integer.parseInt(properties[MINER_ROW]));
//            Entity entity = EntityFactory.createMinerNotFull(properties[MINER_ID],
//                    Integer.parseInt(
//                            properties[MINER_LIMIT]),
//                    pt, Integer.parseInt(
//                            properties[MINER_ACTION_PERIOD]), Integer.parseInt(
//                            properties[MINER_ANIMATION_PERIOD]),
//                    imageStore.getImageList(MINER_KEY));
//            tryAddEntity(entity);
//        }
//
//        return properties.length == MINER_NUM_PROPERTIES;
//    }
//
//    public boolean parseObstacle(
//            String[] properties,  ImageStore imageStore)
//    {
//        if (properties.length == OBSTACLE_NUM_PROPERTIES) {
//            Point pt = new Point(Integer.parseInt(properties[OBSTACLE_COL]),
//                    Integer.parseInt(properties[OBSTACLE_ROW]));
//            Entity entity = EntityFactory.createObstacle(properties[OBSTACLE_ID], pt,
//                    imageStore.getImageList(OBSTACLE_KEY));
//            tryAddEntity(entity);
//        }
//
//        return properties.length == OBSTACLE_NUM_PROPERTIES;
//
//    }
//
//    public boolean parseOre(
//            String[] properties,  ImageStore imageStore)
//    {
//        if (properties.length == ORE_NUM_PROPERTIES) {
//            Point pt = new Point(Integer.parseInt(properties[ORE_COL]),
//                    Integer.parseInt(properties[ORE_ROW]));
//            Entity entity = EntityFactory.createOre(properties[ORE_ID], pt, Integer.parseInt(
//                    properties[ORE_ACTION_PERIOD]),
//                    imageStore.getImageList(ORE_KEY));
//            tryAddEntity(entity);
//        }
//
//        return properties.length == ORE_NUM_PROPERTIES;
//    }
//
//    public  boolean parseSmith(
//            String[] properties,  ImageStore imageStore)
//    {
//        if (properties.length == SMITH_NUM_PROPERTIES) {
//            Point pt = new Point(Integer.parseInt(properties[SMITH_COL]),
//                    Integer.parseInt(properties[SMITH_ROW]));
//            Entity entity =EntityFactory.createBlacksmith(properties[SMITH_ID], pt,
//                    imageStore.getImageList(SMITH_KEY));
//            tryAddEntity(entity);
//        }
//
//        return properties.length == SMITH_NUM_PROPERTIES;
//    }
//
//    public boolean parseVein(
//            String[] properties,  ImageStore imageStore)
//    {
//        if (properties.length == VEIN_NUM_PROPERTIES) {
//            Point pt = new Point(Integer.parseInt(properties[VEIN_COL]),
//                    Integer.parseInt(properties[VEIN_ROW]));
//            Entity entity = EntityFactory.createVein(properties[VEIN_ID], pt,
//                    Integer.parseInt(
//                            properties[VEIN_ACTION_PERIOD]),
//                    imageStore.getImageList(VEIN_KEY));
//            tryAddEntity(entity);
//        }
//
//        return properties.length == VEIN_NUM_PROPERTIES;
//    }


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


}

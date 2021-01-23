import processing.core.PImage;

import java.util.*;

public final class WorldModel
{
    public int numRows;
    public int numCols;
    public Background background[][];
    public Entity occupancy[][];
    public Set<Entity> entities;

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

    public void setBackground(Point pos, Background background)
    {
        if (withinBounds(pos)) {
            this.setBackgroundCell( pos, background);
        }
    }
    private void setBackgroundCell( Point pos, Background background)
    {
        this.background[pos.y][pos.x] = background;
    }

    public  boolean withinBounds( Point pos) {
        return pos.y >= 0 && pos.y < numRows && pos.x >= 0
                && pos.x < numCols;
    }

    public Optional<PImage> getBackgroundImage(
             Point pos)
    {
        if (withinBounds( pos)) {
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
        if (isOccupied(entity.position)) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        entity.addEntity(this);
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
                if (!processLine(in.nextLine(), imageStore)) {
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
    public boolean processLine(String line,  ImageStore imageStore)
    {
        String[] properties = line.split("\\s");
        if (properties.length > 0) {
            switch (properties[Entity.PROPERTY_KEY]) {
                case Background.BGND_KEY:
                    return parseBackground(properties, imageStore);
                case Entity.MINER_KEY:
                    return parseMiner(properties, imageStore);
                case Entity.OBSTACLE_KEY:
                    return parseObstacle(properties, imageStore);
                case Entity.ORE_KEY:
                    return parseOre(properties, imageStore);
                case Entity.SMITH_KEY:
                    return parseSmith(properties, imageStore);
                case Entity.VEIN_KEY:
                    return parseVein(properties, imageStore);
            }
        }

        return false;
    }

    public boolean parseBackground(String[] properties,  ImageStore imageStore)
    {
        if (properties.length == Background.BGND_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Background.BGND_COL]),
                    Integer.parseInt(properties[Background.BGND_ROW]));
            String id = properties[Background.BGND_ID];
            setBackground( pt,new Background(id, Functions.getImageList(imageStore, id)));
        }

        return properties.length == Background.BGND_NUM_PROPERTIES;
    }

    public boolean parseMiner(
            String[] properties, ImageStore imageStore)
    {
        if (properties.length == Entity.MINER_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.MINER_COL]),
                    Integer.parseInt(properties[Entity.MINER_ROW]));
            Entity entity = Functions.createMinerNotFull(properties[Entity.MINER_ID],
                    Integer.parseInt(
                            properties[Entity.MINER_LIMIT]),
                    pt, Integer.parseInt(
                            properties[Entity.MINER_ACTION_PERIOD]), Integer.parseInt(
                            properties[Entity.MINER_ANIMATION_PERIOD]),
                    Functions.getImageList(imageStore,
                            Entity.MINER_KEY));
            tryAddEntity(entity);
        }

        return properties.length == Entity.MINER_NUM_PROPERTIES;
    }

    public boolean parseObstacle(
            String[] properties,  ImageStore imageStore)
    {
        if (properties.length == Entity.OBSTACLE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.OBSTACLE_COL]),
                    Integer.parseInt(properties[Entity.OBSTACLE_ROW]));
            Entity entity = Functions.createObstacle(properties[Entity.OBSTACLE_ID], pt,
                    Functions.getImageList(imageStore,
                            Entity.OBSTACLE_KEY));
            tryAddEntity(entity);
        }

        return properties.length == Entity.OBSTACLE_NUM_PROPERTIES;

    }

    public boolean parseOre(
            String[] properties,  ImageStore imageStore)
    {
        if (properties.length == Entity.ORE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.ORE_COL]),
                    Integer.parseInt(properties[Entity.ORE_ROW]));
            Entity entity = Functions.createOre(properties[Entity.ORE_ID], pt, Integer.parseInt(
                    properties[Entity.ORE_ACTION_PERIOD]),
                    Functions.getImageList(imageStore, Entity.ORE_KEY));
            tryAddEntity(entity);
        }

        return properties.length == Entity.ORE_NUM_PROPERTIES;
    }

    public  boolean parseSmith(
            String[] properties,  ImageStore imageStore)
    {
        if (properties.length == Entity.SMITH_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.SMITH_COL]),
                    Integer.parseInt(properties[Entity.SMITH_ROW]));
            Entity entity = Functions.createBlacksmith(properties[Entity.SMITH_ID], pt,
                    Functions.getImageList(imageStore,
                            Entity.SMITH_KEY));
            tryAddEntity(entity);
        }

        return properties.length == Entity.SMITH_NUM_PROPERTIES;
    }

    public boolean parseVein(
            String[] properties,  ImageStore imageStore)
    {
        if (properties.length == Entity.VEIN_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Entity.VEIN_COL]),
                    Integer.parseInt(properties[Entity.VEIN_ROW]));
            Entity entity = Functions.createVein(properties[Entity.VEIN_ID], pt,
                    Integer.parseInt(
                            properties[Entity.VEIN_ACTION_PERIOD]),
                    Functions.getImageList(imageStore, Entity.VEIN_KEY));
            tryAddEntity(entity);
        }

        return properties.length == Entity.VEIN_NUM_PROPERTIES;
    }

}

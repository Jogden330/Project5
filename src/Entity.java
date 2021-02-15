import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public abstract class Entity
{
    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;

    public Entity(
            String id,
            Point position,
            List<PImage> images)
    {

        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;

    }

    public  PImage getCurrentImage() {return images.get(imageIndex); }

    public String getId() { return id; }

    public Point getPosition() { return position; }

    public List<PImage> getimages() { return images; }

    public int getImageIndex() { return imageIndex; }

    protected void setImageIndex(int integer) { imageIndex = integer; }

    public void setPosition(Point position) { this.position = position; }
}
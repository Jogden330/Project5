import processing.core.PImage;
import java.util.List;


public class Obstacle implements Entity {



    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;


    public Obstacle(
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

    public void setPosition(Point position) { this.position = position; }


}

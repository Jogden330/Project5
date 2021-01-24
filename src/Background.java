import java.util.List;

import processing.core.PImage;

public final class Background
{






    public String id;
    public List<PImage> images;
    public int imageIndex;

    public Background(String id, List<PImage> images) {
        this.id = id;
        this.images = images;
    }

    public PImage getCurrentImage() {

            return images.get(imageIndex);

    }

    public void setBackground(Point pos, WorldModel world)
    {
        if (world.withinBounds(pos)) {
            this.setBackgroundCell( pos, world);
        }
    }

    public void setBackgroundCell( Point pos, WorldModel world)
    {
        world.getBackground()[pos.y][pos.x] = this;
    }
}

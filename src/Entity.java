import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import processing.core.PImage;

public interface Entity
{
    PImage getCurrentImage();
    Point getPosition();
    void setPosition(Point position);
}
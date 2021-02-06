import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public interface Action
{
    void executeAction( EventScheduler scheduler);
}

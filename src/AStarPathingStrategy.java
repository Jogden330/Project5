import javax.swing.text.Position;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AStarPathingStrategy
        implements PathingStrategy
{


    public List<Point> computePath(Point start, Point end,
                                   Predicate<Point> canPassThrough,
                                   BiPredicate<Point, Point> withinReach,
                                   Function<Point, Stream<Point>> potentialNeighbors)
    {
        LinkedList<Point> path = new LinkedList<Point>();

        PriorityQueue<Node> OpenListQueue = new PriorityQueue<Node>((node1, node2) -> Integer.compare(node1.getF(), node2.getF()));

        HashMap<Point,Node> OpenListHash = new HashMap();

        HashMap<Point,Node> ClosedListHash = new HashMap();

        Node StartNode = new Node(start,0, 0, null);

        OpenListQueue.add(StartNode);

        OpenListHash.put(start, StartNode);

        while (!OpenListQueue.isEmpty()){

            Node PrierNode = OpenListQueue.poll();

           // make list of naigbord
            List<Point> neighbors = potentialNeighbors.apply(PrierNode.getPosition())
                    .filter(N -> (ClosedListHash.get(N) == null))//filter out the closed list
                    .filter(canPassThrough)
                    .filter(pt ->!pt.equals(start) && !pt.equals(end))
                    .collect(Collectors.toList());



            for(Point neighbor: neighbors){



                    Node neighborNode = new Node(neighbor, PrierNode.getG() + 1, manhattanDistance(neighbor, end), PrierNode);

                    if(neighborNode.getPosition().adjacent(end)){
//                        makePath(neighborNode, StartNode, path);
//                        System.out.println(path);
                        return makePath(neighborNode, StartNode, path);

                    }

                    if(OpenListHash.get(neighbor) == null ){
                        OpenListHash.put(neighbor,neighborNode);
                        OpenListQueue.add(neighborNode);

                    } else if(OpenListHash.get(neighbor).getG() > neighborNode.getG()){
                        OpenListHash.get(neighbor).setG(neighborNode.getG(), PrierNode);

                    }
            }

            ClosedListHash.put(PrierNode.getPosition(), PrierNode);

        }
        return null;
    }

    private List<Point> makePath(Node neighborNode, Node startNode, LinkedList<Point> path) {
        if(neighborNode.getPosition() == startNode.getPosition()){
            return path;
        }
        path.addFirst(neighborNode.position);
        return makePath(neighborNode.Parent, startNode, path);

    }

    private int manhattanDistance(Point current, Point end){

        return Math.abs(current.x - end.x) + Math.abs(current.y - end.y);
   }
}



class Node {

    Node Parent;
    Point position;
    private int DistStart;
    private int Heuristic;
    private int DistTotal;

    public Node(Point position, int DistStart, int Heuristic, Node Parent){

        this.position = position;
        this.DistStart = DistStart;
        this.Heuristic = Heuristic;
        DistTotal = DistStart + Heuristic;
        this.Parent = Parent;


    }

    public Node getParent(){ return Parent; }

    public Point getPosition(){ return position; }

    public int getG() { return DistStart;}

    public void setG(int DistStart, Node Parent) {
        this.DistStart = DistStart;
        DistTotal = this.DistStart + Heuristic;
        this.Parent = Parent;

    }

    public  int getF() {
        return DistTotal;
    }



}

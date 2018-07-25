import java.util.HashSet;
import java.util.Iterator;

//Node is a parent of Tile and describes the methods needed to keep the neighbours updated
public class Node<T> {
    private final T value;
    private HashSet<Node> neighbours = new HashSet<Node>();

    public Node(T value) { this.value=value; }

    public void addNeighbour(Node neighbour) {
        neighbours.add(neighbour);
    }

    public T getValue() {
        return value;
    }

    public Node[] getNeighbours() {
        Node[] n = new Node[neighbours.size()];
        Iterator it = neighbours.iterator();
        for(int i=0; i<neighbours.size(); i++) {
            n[i]=(Node)it.next();
        }
        return n;
    }


}

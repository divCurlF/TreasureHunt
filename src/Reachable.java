import java.util.HashSet;
import java.util.LinkedList;

public class Reachable {

    private WorldMap map;
    private Tile start;
    private Tile goal;
    private boolean strict;

    public Reachable(WorldMap map, Tile start, Tile goal, boolean strict) {
        this.map = map;
        this.start = start;
        this.goal = goal;
        this.strict=strict;
    }
    //Uses a flood fill to determine is a tile is reachable from the actor location
    public boolean isReachable() {
        LinkedList<Tile> queue = new LinkedList<Tile>();
        HashSet<Tile> connectedTiles = new HashSet<Tile>();

        queue.add(start);

        while(!queue.isEmpty()) {
            Tile next = queue.poll();
            if (connectedTiles.contains(next) || !map.isPassable(next, strict)) {
                continue;
            }
            connectedTiles.add(next);

            for (Node neighbour : next.getNeighbours()) {
                Tile neighbourTile = map.getMap().get(neighbour.getValue());
                if (!connectedTiles.contains(neighbourTile)) {
                    queue.add(neighbourTile);
                }
            }
        }
        return connectedTiles.contains(goal);
    }
}

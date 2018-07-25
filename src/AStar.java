import java.util.*;

public class AStar {
    private SearchNode startNode;
    private SearchNode goalNode;
    private SearchNode current;
    private Comparator<SearchNode> comparator = new FComparator();
    private PriorityQueue<SearchNode> unexplored = new PriorityQueue<>(comparator);
    private ArrayList<SearchNode> explored = new ArrayList<>();
    private HashMap<Tile, SearchNode> mapping = new HashMap<>();
    private Direction direction;
    private WorldMap map;

    //AStar initialises a new A* search
    public AStar(Tile start, Tile goal, WorldMap map, Direction currDirection) {
        this.startNode = new SearchNode(start, 0, 0, 0);
        this.goalNode = new SearchNode(goal);
        mapping.put(start, startNode);
        mapping.put(goal, goalNode);
        this.direction = currDirection;
        this.map = map;
        startNode.sethCost(Heuristic.manhattan(startNode, goalNode, direction));
        unexplored.add(startNode);
    }

    //Find path builds a path using an A* search and returns a hashtable of child and parent tiles
    public Hashtable<Tile, Tile> findPath(Boolean waterOK, Boolean landOK, boolean treeOK, boolean stonesOK) {
        Hashtable<Tile, Tile> cameFrom = new Hashtable<>();
        //If the actor is sailing, first check the island they are about to enter is escapable
        if ((goalNode.getTile().getZone() != startNode.getTile().getZone()) && map.getActor().isOnWater()) {
            boolean islandEscapable = false;
            LinkedList<Tile> trees = map.getObstacles().get(Obstacle.TREE);
            LinkedList<Tile> stones = map.getTools().get(Tool.STONE);
            //if there is a tree in the same zone, the island is escapable
            for (Tile tree : trees) {
                if (tree.getZone() == goalNode.getTile().getZone()) {
                    islandEscapable = true;
                }
            }
            if(stonesOK) {
                //if there is a stone in the same zone, the island is escapable
                for (Tile stone : stones) {
                    if (stone.getZone() == goalNode.getTile().getZone()) {
                        islandEscapable = true;
                    }
                }
            }
            if (!islandEscapable) {
                return null;
            }
        }
        while (!unexplored.isEmpty()) {
            current = unexplored.poll();
            for (Node neighbour : current.getTile().getNeighbours()) {
                Tile neighbourTile = map.getMap().get(neighbour.getValue());

                int add = 1;
                if (neighbour.getValue() == goalNode.getTile().getPoint()) {
                    cameFrom.put(goalNode.getTile(), current.getTile());
                    return cameFrom;
                }

                if (neighbourTile.getZone() != current.getTile().getZone() && map.getActor().isOnWater()) {
                    boolean islandEscapable = false;
                    LinkedList<Tile> trees = map.getObstacles().get(Obstacle.TREE);
                    LinkedList<Tile> stones = map.getTools().get(Tool.STONE);
                    //if there is a tree in the same zone, the island is escapable
                    for (Tile tree : trees) {
                        if (tree.getZone() == neighbourTile.getZone()) {
                            islandEscapable = true;
                        }
                    } if(stonesOK) {
                        //if there is a stone in the same zone, the island is escapable
                        for (Tile stone : stones) {
                            if (stone.getZone() == goalNode.getTile().getZone()) {
                                islandEscapable = true;
                            }
                        }
                    }
                    if (!islandEscapable) {
                        continue;
                    }
                }
                //Checks for whether this tile is allowed
                if ((map.getMap().get(neighbour.getValue()).getObstacle() != Obstacle.WATER) && !landOK) {
                    continue;
                }
                if ((map.getMap().get(neighbour.getValue()).getObstacle() == Obstacle.DOOR)
                        && !map.getActor().hasKey()) {
                    continue;
                }
                if (map.getMap().get(neighbour.getValue()).getObstacle() == Obstacle.WALL) {
                    continue;
                }
                if (map.getMap().get(neighbour.getValue()).getObstacle() == Obstacle.TREE && !treeOK) {
                    continue;
                }
                if (map.getMap().get(neighbour.getValue()).getObstacle() == Obstacle.OUTSIDE) {
                    continue;
                }
                if (map.getMap().get(neighbour.getValue()).getObstacle() == Obstacle.WATER) {
                    if (waterOK == false) {
                        continue;
                    }
                }
                if (map.getMap().get(neighbour.getValue()).getTool() == Tool.STONE && !stonesOK) {
                    continue;
                }

                SearchNode sNode = new SearchNode(map.getMap().get(neighbour.getValue()));
                sNode.setgCost(current.getgCost() + Heuristic.manhattan(
                        current, sNode, direction));
                sNode.sethCost(Heuristic.manhattan(sNode, goalNode, direction));
                if (sNode.getTile().getObstacle() == Obstacle.WATER) {
                    sNode.setfCost(sNode.getfCost() + 160);
                }
                if (sNode.getTile().getObstacle() == Obstacle.TREE) {
                    sNode.setfCost(sNode.getfCost() + 160);
                }
                for (SearchNode s : unexplored) {
                    if (s.getTile() == sNode.getTile() &&
                            s.getfCost() <= sNode.getfCost()) {
                        add = 0;
                        break;
                    }
                }
                for (SearchNode s : explored) {
                    if (s.getTile() == sNode.getTile() &&
                            s.getfCost() <= sNode.getfCost()) {
                        add = 0;
                        break;
                    }
                }
                if (add == 1) {
                    cameFrom.put(sNode.getTile(), current.getTile());
                    unexplored.add(sNode);
                }
            }
            explored.add(current);
        }
        return null;
    }

    //Reconstructs a path from the hashtable returned by getPath
    public Tile[] reconstructPath(
            Hashtable<Tile, Tile> cameFrom) {
        ArrayList<Tile> totalPath = new ArrayList<>();
        Tile current = goalNode.getTile();
        Tile currTile = current;
        totalPath.add(currTile);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            currTile = current;
            totalPath.add(currTile);
        }
        Collections.reverse(totalPath);
        return totalPath.toArray(new Tile[totalPath.size()]);
    }


}



import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;

public class WorldMap {
    private final HashMap<Point, Tile> map = new HashMap<Point, Tile>();
    private final HashMap<Obstacle, LinkedList<Tile>> obstacles = new HashMap<>();
    private final HashMap<Tool, LinkedList<Tile>> tools = new HashMap<>();
    private final Actor actor;
    private int minX=0;
    private int minY=0;
    private int maxX=0;
    private int maxY=0;
    private Integer zoneNumber = 1;

    public HashMap<Point, Tile> getMap() {
        return map;
    }

    public HashMap<Obstacle, LinkedList<Tile>> getObstacles() {
        return obstacles;
    }

    public WorldMap() {
        Point point = new Point();
        addTile(point,Obstacle.NONE, Tool.NONE, zoneNumber);
        actor = new Actor(map.get(point));
        obstacles.put(Obstacle.DOOR, new LinkedList<>());
        obstacles.put(Obstacle.WALL, new LinkedList<>());
        obstacles.put(Obstacle.TREE, new LinkedList<>());
        obstacles.put(Obstacle.WATER, new LinkedList<>());
        tools.put(Tool.KEY, new LinkedList<>());
        tools.put(Tool.AXE, new LinkedList<>());
        tools.put(Tool.TREASURE, new LinkedList<>());
        tools.put(Tool.STONE, new LinkedList<>());
        tools.put(Tool.PLACEDSTONE, new LinkedList<>());
    }

    private Tile addTile(Point position, Obstacle obstacle, Tool tool, Integer zoneNumber) {

        if(map.containsKey(position)) {
            return null;
        }

        Tile tile = new Tile(position, obstacle, tool, zoneNumber);
        map.put(position, tile);
        tile.explored();
        Tile upTile = map.get(new Point((int)position.getX(), (int)position.getY()+1));
        Tile rightTile = map.get(new Point((int)position.getX()+1, (int)position.getY()));
        Tile downTile = map.get(new Point((int)position.getX(), (int)position.getY()-1));
        Tile leftTile = map.get(new Point((int)position.getX()-1, (int)position.getY()));

        //Update neighbours
        if(upTile!= null) {
            tile.addNeighbour(upTile);
            upTile.addNeighbour(tile);
        }
        if(rightTile!=null) {
            tile.addNeighbour(rightTile);
            rightTile.addNeighbour(tile);
        }
        if(downTile!=null) {
            tile.addNeighbour(downTile);
            downTile.addNeighbour(tile);
        }
        if(leftTile!=null) {
            tile.addNeighbour(leftTile);
            leftTile.addNeighbour(tile);
        }
        return tile;
    }


    //UPDATING THE HASH MAP WITH COORDINATES
    //Store the position of an tile/blank space/object considering the rotation level
    //This system considers the direction the agent is initially facing as 'positive y', behind it is 'negative y'
    //To the right (East) of the initial agent position is 'positive x', to the left (West) is 'negative x'
    //It is like a Cartesian plane based off agent starting position
    public void updateMap(char view[][]) {

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2) {
                    continue;
                } else {
                    char tileChar = view[i][j];
                    Point point = rotate(i, j);
                    int x = (int) point.getX();
                    int y = (int) point.getY();

                    //Update min and max x coordinate for map printing
                    if (x < minX) {
                        minX = x;
                    } else if (x > maxX) {
                        maxX = x;
                    }

                    //Update min and max y coordinate for map printing
                    if (y < minY) {
                        minY = y;
                    } else if (y > maxY) {
                        maxY = y;
                    }

                    if (!map.containsKey(point)) {
                        Tile tile = addTile(point, Obstacle.fromChar(tileChar), Tool.fromChar(tileChar), zoneNumber);

                        tile.setZone(-1);

                        for(Node node : tile.getNeighbours()) {
                            Tile neighbour = (Tile) node;
                            if(sameZoneType(tile, neighbour)) {
                                tile.setZone(neighbour.getZone());
                                break;
                            }
                        }

                        if(tile.getZone() == -1) tile.setZone(++zoneNumber);
                        updateZones(tile);

                        switch (tile.getObstacle()) {
                            case WALL:
                                obstacles.get(Obstacle.WALL).add(tile);
                                break;
                            case DOOR:
                                obstacles.get(Obstacle.DOOR).add(tile);
                                break;
                            case TREE:
                                obstacles.get(Obstacle.TREE).add(tile);
                                break;
                            case WATER:
                                obstacles.get(Obstacle.WATER).add(tile);
                        }
                        if (tile.getTool() != Tool.NONE) tools.get(tile.getTool()).add(tile);
                    }
                }
            }
        }

        if(actor.isOnWater()) {
            actor.setHasRaft(false);
        }
        updateZones(actor.getTile());
    }

    //Rotates the i,j values from view to match the cartesian coordinates for out larger worldmap
    private Point rotate(int i, int j) {
        int x = (int)actor.getPosition().getX();
        int y = (int)actor.getPosition().getY();
        switch (actor.getDirection()) {
            case UP:
                x += (j-2);
                y += (2-i);
                break;
            case RIGHT:
                x += (2-i);
                y += (2-j);
                break;
            case DOWN:
                x += (2-j);
                y += (i-2);
                break;
            case LEFT:
                x += (i-2);
                y += (j-2);
                break;
        }
        return new Point(x,y);
    }

    public Actor getActor() {
        return actor;
    }

    //Prints a nice map
    public void printMap(String style) {
        Tile tile = null;
        int width = maxX - minX;
        System.out.print("\n+");
        for(int i = 0; i <= width; i++) System.out.print("-");
        System.out.print("+\n");
        for(int y = maxY; y >= minY; y--) {
            System.out.print("|");
            for(int x = minX; x <= maxX; x++) {
                if(x==actor.getPosition().getX()&&y==actor.getPosition().getY()) {
                    System.out.print("A");
                } else {
                    tile = map.get(new Point(x, y));
                    if(tile==null) {
                        System.out.print("?");
                    } else {
                        switch(style) {
                            case "ZONE":
                                if (tile.getObstacle().equals(Obstacle.OUTSIDE)) {
                                    System.out.print('.');
                                }
                                else {
                                    System.out.print((char) (int) (35 + (tile.getZone()) % (122 - 35)));
                                }
                            break;
                            case "STEP":
                            if (tile.getTool() == Tool.NONE && tile.getObstacle() == Obstacle.NONE) {
                                System.out.print(' ');
                            }
                            else if (tile.getTool() == Tool.NONE) {
                               System.out.print(tile.getObstacle().getChar());
                            }
                            else if (tile.getObstacle() == Obstacle.NONE) {
                                System.out.print(tile.getTool().getChar());
                            }
                        }
                    }
                }
            }
            System.out.print("|\n");
        }
        System.out.print("+");
        for(int i = 0; i <= width; i++) System.out.print("-");
        System.out.print("+\n");
    }

    public HashMap<Tool, LinkedList<Tile>> getTools() {
        return tools;
    }

    //Used by the reachable class to see if a tile can be gone through
    public boolean isPassable(Tile tile, boolean strict) {
        Obstacle obstacle = tile.getObstacle();
        if(obstacle == Obstacle.WALL) {
            return false;
        } else if(obstacle == Obstacle.DOOR && !actor.hasKey()) {
            return false;
        } else if(obstacle == Obstacle.TREE && !actor.hasAxe()) {
            return false;
        } else if(obstacle == Obstacle.OUTSIDE) {
            return false;
        } else if(obstacle == Obstacle.WATER && strict &&
                (!actor.hasRaft() && !actor.isOnWater()) && (actor.getStoneCount() <= 0)) {
            return false;
        } else if(tile == null){
            System.out.print("A ? tile\n");
            return false; //this is for '?' tiles
        } else {
            return true;
        }

    }

    //Describes actions necessary when a tree must be chopped
    public void chop() {
        Tile newTile;
        if (actor.hasAxe()) {
            switch(actor.getDirection()) {
                case UP:
                    newTile = map.get(actor.getTile().getUpNeighbour().getValue());
                    break;
                case RIGHT:
                    newTile = map.get(actor.getTile().getRightNeighbour().getValue());
                    break;
                case DOWN:
                    newTile = map.get(actor.getTile().getDownNeighbour().getValue());
                    break;
                default:
                    newTile = map.get(actor.getTile().getLeftNeighbour().getValue());
                    break;

            }

            if (newTile.getObstacle() == Obstacle.TREE) {
                actor.setHasRaft(true);
                newTile.setObstacle(Obstacle.NONE);
                obstacles.get(Obstacle.TREE).remove(newTile);
                updateZones(newTile);
            }
        }
    }

    //Describes actions necessary when a door must be unlocked
    public void unlock() {
        Tile newTile;
        if (actor.hasKey()) {
            switch(actor.getDirection()) {
                case UP:
                    newTile = map.get(actor.getTile().getUpNeighbour().getValue());
                    break;
                case RIGHT:
                    newTile = map.get(actor.getTile().getRightNeighbour().getValue());
                    break;
                case DOWN:
                    newTile = map.get(actor.getTile().getDownNeighbour().getValue());
                    break;
                default:
                    newTile = map.get(actor.getTile().getLeftNeighbour().getValue());
            }
            if (newTile.getObstacle() == Obstacle.DOOR) {
                newTile.setObstacle(Obstacle.NONE);
                obstacles.get(Obstacle.DOOR).remove(newTile);
                updateZones(newTile);
            }
        }
    }

    public boolean sameZoneType(Tile tile, Tile target) {
       switch(tile.getObstacle()) {
            case WATER:
                return target.getObstacle() == Obstacle.WATER;
            case TREE:
                return (target.getObstacle() == Obstacle.NONE && actor.hasAxe());
            case DOOR:
                return (target.getObstacle() == Obstacle.NONE && actor.hasKey());
            case WALL:
                return target.getObstacle() == Obstacle.WALL;
            case NONE:
                return ((target.getObstacle() == Obstacle.NONE) ||
                        (target.getObstacle() == Obstacle.TREE && actor.hasAxe()) ||
                        (target.getObstacle() == Obstacle.DOOR && actor.hasKey()));
            case OUTSIDE:
                return target.getObstacle() == Obstacle.OUTSIDE;
        }

        return false;
    }

    //Updates the zones for example once the axe has been picked up
    public void updateZones(Tile tile)
    {
        for(Node node : tile.getNeighbours()) {
            Tile neighbour = (Tile) node;
            if(sameZoneType(tile, neighbour) && tile.getZone() != neighbour.getZone()) {
                mergeZones(tile);
            }
        }
    }

    //Merges zones if the actor can pass between them
    private void mergeZones(Tile tile)
    {
        int zone = ++zoneNumber;

        HashSet<Tile> expanded = new HashSet<>();
        LinkedList<Tile> unexpanded = new LinkedList<>();

        expanded.add(tile);
        unexpanded.add(tile);

        while((tile = unexpanded.poll()) != null) {
            tile.setZone(zone);
            for(Node node : tile.getNeighbours()) {
                Tile neighbour = (Tile) node;
                if(sameZoneType(neighbour, tile) && !expanded.contains(neighbour)) {
                    unexpanded.add(neighbour);
                    expanded.add(neighbour);
                }
            }
        }
    }


}
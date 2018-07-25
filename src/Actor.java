import java.awt.*;
import java.util.LinkedList;

public class Actor {
    private Tile tile;
    private Direction direction = Direction.UP;
    private boolean onWater = false;
    private boolean hasAxe = false;
    private boolean hasKey = false;
    private boolean hasRaft = false;
    private boolean hasGold = false;
    private boolean preventDeath = false;
    private int stoneCount = 0;

    public Actor(Tile tile) {
        this.tile = tile;
    }

    public Direction getDirection() {
        return direction;
    }

    public Tile getTile() {
        return tile;
    }

    public Point getPosition() {
        return tile.getPoint();
    }

    public boolean moveForward(WorldMap map) {
        Tile newTile = null;
        switch (direction) {
            case UP:
                newTile = (Tile) tile.getUpNeighbour();
                break;
            case RIGHT:
                newTile = (Tile) tile.getRightNeighbour();
                break;
            case DOWN:
                newTile = (Tile) tile.getDownNeighbour();
                break;
            case LEFT:
                newTile = (Tile) tile.getLeftNeighbour();
                break;
        }
        if (newTile == null) return false;

        if (newTile.getObstacle() != Obstacle.NONE) {
            if (newTile.getObstacle() == Obstacle.WATER) {
                if (stoneCount > 0) {
                    newTile.setObstacle(Obstacle.NONE);
                    newTile.setTool(Tool.PLACEDSTONE);
                    stoneCount--;
                    tile = newTile;
                    return true;
                } else if (hasRaft) {
                    hasRaft = false;
                    onWater = true;
                    tile = newTile;
                } else if (onWater) {
                    tile = newTile;
                } else {
                    System.out.println("Stop everything");
                    setPreventDeath(true);
                }
            }
            return false;
        }

        if (newTile.getTool() != Tool.NONE && newTile.getTool() != Tool.PLACEDSTONE) {
            onWater = false;
            Tool tool = newTile.getTool();
            switch (tool) {
                case AXE:
                    hasAxe = true;
                    LinkedList<Tile> trees = map.getObstacles().get(Obstacle.TREE);
                    for (Tile t : trees) {
                        map.updateZones(t);
                    }
                    break;
                case STONE:
                    map.getTools().get(Tool.STONE).remove(newTile);
                    stoneCount++;
                    break;
                case KEY:
                    hasKey = true;
                    LinkedList<Tile> doors = map.getObstacles().get(Obstacle.DOOR);
                    for (Tile t : doors) {
                        map.updateZones(t);
                    }
                    break;
                case TREASURE:
                    hasGold = true;
            }
            newTile.setTool(Tool.NONE);
        }
        onWater = false;
        tile = newTile;
        return true;
    }

    public void turnLeft() {
        switch (direction) {
            case UP:
                direction = Direction.LEFT;
                break;
            case RIGHT:
                direction = Direction.UP;
                break;
            case DOWN:
                direction = Direction.RIGHT;
                break;
            case LEFT:
                direction = Direction.DOWN;
                break;
        }
    }

    public void turnRight() {
        switch (direction) {
            case UP:
                this.direction = Direction.RIGHT;
                break;
            case RIGHT:
                this.direction = Direction.DOWN;
                break;
            case DOWN:
                this.direction = Direction.LEFT;
                break;
            case LEFT:
                this.direction = Direction.UP;
                break;
        }
    }

    public boolean isPreventDeath() {
        return preventDeath;
    }

    public void setPreventDeath(boolean preventDeath) {
        this.preventDeath = preventDeath;
    }

    public boolean hasGold() {
        return hasGold;
    }

    public boolean hasAxe() {
        return hasAxe;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public boolean hasRaft() {
        return hasRaft;
    }

    public void setHasRaft(Boolean hasRaft) {
        this.hasRaft = hasRaft;
    }

    public int getStoneCount() {
        return stoneCount;
    }

    public Boolean isOnWater() {
        return onWater;
    }

}

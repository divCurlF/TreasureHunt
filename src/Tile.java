import java.awt.*;

//A tile is a square on the map and can have an obstacle and/or a tool as well as a zone number
public class Tile extends Node<Point> {

    private Tool tool;
    private Obstacle obstacle;
    private boolean explored;
    private Integer zone;

    public Tile(Point value, Obstacle obstacle, Tool tool, Integer zone) {
        super(value);
        this.obstacle=obstacle;
        this.tool=tool;
        this.zone = zone;
    }

    public Tile(Point value) {
        this(value, Obstacle.NONE, Tool.NONE, 0);
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public Obstacle getObstacle() {
        return obstacle;
    }

    public void setObstacle(Obstacle obstacle) {
        this.obstacle = obstacle;
    }

    public Node getNeighbour(Direction direction) {
        Point neighbourPoint = new Point();
        switch(direction) {
            case UP:
                neighbourPoint = new Point((int)this.getPoint().getX(), (int)this.getPoint().getY()+1);
                break;
            case RIGHT:
                neighbourPoint = new Point((int)this.getPoint().getX()+1, (int)this.getPoint().getY());
                break;
            case DOWN:
                neighbourPoint = new Point((int)this.getPoint().getX(), (int)this.getPoint().getY()-1);
                break;
            case LEFT:
                neighbourPoint = new Point((int)this.getPoint().getX()-1, (int)this.getPoint().getY());
                break;
        }

        Node[] nodes = this.getNeighbours();

        for(Node neighbour: nodes) {
            Tile neighbourTile=(Tile)neighbour;
            if (neighbourTile == null) {
                continue;
            }
            if(neighbourTile.getPoint().equals(neighbourPoint)) {
                return neighbourTile;
            }
        }
        return null;
    }
    /**
     An explored tile is one that either the player has stood in or that if
     the player were to stand on the tile they would not see any new tiles that
     they have never seen before.

     Sets this tile to be explored and then automatically finds all other tiles
     around this tile that are equivalently explored (that is, even if the
     player has not stood in that tile before, if they were to they would not
     gain any new information for doing so.
     */
    public void explored()
    {
        explored = true;
        updateExplored(Direction.UP, (Tile)this.getUpNeighbour(), 5);
        updateExplored(Direction.RIGHT, (Tile)this.getRightNeighbour(), 5);
        updateExplored(Direction.DOWN, (Tile)this.getDownNeighbour(), 5);
        updateExplored(Direction.LEFT, (Tile)this.getLeftNeighbour(), 5);
    }

    /**
     * Helper function for explored() used to recursively find all tiles that are
     * considered explored based on the definition given in the explored()
     * description.
     */
    private boolean updateExplored(Direction direction, Tile tile, int distance) {
        if (distance <= 0 || tile == null) return false;
        if (tile.isExplored() || tile.getObstacle() == Obstacle.OUTSIDE) return true;
        if (updateExplored(direction, (Tile)tile.getNeighbour(direction), distance - 1)) {
            tile.explored();
            return true;
        } else return false;
    }

    public boolean isExplored() {
        return explored;
    }

    public Node getUpNeighbour() {
        return getNeighbour(Direction.UP);
    }

    public Node getRightNeighbour() {
        return getNeighbour(Direction.RIGHT);
    }

    public Node getDownNeighbour() {
        return getNeighbour(Direction.DOWN);
    }

    public Node getLeftNeighbour() {
        return getNeighbour(Direction.LEFT);
    }

    public Integer getZone() {return zone;}

    public Integer setZone(Integer zone) {this.zone = zone; return zone;}

    public Point getPoint() {
        return getValue();
    }

}

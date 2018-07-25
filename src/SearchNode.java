
//Creates a Node that is just a tile but with the f, g and h values needed for the A* search
public class SearchNode {
    private int gCost;
    private int hCost;
    private int fCost;
    private Tile tile;

    public SearchNode(Tile tile, int gCost, int hCost, int fCost) {
        this.tile=tile;
        this.gCost=gCost;
        this.hCost=hCost;
        this.fCost=fCost;
    }
    public SearchNode(Tile tile) {
        this.tile=tile;
    }


    public Tile getTile() {
        return tile;
    }

    public int getgCost() {
        return gCost;
    }

    //fValue is updated automatically if gCost updated
    public void setgCost(int gCost) {
        this.gCost = gCost;
        setfCost(gCost+hCost);
    }

    //fValue is updated automatically if hCost updated
    public void sethCost(int hCost) {
        this.hCost = hCost;
        setfCost(gCost+hCost);
    }

    public int getfCost() {
        return fCost;
    }

    public void setfCost(int fCost) {
        this.fCost = fCost;
    }
}

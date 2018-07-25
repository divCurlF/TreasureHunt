import java.awt.*;

//This is a standard Manhattan heuristic but includes the number of turns the actor must take to get from start to goal
public class Heuristic {
    public static int manhattan(SearchNode currNode, SearchNode goalNode, Direction direction) {
        Point currPoint = currNode.getTile().getPoint();
        Point goalPoint = goalNode.getTile().getPoint();
        int xDiff = (int) (goalPoint.getX() - currPoint.getX());
        int yDiff = (int) (goalPoint.getY() - currPoint.getY());
        int turns=0;
        switch(direction) {
            case UP:
                if(yDiff<0) turns+=2;
                if(xDiff!=0) turns++;
                break;
            case RIGHT:
                if(xDiff<0) turns+=2;
                if(yDiff!=0) turns++;
                break;
            case DOWN:
                if(yDiff>0) turns+=2;
                if(xDiff!=0) turns++;
                break;
            case LEFT:
                if(xDiff>0) turns+=2;
                if(yDiff!=0) turns++;
        }

        int distance = Math.abs(xDiff) + Math.abs(yDiff) + turns;
        return distance;
    }


}

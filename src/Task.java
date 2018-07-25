import java.util.LinkedList;

// This class is written in preparation for receiving a list of tiles as a path from A*
// It extends linked list for ease of used

public class Task extends LinkedList<Character> {

    public Task(WorldMap map, Tile[] tiles, Direction currentDirection) {
        //To access the list of moves this class generates from another class, just call the constructor,
        // i.e. Task tasks = new Task(map,path)
        //and then start polling tasks to get the next action
        Direction nextDirection;

        for(int i=1; i<tiles.length; i++) {
            Tile currentTile=tiles[i-1];
            Tile nextTile=tiles[i];

            if(currentTile.getUpNeighbour() == nextTile) nextDirection = Direction.UP;
            else if(currentTile.getLeftNeighbour() == nextTile) nextDirection = Direction.LEFT;
            else if(currentTile.getDownNeighbour() == nextTile) nextDirection = Direction.DOWN;
            else nextDirection = Direction.RIGHT;

            switch(nextDirection) {
                case UP:
                    switch(currentDirection) {
                        case LEFT: add('r'); break;
                        case DOWN: add('r'); add('r'); break;
                        case RIGHT: add('l'); break;
                    }
                    break;
                case LEFT:
                    switch (currentDirection) {
                        case DOWN: add('r'); break;
                        case RIGHT: add('r'); add('r'); break;
                        case UP: add('l'); break;
                    }
                    break;
                case DOWN:
                    switch (currentDirection) {
                        case RIGHT: add('r'); break;
                        case UP: add('r'); add('r'); break;
                        case LEFT: add('l'); break;
                    }
                    break;
                case RIGHT:
                    switch (currentDirection) {
                        case UP: add('r'); break;
                        case LEFT: add('r'); add('r'); break;
                        case DOWN: add('l'); break;
                    }
                    break;
            }

            currentDirection = nextDirection;
            if(nextTile.getObstacle()==Obstacle.DOOR) {
                add('u');
            } else if(nextTile.getObstacle()==Obstacle.TREE) {
                add('c');
            }
            add('f');
        }
    }

    public Task() {}
}

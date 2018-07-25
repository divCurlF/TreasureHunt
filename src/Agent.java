/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411/9414/9814 Artificial Intelligence
 *  UNSW Session 1, 2018
 *
 *  Assignment completed by Zachary Sanchez (z5194994) and Lauren Taylor (z5012318).
 *
 *  ANSWER TO THE QUESTION
 *  Our program works by, first of all, considering the starting position and direction of the 'Actor' as (0,0) and UP.
 *  The agent builds up the map as it moves around. Information about the world is stored in WorldMap, which uses a
 *  HashMap to store points (x,y) as a key, with a Tile as a value. The Tile stores information about the Tool and/or Obstacle
 *  present in the tile position. The parent of Tile - Node - provides functions to update and return the neighbours
 *  of a Tile. WorldMap also contains other information about the world such as a list of locations
 *  of tiles and tools again in HashMaps with the key as a tool/obstacle type and the value as a Linked List of Tiles.
 *  The Actor makes decisions based on the hierarchy in the get_AI_action() function. After every decision step, if Task
 *  remains empty, it moves onto the next step in the decision making process. Task is a class that extends Linked List
 *  and is able to convert a path of tiles into a list of characters describing the moves the Actor must make to take
 *  the path. To generate a path, the AI first chooses a destination. This may be a tile containing a particular tool
 *  (or the treasure) if the Actor is 'Gathering', or if it is 'Exploring', it will choose a destination tile based on
 *  the number of unexplored neighbours of that tile, divided by the distance it takes to get there. A chosen tile is
 *  determined to be 'Reachable' by our implementation of the 'Flood/Seed Fill' algorithm, which looks at the tiles
 *  connecting the start to the goal tile and attempts to find some way that is not blocked. Once reachability has been
 *  determined, we use an A* algorithm to find the shortest path to the goal.
 *
 *  Our A* heuristic is a variation on the Manhattan Heuristic, which uses the different in the x position of the
 *  start/goal tiles, plus the difference in y values of the tiles, but also counts the number of turns that must be
 *  made by the Actor as part of the 'distance'. This ensures that the heuristic value is always admissible.  Within the
 *  A*, we used a priority queue for the unexplored neighbours so that we can pass in our own comparator, FComparator,
 *  which looks at the F scores of the unexplored neighbours and prioritises those with the lowest values. We used an
 *  array list for the explored neighbours since we only needed to add to the end of the list, and array list is fast
 *  for this purpose. We decided to use an A* algorithm, since we had a good heuristic which makes the search optimal,
 *  so it will find the shortest path (which is good since we have a limited number of moves) and complete, so it will
 *  always find a solution if one exists. It is also much faster than Dijkstra's algorithm due to the heuristic which is
 *  necessary to avoid time-outs. Using A* would be a problem if the map size became too large, since it stores all
 *  explored and unexplored nodes in memory but since we know the map size is limited to 80x80, A* was still a good
 *  choice. We also decided to implement zoning, which allowed us primarily to determine if it is possible to get off an
 *  island before the Actor leaves the raft. The other option would have been to find a path onto an island, find a path
 *  to the tool needed to leave the island, and then a path back off the island, which we determined would be too
 *  computationally expensive.
 */

import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class Agent {
    private final WorldMap map = new WorldMap();
    Task task = new Task();

    public char get_action(char view[][]) {
        char c = get_AI_action(view);
        return c;
    }

    private char get_AI_action(char view[][]) {
        map.updateMap(view);
        //map.printMap("STEP");
        if(map.getActor().hasGold() && task.isEmpty()) {
            generateAStar(map.getMap().get(new Point(0,0)), false, true, false, true);
            if (task.isEmpty()) {
                generateAStar(map.getMap().get(new Point(0,0)), true, true, false, true);
            }
            if (task.isEmpty()) {
                generateAStar(map.getMap().get(new Point(0,0)), true, true, true, true);
            }
        }

        //The following is the decision chain, if the path is impossible for one of them,
        // //it moves onto the next possibility
        if(!map.getActor().isOnWater()) {
            if (task.isEmpty()) {
                gather(false, true, false, false);
            }
            if (task.isEmpty()) {
                explore(false, true, false, false);
            }
            if (task.isEmpty()) {
                gather(true, true, false, false);
            }
            if (task.isEmpty()) {
                explore(true, true, false, false);
            }
            if (task.isEmpty()) {
                gather(true, true, true, false);
            }
            if(task.isEmpty()) {
                explore(true, true, true, false);
            }
        } else {
            if (task.isEmpty()) {
                explore(true, false, false, false);
            }
            if (task.isEmpty()) {
                gather(true, true, true, false);
            }
            if (task.isEmpty()) {
                explore(true, true, true, false);
            }
            if (task.isEmpty()) {
                gather(true, true, true, true);
            }
            if (task.isEmpty()) {
                explore(true, true, true, true);
            }
        }

        char c = task.poll();

        switch(Action.fromChar(c)) {
            case FORWARD:
                map.getActor().moveForward(map);
                break;
            case LEFT:
                map.getActor().turnLeft();
                break;
            case RIGHT:
                map.getActor().turnRight();
                break;
            case UNLOCK:
                map.unlock();
            case CHOP:
                map.chop();
        }
        if(map.getActor().isPreventDeath()) {
            task.clear();
            map.getActor().setPreventDeath(false);
            return 0;
        }
        return c;
    }

    //Finds a specific item to gather, if a certain item is unreachable,
    // it moves onto the next lower priority possibility
    public void gather(boolean waterOK, boolean landOK, boolean treeOK, boolean stonesOK) {
        HashMap<Tool, LinkedList<Tile>> collectibles = map.getTools();
        LinkedList<Tile> trees = map.getObstacles().get(Obstacle.TREE);

        //Separate Gather mechanism when the agent is not sailing

        if(!map.getActor().isOnWater()) {

            //First priority is to get the treasure.
            if (!collectibles.get(Tool.TREASURE).isEmpty()) {
               Reachable tester = new Reachable(map, map.getActor().getTile(), collectibles.get(Tool.TREASURE).peek(), false);
               if (tester.isReachable()) {
                    generateAStar(collectibles.get(Tool.TREASURE).peek(), waterOK, landOK, treeOK, stonesOK);
                }
            }

            //Second priority is to get a key
            if (!collectibles.get(Tool.KEY).isEmpty()) {
                Reachable tester = new Reachable(map, map.getActor().getTile(), collectibles.get(Tool.KEY).peek(), false);
                if (tester.isReachable()) {
                    generateAStar(collectibles.get(Tool.KEY).poll(), waterOK, landOK, treeOK, stonesOK);
                }
            }

            //Third priority is to get an axe
            if (!collectibles.get(Tool.AXE).isEmpty()) {
                Reachable tester = new Reachable(map, map.getActor().getTile(), collectibles.get(Tool.AXE).peek(), false);
                if (tester.isReachable()) {
                    generateAStar(collectibles.get(Tool.AXE).poll(), waterOK, landOK, treeOK, stonesOK);
                }
            }
        } else {

            if (!collectibles.get(Tool.TREASURE).isEmpty()) {
                Reachable tester = new Reachable(map, map.getActor().getTile(), collectibles.get(Tool.TREASURE).peek(), false);
                if (tester.isReachable()) {
                    generateAStar(collectibles.get(Tool.TREASURE).peek(), waterOK, landOK, treeOK, stonesOK);
                }
            }

            //First priority is to find a tree
            if (!trees.isEmpty() && map.getActor().hasAxe()) {
                Reachable tester = new Reachable(map, map.getActor().getTile(), trees.peek(), false);
                if (tester.isReachable()) {
                    generateAStar(trees.peek(), true, true, true, stonesOK);
                }
            }

            //Second priority is to get a stone
            if (!collectibles.get(Tool.STONE).isEmpty()) {
                Reachable tester = new Reachable(map, map.getActor().getTile(), collectibles.get(Tool.STONE).peek(), false);
                if (tester.isReachable()) {
                    generateAStar(collectibles.get(Tool.STONE).peek(), true, true, true, stonesOK);
                }
            }
        }
    }

    // Finds a tiles to explored based on most amount of unexplored neighbours it has, divided by distance
    public void explore(boolean waterOK, boolean landOK, boolean treeOK, boolean stonesOK) {
        double mostBeneficial=0.0;
        Tile leastExplored = map.getActor().getTile();
        for(Tile tile: map.getMap().values()) {
            if(tile!=map.getActor().getTile() && tile.getObstacle()!=Obstacle.OUTSIDE
                    && !(tile.getObstacle()==Obstacle.WATER && waterOK==false) &&tile.getObstacle()!=Obstacle.WALL) {
                double distance;
                if(tile.getObstacle()==Obstacle.TREE && treeOK==false) {
                    continue;
                } else {
                    distance = Heuristic.manhattan(new SearchNode(map.getActor().getTile()),
                            new SearchNode(tile), map.getActor().getDirection());
                }
                int numUnexplored = 0;
                Point upPoint = new Point((int) tile.getPoint().getX(), (int) tile.getPoint().getY() + 1);
                Point rightPoint = new Point((int) tile.getPoint().getX() + 1, (int) tile.getPoint().getY());
                Point downPoint = new Point((int) tile.getPoint().getX(), (int) tile.getPoint().getY() - 1);
                Point leftPoint = new Point((int) tile.getPoint().getX() - 1, (int) tile.getPoint().getY());
                Point[] neighbourPoints = {upPoint, rightPoint, downPoint, leftPoint};

                /*For each neighbour of the search node, if it is not in the map add to unexplored.
                  If it is in the map and it has not been explored, add to unexplored
                 */
                for (Point p : neighbourPoints) {
                    Boolean contains = map.getMap().containsKey(p);
                    if (contains == true) {
                        if (map.getMap().get(p).isExplored() == false) {
                            numUnexplored++;
                        }
                    } else if (contains == false) {
                        numUnexplored++;
                    }
                }

                //Quantify the value of exploring this node by how many unexplored tiles it finds per unit move.

                double result = 0;
                if (distance != 0) {
                    result = numUnexplored / distance;
                }

                //If the result is better than before, check if we can get there, then return this as the best tile.
                if (result > mostBeneficial) {
                    Reachable tester = new Reachable(map, map.getActor().getTile(), tile, false);
                    if (tester.isReachable()) {
                        mostBeneficial = result;
                        leastExplored = tile;
                    }
                }
            }
        }

        // Generate a path if we have found a valid tile to go to
        if (map.getActor().getTile() != leastExplored) {
            generateAStar(leastExplored, waterOK, landOK, treeOK, stonesOK);
        }
    }

    // Returns closest distance of a certain tile from the agent.
    private Tile closestTile(Actor agent, LinkedList<Tile> tools) {
        Tile agentTile = agent.getTile();
        Integer minDistance = Integer.MAX_VALUE;
        Integer currentDistance;
        Tile closest = new Tile(new Point(Integer.MAX_VALUE,Integer.MAX_VALUE));
        for (Tile tile : tools) {
            currentDistance = Heuristic.manhattan(new SearchNode(agentTile), new SearchNode(tile), agent.getDirection());
            if (currentDistance < minDistance) {
                closest = tile;
                minDistance = currentDistance;
            }
        }
        if (closest == new Tile(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE))) {
            return null;
        }

        return closest;
    }

    //Generates the A* to a tile, but if it determines it needs stones or a raft to complete the path, it will
    //send the agent there first to pick it up.
    public void generateAStar(Tile tile, boolean waterOK, boolean landOK, boolean treeOK, boolean stonesOK) {
        Tile latestTile;

        /* Begin an AStar search to the tile. This tile is loosely "reachable" given the items the agent holds.
        Further checks need to be done if the path involves water.
        */

        AStar search = new AStar(map.getActor().getTile(), tile, map, map.getActor().getDirection());

        // generate the chain of child/parent tiles
        Hashtable<Tile,Tile> cameFrom = search.findPath(waterOK, landOK, treeOK, stonesOK);


        if(cameFrom!=null) {
            // reconstruct the path from the cameFrom hashtable
            Tile[] path = search.reconstructPath(cameFrom);

            //Count the number of water tiles so we can compare this to the number of stepping stones or check if a raft
            //is needed.
            if(waterOK==true) {
                boolean newLandZone=false;
                int numWaterTiles=0;
                for(Tile t: path) {
                    if(t.getObstacle()==Obstacle.WATER) {
                        numWaterTiles++;
                    } else if(t.getObstacle()!=Obstacle.WATER && t.getZone()!=path[0].getZone()) {
                        newLandZone=true;
                    }
                }
                //Keep track of last tile in the path and the second last tile for direction discrepancies.
                Tile startPoint = map.getActor().getTile();
                latestTile = startPoint;
                Direction startingDirection = map.getActor().getDirection();
                Direction latestDirection = startingDirection;

                boolean stonesReachable = true;
                while(numWaterTiles>0) {
                    //If we have enough stones to cross, attempt to cross. Otherwise we will try to get stones.
                    if(map.getActor().getStoneCount() >= numWaterTiles && newLandZone) { break; }

                    //LOGIC: We want to see if we can reach a new zone with the stepping stones, if we can't, we should
                    // leave them because we would waste them. For each water adjacent water tile in the current zone,
                    // see if any of the neighbours of the water tile are in a new zone. If the neighbour is in a new zone,
                    // Pick up a stone and make a path to the neighbour. Otherwise, check the neighbours of the water tile until
                    // we exhaust all the possible stones we could use. Then repeat for each adjacent tile. This will get
                    // Incredibly expensive if there are lots of stones and/or lots of water.

                    //Keep a hashset to reduce memory wastage.

                    //If we see stones and are not on water, try to collect the stones.


                    if(!map.getTools().get(Tool.STONE).isEmpty() && !map.getActor().isOnWater()
                            && stonesReachable==true && newLandZone==true) {
                        //Find a stone to go to prioritised by how close it is to the agent
                        LinkedList<Tile> tempStones = map.getTools().get(Tool.STONE);
                        Tile closestStone = closestTile(map.getActor(), tempStones);
                        Reachable tester = new Reachable(map, map.getActor().getTile(), closestStone, true);

                        while(!tester.isReachable()) {
                            if(tempStones.isEmpty()) {
                                stonesReachable = false;
                                break;
                            }

                            tempStones.remove(closestStone);
                            closestStone = closestTile(map.getActor(), tempStones);
                            tester = new Reachable(map, map.getActor().getTile(), closestStone, true);
                        }

                        if(!stonesReachable) { continue; }
                        //Generate a path to the stone and remove it.
                        AStar stoneSearch = new AStar(latestTile, closestStone, map, latestDirection);
                        map.getTools().get(Tool.STONE).remove(closestStone);
                        Hashtable<Tile,Tile> stoneCameFrom = stoneSearch.findPath(false, landOK, treeOK, stonesOK);
                        if(stoneCameFrom!=null) {
                            Tile[] stonePath = stoneSearch.reconstructPath(stoneCameFrom);
                            task.addAll(new Task(map, stonePath, latestDirection));
                            latestTile = stonePath[stonePath.length - 1];
                            latestDirection = findLastDirection(stonePath[stonePath.length - 1],
                                    stonePath[stonePath.length - 2]);
                            numWaterTiles--;
                        }

                        //Here we have no stones OR we are currently sailing.

                    } else {
                        //If we have a raft, we will use this to begin sailing to the target destination.
                        if(map.getActor().hasRaft()) {
                            latestTile=null;
                            break;
                        } else if (map.getActor().isOnWater()){
                            latestTile=null;
                            break;
                        } else {
                            if(map.getActor().hasAxe()) {

                                //Similar algorithm to stone reachable, finding the closest tree we can get to.

                                LinkedList<Tile> tempTrees = map.getObstacles().get(Obstacle.TREE);
                                Tile closestTree = closestTile(map.getActor(),map.getObstacles().get(Obstacle.TREE));
                                Reachable treeTester = new Reachable(map, map.getActor().getTile(), closestTree, true);

                                while(!treeTester.isReachable()) {
                                    tempTrees.remove(closestTree);
                                    closestTree = closestTile(map.getActor(), tempTrees);
                                    treeTester = new Reachable(map, map.getActor().getTile(), closestTree, true);
                                }

                                AStar treeSearch = new AStar(latestTile, closestTree, map, latestDirection);
                                map.getTools().get(Tool.AXE).remove(closestTree);

                                // We do not want to make a path that goes through water

                                Hashtable<Tile,Tile> treeCameFrom = treeSearch.findPath(
                                        false, landOK, treeOK, stonesOK);

                                // Add the path to the task
                                if(treeCameFrom!=null) {
                                    Tile[] treePath = treeSearch.reconstructPath(treeCameFrom);
                                    task.addAll(new Task(map, treePath, latestDirection));
                                    latestTile = treePath[treePath.length - 1];
                                }
                                break;
                            }
                        }
                    }
                }

                // Number of water tiles is zero, so return the path if it is valid.
                if(latestTile != null && latestTile != startPoint) {
                    return;
                }
            }
            task.addAll(new Task(map,path, map.getActor().getDirection()));
        }
    }

    private Direction findLastDirection(Tile latestTile, Tile secondLatestTile) {
        Direction direction;
        if (latestTile.getPoint().getX() < secondLatestTile.getPoint().getX()) {
            direction = Direction.LEFT;
        } else if (latestTile.getPoint().getX() > secondLatestTile.getPoint().getX()) {
            direction = Direction.RIGHT;
        } else if (latestTile.getPoint().getY() < secondLatestTile.getPoint().getY()) {
            direction = Direction.DOWN;
        } else if (latestTile.getPoint().getY() > secondLatestTile.getPoint().getY()) {
            direction = Direction.UP;
        }
        else {
            direction = null;
        }
        return direction;
    }

    void print_view(char view[][]) {
        int i, j;

        System.out.println("\n+-----+");
        for (i = 0; i < 5; i++) {
            System.out.print("|");
            for (j = 0; j < 5; j++) {
                if ((i == 2) && (j == 2)) {
                    System.out.print('^');
                } else {
                    System.out.print(view[i][j]);
                }
            }
            System.out.println("|");
        }
        System.out.println("+-----+");
    }

    public static void main(String[] args) {
        InputStream in = null;
        OutputStream out = null;
        Socket socket = null;
        Agent agent = new Agent();
        char view[][] = new char[5][5];
        char action = 'F';
        int port;
        int ch;
        int i, j;

        if (args.length < 2) {
            System.out.println("Usage: java Agent -p <port>\n");
            System.exit(-1);
        }

        port = Integer.parseInt(args[1]);

        try { // open socket to Game Engine
            socket = new Socket("localhost", port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Could not bind to port: " + port);
            System.exit(-1);
        }

        try { // scan 5-by-5 wintow around current location
            while (true) {
                for (i = 0; i < 5; i++) {
                    for (j = 0; j < 5; j++) {
                        if (!((i == 2) && (j == 2))) {
                            ch = in.read();
                            if (ch == -1) {
                                System.exit(-1);
                            }
                            view[i][j] = (char) ch;
                        }
                    }
                }
                //agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION
                action = agent.get_action(view);
                out.write(action);
            }
        } catch (IOException e) {
            System.out.println("Lost connection to port: " + port);
            System.exit(-1);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }



}

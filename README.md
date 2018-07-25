# TreasureHunt
An AI that plays a simple 2D adventure game.

---------------------------------------------------------------------

# Running the game [requires jre/jdk 8]

Game can be played by first going to the source directory and running **two** terminal windows:

**First window**:

Run the following to compile and run the host server.

```
javac *.java
java Step -p 31415 -i ../levels/s0.in
```
**Second window**

On the other window run:

```
java Agent -p 31415
```

You can also try the game on another map:

```
java Step -p 31415 -i ../levels/s1.in

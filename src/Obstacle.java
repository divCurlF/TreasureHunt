//Described the possible obstacles
public enum Obstacle {
    TREE('T'),
    DOOR('-'),
    WATER('~'),
    WALL('*'),
    OUTSIDE('.'),
    NONE('n');
    char c;

    Obstacle(char c) {
        this.c = c;
    }

    public static Obstacle fromChar(char c)
    {
        switch(c) {
            case 'T': return TREE;
            case '-': return DOOR;
            case '~': return WATER;
            case '*': return WALL;
            case '.': return OUTSIDE;
            default: return NONE;
        }
    }

    public char getChar() {
        return c;
    }
}

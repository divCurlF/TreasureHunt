//Describes the possible tools
public enum Tool {
    AXE('a'),
    KEY('k'),
    STONE('o'),
    TREASURE('$'),
    PLACEDSTONE('O'),
    NONE('n');
    char c;

    Tool(char c) {
        this.c = c;
    }

    public static Tool fromChar(char c)
    {
        switch(c) {
            case 'a': return AXE;
            case 'k': return KEY;
            case 'o': return STONE;
            case '$': return TREASURE;
            case 'O': return PLACEDSTONE;
            default: return NONE;
        }
    }

    public char getChar() {
        return c;
    }
}



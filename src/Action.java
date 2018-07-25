//Enumerated class which gives the action possibilities to make the code clearer (rather than using the chars)
public enum Action {
    FORWARD('f'), LEFT('l'), RIGHT('r'), CHOP('c'), UNLOCK('u'), INVALID('?');

    private final char key;

    Action(char key) {
        this.key = key;
    }

    public static char toChar(Action action) {
        return action.key;
    }

    public static Action fromChar(char c) {
        switch(c) {
            case 'F':
            case 'f':
                return FORWARD;
            case 'L':
            case 'l':
                return LEFT;
            case 'R':
            case 'r':
                return RIGHT;
            case 'C':
            case 'c':
                return CHOP;
            case 'U':
            case 'u':
                return UNLOCK;
            default:
                return INVALID;

        }
    }
}

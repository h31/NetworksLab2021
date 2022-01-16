package Utils;

public enum Command {

    GREETING('g'),
    TEXT('t'),
    CLOSE('c');

    private final char symbol;

    Command(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}

public class SymbolEntry {

    private String type;
    private SymbolTable.Kind kind;
    private int index;

    public SymbolEntry(String type, SymbolTable.Kind kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    // Getters and Setters
    public String getType() { return type; }
    public SymbolTable.Kind getKind() { return kind; }
    public int getIndex() { return index; }

    @Override
    public String toString() {
        return ", Type: " + type + ", Kind: " + kind + ", Index: " + index;
    }
}

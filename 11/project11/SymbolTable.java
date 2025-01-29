import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    int staticIndex, fieldIndex, argIndex, varIndex;
    Map<String, SymbolEntry> symbolTable;

    /**
     * Creates a new symbol table
     */
    public SymbolTable() {
        symbolTable = new HashMap<>();
    }

    /**
     * All possible identifier kinds
     */
    enum Kind {
        STATIC,
        FIELD,
        ARG,
        VAR,
        NONE;

        public VMWriter.Segment matchSegment(){
            return switch (this){
                case STATIC -> VMWriter.Segment.STATIC;
                case FIELD -> VMWriter.Segment.THIS;
                case ARG -> VMWriter.Segment.ARGUMENT;
                case VAR -> VMWriter.Segment.LOCAL;
                default -> null;
            };
        }
    }


    /**
     * Empties the symbol table, and resets the four
     * indexes to 0. Should be called when starting
     * to compile a subroutine declaration.
     */
    public void reset() {
        symbolTable.clear();
        staticIndex = 0;
        fieldIndex = 0;
        argIndex = 0;
        varIndex = 0;
    }

    /**
     * Adds to the table a new variable of the given name
     * type, and kind. Assigns to it the index value of that kind,
     * and adds 1 to the index.
     *
     * @param name
     * @param type
     * @param kind
     */
    public void define(String name, String type, Kind kind) {
        int index = 0;
        switch (kind) {
            case STATIC: {
                index = staticIndex;
                staticIndex++;
                break;
            }
            case FIELD: {
                index = fieldIndex;
                fieldIndex++;
                break;
            }
            case ARG: {
                index = argIndex;
                argIndex++;
                break;
            }
            case VAR: {
                index = varIndex;
                varIndex++;
                break;
            }

        }
        SymbolEntry entry = new SymbolEntry(type, kind, index);
        symbolTable.put(name, entry);
    }

    /**
     * Returns the number of variables of the given kind
     * already defined in the table
     *
     * @param knd
     * @return
     */
    public int varCount(Kind knd) {
        return switch (knd) {
            case STATIC -> staticIndex;// == 0 ? 0 : staticIndex + 1;
            case FIELD -> fieldIndex;// == 0 ? 0 : fieldIndex + 1;
            case ARG -> argIndex;// == 0 ? 0 : argIndex + 1;
            case VAR -> varIndex;// == 0 ? 0 : varIndex + 1;
            default -> 0;
        };
    }

    /**
     * Returns the kind of the named Identifier.
     * If the identifier is not found, returns NONE.
     *
     * @param name
     * @return
     */
    public Kind kindOf(String name) {
        SymbolEntry entry = symbolTable.get(name);
        return entry == null ? Kind.NONE : entry.getKind();
    }

    /**
     * Returns the type of the named variable.
     *
     * @param name
     * @return
     */
    public String typeOf(String name) {
        SymbolEntry entry = symbolTable.get(name);
        return entry == null ? "" : entry.getType();
    }

    /**
     * Returns the index of the named variable.
     *
     * @param name
     * @return
     */
    public int indexOf(String name) {
        SymbolEntry entry = symbolTable.get(name);
        return entry == null ? -1 : entry.getIndex();
    }

    //specified toString- used for developer friendly debugging
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Name|\tType|\tKind|\tIndex|\n");
        for(String name: symbolTable.keySet()){
            SymbolEntry e = symbolTable.get(name);
            str.append(name + "|\t" + e.getType() + "|\t" + e.getKind() + "|\t" + e.getIndex() + "|\n");
        }
        return str.toString();
    }

}

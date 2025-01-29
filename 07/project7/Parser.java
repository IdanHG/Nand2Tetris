import java.io.BufferedReader;
import java.io.*;


/**
 * The Parser class is responsible for parsing a VM source file.
 * It reads commands, determines their type, and extracts arguments for further processing.
 */
public class Parser {

    //Members
    private String curCommand;
    private BufferedReader reader;
    private final File file;

    /**
     * enum defines constant types for commands
     */
    enum Command {
        C_ARITHMENTIC,
        C_PUSH,
        C_POP
    }

    /**
     * Constructor: Initializes the Parser and opens the input VM file for reading.
     *
     * @param file The path to the VM source file.
     */
    public Parser(File file) throws IOException {
        this.reader = new BufferedReader(new FileReader(file));
        this.file = file;
        this.advance();
    }


    /**
     * Checks if there are more commands to process in the VM file.
     *
     * @return true if there are more commands, false otherwise.
     */
    public boolean hasMoreLines() throws IOException {
        reader.mark(1);
        int nextChar = reader.read();
        reader.reset();
        return nextChar != -1;
    }

    /**
     * Advances to the next command in the VM file.
     * Makes the next command the current command.
     */
    public void advance() throws IOException {
        if(hasMoreLines()){
            curCommand = (reader.readLine().trim());
            while ((curCommand.equals("")  || curCommand.startsWith("//")) && hasMoreLines()) {
                curCommand = (reader.readLine().trim());
            }
        }
        else curCommand = null;
    }

    /**
     * Returns the type of the current command.
     *
     * @return A string constant representing the command type:
     *         - C_ARITHMETIC: for arithmetic or logical commands.
     *         - C_PUSH, C_POP: for push or pop commands.
     */
    public Command commandType() {
        return switch(this.curCommand.split(" +")[0]) {
            case "push"-> Command.C_PUSH;
            case "pop"-> Command.C_POP;
            default ->  Command.C_ARITHMENTIC;
            };
    }

    /**
     * Returns the first argument of the current command.
     * In the case of C_ARITHMETIC, the command itself is returned.
     *
     * @return The first argument of the current command as a string.
     */
    public String arg1() {
        if(commandType() == Command.C_ARITHMENTIC) return this.curCommand;
        else {
            return this.curCommand.split(" +")[1];
        }
    }

    /**
     * Returns the second argument of the current command.
     * This method is called only if the command is C_PUSH, C_POP,
     * C_FUNCTION, or C_CALL.
     *
     * @return The second argument of the current command as an integer.
     */
    public int arg2() {
        if(commandType() == Command.C_PUSH || commandType() == Command.C_POP)
            return Integer.parseInt(this.curCommand.split(" +")[2]);
        else return  -1;
    }
}


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    BufferedWriter writer;

    /**
     * Creates a new output .vm file and prepares it for writing.
     * @param output
     */
    public VMWriter(File output) throws IOException {
        writer = new BufferedWriter(new FileWriter(output));
    }

    enum Segment{
        CONSTANT,
        ARGUMENT,
        LOCAL,
        STATIC,
        THIS,
        THAT,
        POINTER,
        TEMP;

    }

    enum Command{
        ADD,
        SUB,
        NEG,
        EQ,
        GT,
        LT,
        AND,
        OR,
        NOT;

        public static Command matchCommand(String op){
            return switch (op) {
                case "+" -> ADD;
                case "-" -> SUB;
                case "=" -> EQ;
                case ">" -> GT;
                case "<" -> LT;
                case "&" -> AND;
                case "|" -> OR;
                case "~" -> NOT;
                default -> null;
            };
        }
    }

    /**
     * Writes a VM push command.
     * @param segment
     * @param index
     */
    public void writePush(Segment segment, int index) throws IOException {
        writer.write("    push " + segment.toString().toLowerCase() + " " + index +"\n");
    }

    /**
     * Writes a VM pop command.
     * @param segment
     * @param index
     */
    public void writePop(Segment segment, int index) throws IOException {
        writer.write("    pop " + segment.toString().toLowerCase() + " " + index +"\n");

    }

    /**
     * Writes a VM Arithmetic-logical command.
     * @param cmd
     */
    public void writeArithmetic(Command cmd) throws IOException {
        writer.write("    " + cmd.toString().toLowerCase() + "\n");
    }

    /**
     * Writes a VM label command.
     * @param label
     */
    public void writeLabel(String label) throws IOException {
        writer.write("label " + label + "\n");
    }

    /**
     * Writes a VM goto command.
     * @param label
     */
    public void writeGoto(String label) throws IOException {
        writer.write("    goto " + label + "\n" );
    }

    /**
     * Writes a VM if-goto command.
     * @param label
     */
    public void writeIf(String label) throws IOException {
        writer.write("    if-goto " + label + "\n");
    }

    /**
     * Writes a VM call command.
     * @param name
     * @param nVars
     */
    public void writeCall(String name, int nVars) throws IOException {
        writer.write("    call " + name + " " + nVars + "\n");
    }

    /**
     * Writes a VM function command.
     * @param name
     * @param nVars
     */
    public void writeFunction(String name, int nVars) throws IOException {
        writer.write("function " + name + " " + nVars + "\n");
    }

    /**
     * Writes a VM return command.
     */
    public void writeReturn() throws IOException {
        writer.write("    return\n");
    }

    /**
     * Closes the output file.
     */
    public void close() throws IOException {
        writer.close();
    }

}
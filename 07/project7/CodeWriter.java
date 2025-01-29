import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The CodeWriter class generates assembly code from the parsed VM commands.
 * It writes the assembly commands into an output file or stream.
 */
public class CodeWriter {

    BufferedWriter writer;
    String filename;
    static int counter = 0;
    /**
     * Constructor: Opens the output file or stream and gets ready to write into it.
     *
     * @param outputFile The name of the output file where the assembly code will be written.
     */
    public CodeWriter(String outputFile) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFile));
        filename = outputFile.substring(outputFile.lastIndexOf("/") + 1, outputFile.lastIndexOf("."));
    }

    /**
     * Writes the assembly code that implements the given arithmetic-logical command.
     *
     * @param command The arithmetic or logical command (e.g., "add", "sub", "neg").
     */
    public void writeArithmetic(String command) throws IOException {
        writer.write("// "+ command);
        writer.newLine();
        String str="";
        switch(command){
            case "add":
                str = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "M=D+M";
                break;
            case "sub":
                str = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "M=M-D";
                break;
            case "neg":
                str = "@SP\n" +
                        "A=M\n" +
                        "A=A-1\n" +
                        "M=-M";
                break;
            case "eq":
                str = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "AM=M-1\n" +
                        "D=M-D\n" +
                        "@TRUE" + counter +"\n" +
                        "D;JEQ\n" +
                        "D=0\n" +
                        "@FALSE" + counter +"\n" +
                        "0;JMP\n" +
                        "(TRUE" + counter +")\n" +
                        "D=-1\n" +
                        "(FALSE" + counter +")\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1";
                counter++;
                break;
            case "gt":
                str = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "AM=M-1\n" +
                        "D=M-D\n" +
                        "@TRUE" + counter +"\n" +
                        "D;JGT\n" +
                        "D=0\n" +
                        "@FALSE" + counter +"\n" +
                        "0;JMP\n" +
                        "(TRUE" + counter +")\n" +
                        "D=-1\n" +
                        "(FALSE" + counter +")\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1";
                counter++;
                break;
            case "lt":
                str = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "AM=M-1\n" +
                        "D=M-D\n" +
                        "@TRUE" + counter +"\n" +
                        "D;JLT\n" +
                        "D=0\n" +
                        "@FALSE" + counter +"\n" +
                        "0;JMP\n" +
                        "(TRUE" + counter +")\n" +
                        "D=-1\n" +
                        "(FALSE" + counter +")\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1";
                counter++;
                break;

            case "and":
                str = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "M=D&M";
                break;

            case "or":
                str = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "M=D|M";
                break;

            case "not":
                str = "@SP\n" +
                        "A=M\n" +
                        "A=A-1\n" +
                        "M=!M";
                break;
            default:
                str="";
        }
        this.writer.write(str);
        this.writer.newLine();
    }

    /**
     * Writes the assembly code that implements the given push or pop command.
     *
     * @param command The command type (C_PUSH or C_POP).
     * @param segment The memory segment to operate on (e.g., "local", "argument").
     * @param i   The index within the memory segment.
     */
    public void writePushPop(Parser.Command command, String segment, int i) throws IOException {
        writer.write("// "+ command + " " + segment + " " +i);
        writer.newLine();
        String asmCommand = "";
        segment = parseSegment(segment);
        if(command == Parser.Command.C_POP) {
            switch (segment) {
                case "static" -> asmCommand = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "@" + this.filename + "." + i + "\n" +
                        "M=D";
                case "pointer" ->  asmCommand = "@THIS\n" +
                        "D=A\n" +
                        "@" + i + "\n" +
                        "D=D+A\n" +
                        "@R13\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M-1\n" +
                        "A=M\n" +
                        "D=M\n" +
                        "@13\n" +
                        "A=M\n" +
                        "M=D";

                case "temp" -> asmCommand = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "@" + (5+i) + "\n" +
                        "M=D";
                default -> asmCommand = "@" + segment + "\n" +
                        "D=M\n" +
                        "@" + i + "\n" +
                        "D=D+A\n" +
                        "@R15\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "@R15\n" +
                        "A=M\n" +
                        "M=D";
            }
        }
        else if(command == Parser.Command.C_PUSH) {
            switch (segment) {
                case "constant" -> asmCommand = "@" + i + "\n" +
                        "D=A\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1";
                case "static" -> asmCommand = "@" + this.filename + "." + i  + "\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1";
                case "pointer" -> {
                    asmCommand = "@THIS\n" +
                            "D=A\n" +
                            "@" + i +"\n" +
                            "A=D+A\n" +
                            "D=M\n" +
                            "@SP\n" +
                            "A=M\n" +
                            "M=D\n" +
                            "@SP\n" +
                            "M=M+1";
                }
                case "temp" -> {
                    asmCommand= "@" + (5+i) + "\n" +
                            "D=M\n" +
                            "@SP\n" +
                            "A=M\n" +
                            "M=D\n" +
                            "@SP\n" +
                            "M=M+1";
                }
                default -> asmCommand = "@" + segment + "\n" +
                        "D=M\n" +
                        "@" + i + "\n" +
                        "A=D+A\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "M=M+1";
            }
        }
        else asmCommand = "";

        this.writer.write(asmCommand);
        this.writer.newLine();
    }

    /**
     Auxilary method to parse the segment in writePushPop method.
     takes segment from command and return the desired register symbol. e.g "local" --> "LCL"
     @param segment second "word" in commant - only for push and pop commands
     @return string to be ued and asmCommand
     */
    private String parseSegment(String segment) {
        if(segment.equals("local")) return "LCL";
        else if(segment.equals("constant")) return "constant";
        else if(segment.equals("argument")) return "ARG";
        else if(segment.equals("this")) return "THIS";
        else if(segment.equals("that")) return "THAT";
        return segment;
    }

    /**
     * Closes the output file.
     */
    public void close() throws IOException {
        writer.write("(END)\n@END\n0;JMP\n");
        this.writer.close();
    }
}
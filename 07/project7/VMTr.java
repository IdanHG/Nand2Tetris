import java.io.File;
import java.io.IOException;

public class VMTr {

    public static void VMTr(String arg) throws IOException {
        Parser parser = new Parser(new File(arg));

        //Output file name ends with .hack instead of .asm
        String progName = arg.substring(0,arg.lastIndexOf(".")) + ".asm";
        CodeWriter writer = new CodeWriter(progName);

        boolean lastIteration = false;

        while(parser.hasMoreLines() || lastIteration) {

            if(parser.commandType() == Parser.Command.C_ARITHMENTIC) {
                writer.writeArithmetic(parser.arg1());
            }
            else {
                writer.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
            }
            //allow 1 more iteration once last line is reached
            if(!lastIteration) parser.advance();
            else break;
            if(!parser.hasMoreLines()) lastIteration = true;

        }
        writer.close();
    }
}
import java.io.*;

public class Main {

    public static  void main(String[] args) throws IOException, FileNotFoundException {
        Parser parser = new Parser(new File(args[0]));
        SymbolTable symbolTable = new SymbolTable();

        //Output file name ends with .hack instead of .asm
        String progName = args[0].substring(0,args[0].lastIndexOf(".")) + ".hack";
        BufferedWriter writer = new BufferedWriter(new FileWriter(progName));

        boolean lastIteration = false; // will be used for 1 more iteration in 2nd while

        //First pass, goes over all labels and adds them to the symbol table
        while(parser.hasMoreLines()) {
            if (parser.instructionsType() == Parser.Instructions.L_INSTRUCTION) {
                symbolTable.addEntry(parser.symbol(), Parser.curr_line);
            }
            parser.advance();
        }

        //Reset parser and cursor
        parser.reset();

        //Second pass, goes over all lines, translates them into binary code
        while(parser.hasMoreLines() || lastIteration) {
            String address="";

            //If line declares a variable (A.instruction)
            //and symbol table doesn't contain it
            if(parser.isSymbol()){
                if(!symbolTable.contains(parser.symbol())) {
                    symbolTable.addEntry(parser.symbol(), SymbolTable.varRegister);
                    SymbolTable.varRegister++;
                }

                //Gets the address of the symbol, translates it to binary
                String binaryString = Integer.toBinaryString(symbolTable.getAddress(parser.symbol()));
                address = String.format("%16s", binaryString).replace(' ', '0');

            }
            else if(parser.instructionsType() == Parser.Instructions.A_INSTRUCTION) {
                String binaryString = Integer.toBinaryString(Integer.parseInt(parser.symbol()));
                address = String.format("%16s", binaryString).replace(' ', '0');
            }
            //If line is a C.instruction translates it to binary code
            else if(parser.instructionsType() == Parser.Instructions.C_INSTRUCTION){
                address = "111" + Code.comp(parser.comp()) + Code.dest(parser.dest()) + Code.jump(parser.jump());
            }

            //Write the line into the output.txt
            if(parser.instructionsType() != Parser.Instructions.L_INSTRUCTION){
                writer.write(address);
                writer.newLine();
            }

            //allow 1 more iteration once last line is reached
            if(!lastIteration) parser.advance();
            else break;
            if(!parser.hasMoreLines()) lastIteration = true;
        }
        writer.close();
    }
}

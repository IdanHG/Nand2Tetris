import java.io.*;



public class Parser {

    //Members
    static int curr_line;
    private String current_instruction;
    private BufferedReader reader;
    private final File file;

    //Constant instruction types
    enum Instructions{
        A_INSTRUCTION,
        C_INSTRUCTION,
        L_INSTRUCTION
    }

    //Constructor
    public Parser(File file) throws FileNotFoundException, IOException {
        this.reader = new BufferedReader(new FileReader(file));
        this.file = file;
        this.advance();
    }

    //Methods

    //Checks if there is more work to do
    protected boolean hasMoreLines() throws IOException {
        reader.mark(1);
        int nexxtChar = reader.read();
        reader.reset();
        return nexxtChar != -1;
    }

    //Gets the next instruction and makes it the current instruction
    protected void advance() throws IOException{
        if(hasMoreLines()){
            current_instruction = (reader.readLine().trim());
            while ((current_instruction.equals("")  || current_instruction.startsWith("//")) && hasMoreLines()) {
                current_instruction = (reader.readLine().trim());
            }
        }
        else current_instruction = null;
        //Count line only if A or C instructions (labels don't count)
        if(instructionsType() != Instructions.L_INSTRUCTION){
            this.curr_line++;
        }
    }

    //Returns the type of the current instruction, as a constant
    //It's necessary to check that current_instruction is not null
    protected Instructions instructionsType(){
        if(current_instruction.charAt(0) == '@'){
            return Instructions.A_INSTRUCTION;
        } else if(current_instruction.charAt(0) == '('){
            return Instructions.L_INSTRUCTION;
        }
        else return Instructions.C_INSTRUCTION;
    }

    //Returns the instruction’s symbol (for @sum it returns sum, for (LOOP) returns LOOP)
    protected String symbol(){
        if(instructionsType() == Instructions.A_INSTRUCTION){
            return current_instruction.substring(1);
        }
        else if(instructionsType() == Instructions.L_INSTRUCTION){
            return current_instruction.substring(1, current_instruction.length()-1);
        }
        else return "";
    }

    //Checks whether current instructions contains alphabetic chars,
    // meaning it's a variable
    protected boolean isSymbol(){
        return current_instruction.matches(".*[a-zA-Z]+.*") && instructionsType() == Instructions.A_INSTRUCTION;
    }

    //Returns the instruction’s dest field (for D=D+1;JLE returns D)
    //Relevant for C instructions only
    protected String dest(){
        if(instructionsType() == Instructions.C_INSTRUCTION){
            if(current_instruction.contains("=")){
                return current_instruction.split("[=;]")[0];
            }
            else if(!current_instruction.contains("=") && current_instruction.contains(";")){
                return "";
            }
        }
        return "";
    }

    //Returns the instruction’s dest field (for D=D+1;JLE returns D+1)
    //Relevant for C instructions only
    protected String comp(){
        if(instructionsType() == Instructions.C_INSTRUCTION){
            if(current_instruction.contains("=") && current_instruction.contains(";")){
                return current_instruction.split("[=;]")[1];
            }
            else if(current_instruction.contains("=") && !current_instruction.contains(";")){
                return current_instruction.split("=")[1];
            }
            else if(!current_instruction.contains("=") && current_instruction.contains(";")){
                return current_instruction.split(";")[0];
            }
        }
        return "";
    }

    //Returns the instruction’s dest field (for D=D+1;JLE returns JLE)
    //Relevant for C instructions only
    protected String jump(){
        if(instructionsType() == Instructions.C_INSTRUCTION){
            if(current_instruction.contains("=") && current_instruction.contains(";")){
                return current_instruction.split("[=;]")[2];
            }
            else if(current_instruction.contains("=") && !current_instruction.contains(";")){
                return "";
            }
            else if(!current_instruction.contains("=") && current_instruction.contains(";")){
                return current_instruction.split(";")[1];
            }
        }
        return "";
    }

    //Starts reading the file from beginning
    protected void reset() throws IOException, FileNotFoundException {
        curr_line = 0;
        this.reader.close();
        this.reader = new BufferedReader(new FileReader(this.file));
        this.advance();
    }
}

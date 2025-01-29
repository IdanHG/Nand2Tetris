import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;

public class CompilationEngine {

    BufferedWriter writer;
    JackTokenizer tokenizer;
    static int indentLevel = 0;

    /**
     * Creates a new compilation engine with the given input and output.
     * @param inputFile xxx.Jack
     * @param outputFile xxx.XML
     * @throws IOException
     */
    public CompilationEngine(File inputFile, File outputFile) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFile));
        tokenizer = new JackTokenizer(inputFile);
        tokenizer.advance();
        compileClass();
        writer.flush();
        writer.close();
    }

    /**
     * Returns current level of indentation.
     * @return a string containing "  " indentLevel times
     */
    private String getIndentation(){
        StringBuilder indent = new StringBuilder();
        for(int i = 0; i < indentLevel; i++){
            indent.append("  ");
        }
        return indent.toString();
    }

    /**
     * Helper method
     * Handles the current input token and advanced the input
     * @param str
     * @throws IOException
     */
    public void process(String str) throws IOException {

        if(tokenizer.getCurrToken().equals(str)){

            //If str is string_const, print it without " "
            if(tokenizer.tokenType() == JackTokenizer.Tokens.STRING_CONST){
                str = str.substring(1, str.length()-1);
            }

            //Change these symbols as requested
            switch(str){
                case "<" -> str = "&lt;p";
                case ">" -> str = "&gt;";
                case "\"" -> str = "&quot;";
                case "&" -> str = "&amp;";
            }
            printXMLToken(str);
        }
        else{
            writer.write("Syntax Error\n");
        }
        tokenizer.advance();
    }

    /**
     * Prints graphic tag representation for basic token types.
     * @param str
     * @throws IOException
     */
    public void printXMLToken(String str) throws IOException {
        String type;
        switch(tokenizer.tokenType()){
            case IDENTIFIER -> type = "identifier";
            case INT_CONST -> type = "integerConstant";
            case STRING_CONST -> type = "stringConstant";
            case SYMBOL -> type = "symbol";
            case KEYWORD -> type = "keyword";
            default -> type = "unknown";
        }
        writer.write(getIndentation() + "<" + type + "> " + str + " </" + type + ">\n");
    }


    /**
     * Compiles a complete class
     */
    public void compileClass() throws IOException {
        writer.write(getIndentation() + "<class>\n");
        indentLevel++;
        process("class");

        //className
        process(tokenizer.getCurrToken());
        process("{");

        //classVarDec*
        while(tokenizer.getCurrToken().equals("static") || tokenizer.getCurrToken().equals("field")){
            compileClassVarDec();
        }

        //subRoutineDec*
        while(tokenizer.getCurrToken().equals("constructor") || tokenizer.getCurrToken().equals("method") || tokenizer.getCurrToken().equals("function")){
            compileSubroutine();
        }

        process("}");

        indentLevel--;
        writer.write(getIndentation() + "</class>\n");
    }

    /**
     * Compiles a static variable declaration, or a field declaration.
     */
    public void compileClassVarDec() throws IOException {

        writer.write(getIndentation() + "<classVarDec>\n");
        indentLevel++;

        //Process static || field
        if(tokenizer.getCurrToken().equals("static") || tokenizer.getCurrToken().equals("field")){
            process(tokenizer.getCurrToken());
        }

        //Process type
        if(tokenizer.getCurrToken().equals("int") || tokenizer.getCurrToken().equals("char") || tokenizer.getCurrToken().equals("boolean")){
            process(tokenizer.getCurrToken());
        }
        //Assumes it is a className
        else{
            process(tokenizer.getCurrToken());
        }

        //Process varName
        process(tokenizer.getCurrToken());

        //Process all coming varNames (int x, y, z...)
        while(tokenizer.getCurrToken().equals(",")){
            process(",");
            process(tokenizer.getCurrToken());
        }

        process(";");
        indentLevel--;
        writer.write(getIndentation() + "</classVarDec>\n");
    }

    /**
     * Compiles a complete method, function, or constructor.
     */
    public void compileSubroutine() throws IOException {
        writer.write(getIndentation() + "<subroutineDec>\n");
        indentLevel++;

        //Process constructor || function || method
        if(tokenizer.getCurrToken().equals("constructor") || tokenizer.getCurrToken().equals("function") || tokenizer.getCurrToken().equals("method")){
            process(tokenizer.getCurrToken());
        }

        //Process type: int || char || boolean || className
        if(tokenizer.getCurrToken().equals("int") || tokenizer.getCurrToken().equals("char") || tokenizer.getCurrToken().equals("boolean") || tokenizer.getCurrToken().equals("void")){
            process(tokenizer.getCurrToken());
        } else{ //className
            process(tokenizer.getCurrToken());
        }

        //Process subroutineName
        process(tokenizer.getCurrToken());

        process("(");
        compileParameterList();
        process(")");

        compileSubroutineBody();

        indentLevel--;
        writer.write(getIndentation() + "</subroutineDec>\n");
    }

    /**
     * Compiles a (possibly empty) parameter list.
     * Does not handle the enclosing parentheses tokes ( and ).
     */
    public void compileParameterList() throws IOException {
        writer.write(getIndentation() + "<parameterList>\n");
        indentLevel++;

        //If parameter list is empty
        if(tokenizer.getCurrToken().equals(")")){
            indentLevel--;
            writer.write(getIndentation() + "</parameterList>\n");
            return;
        }

        //Process type
        if(tokenizer.getCurrToken().equals("int") || tokenizer.getCurrToken().equals("char") || tokenizer.getCurrToken().equals("boolean")){
            process(tokenizer.getCurrToken());
        } else { //Assume it's a className
            process(tokenizer.getCurrToken());
        }

        //Assume it's the varName
        process(tokenizer.getCurrToken());
        while(tokenizer.getCurrToken().equals(",")){
            process(",");
            //Process type
            process(tokenizer.getCurrToken());
            //Process varName
            process(tokenizer.getCurrToken());
        }
        indentLevel--;
        writer.write(getIndentation() + "</parameterList>\n");
    }

    /**
     * Compiles a subroutine call.
     * Expects subroutineName \ classname \ varName to already be read.
     * @throws IOException
     */
    public void compileSubroutineCall() throws IOException {
        //subroutineCall is for subroutineName
        if(tokenizer.getCurrToken().equals("(")){
            process("(");
            compileExpressionList();
            process(")");
        }

        //subroutineCall is for className \ varName
        else if(tokenizer.getCurrToken().equals(".")){
            process(".");
            //Process subroutineName
            process(tokenizer.getCurrToken());
            process("(");
            compileExpressionList();
            process(")");
        }
    }

    /**
     * Compiles a subroutine's body.
     */
    public void compileSubroutineBody() throws IOException {

        writer.write(getIndentation() + "<subroutineBody>\n");
        indentLevel++;

        process("{");

        //Compile varDec*
        while(tokenizer.getCurrToken().equals("var")){
            compileVarDec();
        }

        //Compile statements
        compileStatements();

        process("}");
        indentLevel--;
        writer.write(getIndentation() + "</subroutineBody>\n");

    }

    /**
     * Compiles a var declaration.
     */
    public void compileVarDec() throws IOException {
        writer.write(getIndentation() + "<varDec>\n");
        indentLevel++;

        process("var");

        //Process type
        if(tokenizer.getCurrToken().equals("int") || tokenizer.getCurrToken().equals("char") || tokenizer.getCurrToken().equals("boolean")){
            process(tokenizer.getCurrToken());
        } else { //Assume it's a className
            process(tokenizer.getCurrToken());
        }

        //Process varName
        process(tokenizer.getCurrToken());
        while(tokenizer.getCurrToken().equals(",")){
            process(",");
            //Process varName;
            process(tokenizer.getCurrToken());
        }
        process(";");

        indentLevel--;
        writer.write(getIndentation() + "</varDec>\n");
    }

    /**
     * Compiles a sequence of statements,
     * Does not handle the enclosing curly bracket tokes { and }.
     */
    public void compileStatements() throws IOException {

        writer.write(getIndentation() + "<statements>\n");
        indentLevel++;

        while(tokenizer.getCurrToken().equals("let") || tokenizer.getCurrToken().equals("if") || tokenizer.getCurrToken().equals("while") || tokenizer.getCurrToken().equals("do") || tokenizer.getCurrToken().equals("return")){
            switch(tokenizer.getCurrToken()){
                case "let": compileLet(); break;
                case "if": compileIf(); break;
                case "while": compileWhile(); break;
                case "do": compileDo(); break;
                case "return": compileReturn(); break;
            }
        }
        indentLevel--;
        writer.write(getIndentation() + "</statements>\n");


    }

    /**
     * Compiles a let statement.
     */
    public void compileLet() throws IOException {

        writer.write(getIndentation() + "<letStatement>\n");
        indentLevel++;

        process("let");

        //Process varName
        process(tokenizer.getCurrToken());

        //In case of an array
        if(tokenizer.getCurrToken().equals("[")){
            process("[");
            compileExpression();
            process("]");
        }
        process("=");

        compileExpression();

        process(";");

        indentLevel--;
        writer.write(getIndentation() + "</letStatement>\n");
    }

    /**
     * Compiles an if statement, possibly with a trailing else clause.
     */
    public void compileIf() throws IOException {
        writer.write(getIndentation() + "<ifStatement>\n");
        indentLevel++;

        process("if");
        process("(");

        compileExpression();

        process(")");
        process("{");

        compileStatements();

        process("}");

        if(tokenizer.getCurrToken().equals("else")){
            process("else");
            process("{");
            compileStatements();
            process("}");
        }

        indentLevel--;
        writer.write(getIndentation() + "</ifStatement>\n");
    }

    /**
     * Compiles a while statement.
     */
    public void compileWhile() throws IOException {
        writer.write(getIndentation() + "<whileStatement>\n");
        indentLevel++;

        process("while");
        process("(");

        compileExpression();

        process(")");
        process("{");

        compileStatements();

        process("}");

        indentLevel--;
        writer.write(getIndentation() + "</whileStatement>\n");
    }

    /**
     * Compiles a do statement
     */
    public void compileDo() throws IOException {
        writer.write(getIndentation() + "<doStatement>\n");
        indentLevel++;

        process("do");

        //Process subroutineName / className
        process(tokenizer.getCurrToken());
        compileSubroutineCall();

        process(";");

        indentLevel--;
        writer.write(getIndentation() + "</doStatement>\n");
    }

    /**
     * Compiles a return statement
     */
    public void compileReturn() throws IOException {
        writer.write(getIndentation() + "<returnStatement>\n");
        indentLevel++;
        process("return");

        if(!tokenizer.getCurrToken().equals(";")){
            compileExpression();
        }
        process(";");

        indentLevel--;
        writer.write(getIndentation() + "</returnStatement>\n");
    }

    /**
     * Compiles an expression
     */
    public void compileExpression() throws IOException {
        writer.write(getIndentation() + "<expression>\n");
        indentLevel++;

        compileTerm();
        while("+-*/&|<>=".contains(tokenizer.getCurrToken())){
            //Process op
            process(tokenizer.getCurrToken());
            compileTerm();
        }

        indentLevel--;
        writer.write(getIndentation() + "</expression>\n");

    }

    /**
     * Compiles a term. If the current token is an identifier,
     * the routine must resolve it into a variable, an array entry or a subroutine call.
     * A single lookahead token, which may be [, (, or ., suffices to distinguish between
     * the possibilities. Any other token is not part of this term and should not
     * be advanced over
     */
    public void compileTerm() throws IOException {

        writer.write(getIndentation() + "<term>\n");
        indentLevel++;

        if(tokenizer.tokenType() == JackTokenizer.Tokens.INT_CONST){
            process(tokenizer.getCurrToken());
        } else if  (tokenizer.tokenType() == JackTokenizer.Tokens.STRING_CONST){
            process(tokenizer.getCurrToken());
        } else if (tokenizer.tokenType() == JackTokenizer.Tokens.KEYWORD){
            process(tokenizer.getCurrToken());
        } else if (tokenizer.tokenType() == JackTokenizer.Tokens.IDENTIFIER){
            process(tokenizer.getCurrToken());
            //In case of array
            if(tokenizer.getCurrToken().equals("[")){
                process("[");
                compileExpression();
                process("]");
            }

            //Reads methods
            if(tokenizer.getCurrToken().equals("(") || tokenizer.getCurrToken().equals(".")){
                compileSubroutineCall();
            }
        } else if (tokenizer.tokenType() == JackTokenizer.Tokens.SYMBOL){
            if(tokenizer.getCurrToken().equals("(")){
                process("(");
                compileExpression();
                process(")");
            }
            else if (tokenizer.getCurrToken().equals("-")){
                process("-");
                compileTerm();
            }
            else if (tokenizer.getCurrToken().equals("~")){
                process("~");
                compileTerm();
            }
        }

        indentLevel--;
        writer.write(getIndentation() + "</term>\n");
    }

    /**
     * Compiles a (possibly empty) comma-seperated list of expressions.
     * @return the number of expressions in the list
     */
    public int compileExpressionList() throws IOException {

        writer.write(getIndentation() + "<expressionList>\n");
        indentLevel++;

        //If expression list empty:
        if(tokenizer.getCurrToken().equals(")")){
            indentLevel--;
            writer.write(getIndentation() + "</expressionList>\n");
            return 0;
        }

        //At least 1 expression
        int numOfExpressions = 1;
        compileExpression();

        //Continue processing all expressions
        while(tokenizer.getCurrToken().equals(",")){
            process(",");
            compileExpression();
            numOfExpressions++;
        }

        indentLevel--;
        writer.write(getIndentation() + "</expressionList>\n");

        return numOfExpressions;
    }

}








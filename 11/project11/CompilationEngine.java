import java.io.File;
import java.io.IOException;

public class CompilationEngine {

    JackTokenizer tokenizer;
    static int labelNum = 1;
    SymbolTable classSymbolTable, subroutineSymbolTable;
    VMWriter vmwriter;

    // Helper field for knowing when to add "this" argument to subroutineSymbolTable
    // Example: when calling a constructor/function, we are inside a subroutine, but "this" is not required
    static boolean inMethod = false;
    static boolean inIf, inWhile;

    // Helper field for knowing "this" type when inserting it into subroutineSymbolTable
    static String className = "";

    /**
     * Creates a new compilation engine with the given input and output.
     *
     * @param inputFile  xxx.Jack
     * @param outputFile xxx.XML
     * @throws IOException
     */
    public CompilationEngine(File inputFile, File outputFile) throws IOException {
        tokenizer = new JackTokenizer(inputFile);
        vmwriter = new VMWriter(outputFile);
        tokenizer.advance();
        classSymbolTable = new SymbolTable();
        subroutineSymbolTable = new SymbolTable();
        compileClass();
        vmwriter.close();
    }

    /**
     * Compiles a complete class.
     */
    public void compileClass() throws IOException {
        tokenizer.advance(); // Advance class

        // Set the class name
        className = tokenizer.getCurrToken();
        tokenizer.advance(); // Advance className

        tokenizer.advance(); // Advance {

        // Compile class variable declarations (static/field)
        while (tokenizer.getCurrToken().equals("static") || tokenizer.getCurrToken().equals("field")) {
            compileClassVarDec();
        }

        // Compile subroutine declarations (constructor, method, function)
        while (tokenizer.getCurrToken().equals("constructor") || tokenizer.getCurrToken().equals("method") || tokenizer.getCurrToken().equals("function")) {
            compileSubroutine();
        }

        tokenizer.advance(); // Advance }
    }

    /**
     * Compiles a static variable declaration, or a field declaration.
     */
    public void compileClassVarDec() throws IOException {
        String type, name;
        SymbolTable.Kind kind = null;

        // Determine the kind (static or field)
        if (tokenizer.getCurrToken().equals("static")) {
            kind = SymbolTable.Kind.STATIC;
        } else if (tokenizer.getCurrToken().equals("field")) {
            kind = SymbolTable.Kind.FIELD;
        }
        tokenizer.advance(); // Advance static/field

        // Determine the type (int, char, boolean, or className)
        if (tokenizer.getCurrToken().matches("int|char|boolean")) {
            type = tokenizer.getCurrToken();
        } else {
            type = tokenizer.getCurrToken(); // Assume it's a className
        }
        tokenizer.advance(); // Advance type

        // Add the first variable to the symbol table
        name = tokenizer.getCurrToken();
        classSymbolTable.define(name, type, kind);
        tokenizer.advance(); // Advance varName

        // Add additional variables in the same declaration (e.g., int x, y, z;)
        while (tokenizer.getCurrToken().equals(",")) {
            tokenizer.advance(); // Advance ,
            name = tokenizer.getCurrToken();
            classSymbolTable.define(name, type, kind);
            tokenizer.advance(); // Advance varName
        }

        tokenizer.advance(); // Advance ;
    }

    /**
     * Compiles a complete method, function, or constructor.
     */
    public void compileSubroutine() throws IOException {
        boolean inConstructor = false;
        subroutineSymbolTable.reset(); // Reset the subroutine symbol table

        // Determine the type of subroutine (constructor, method, function)
        if (tokenizer.getCurrToken().equals("method")) {
            inMethod = true;
        } else if (tokenizer.getCurrToken().equals("constructor")) {
            inConstructor = true;
        }
        tokenizer.advance(); // Advance constructor/method/function

        tokenizer.advance(); // Advance return type (int/char/boolean/className)

        // Get subroutine name
        String identifier = tokenizer.getCurrToken();
        tokenizer.advance(); // Advance subroutineName

        tokenizer.advance(); // Advance (

        // Compile the parameter list
        compileParameterList();
        tokenizer.advance(); // Advance )

        tokenizer.advance(); // Advance {

        // Compile variable declarations
        while (tokenizer.getCurrToken().equals("var")) {
            compileVarDec();
        }

        // Write function declaration in VM code
        vmwriter.writeFunction(className + "." + identifier, subroutineSymbolTable.varCount(SymbolTable.Kind.VAR));

        // Allocate memory for the constructor
        if (inConstructor) {
            vmwriter.writePush(VMWriter.Segment.CONSTANT, classSymbolTable.varCount(SymbolTable.Kind.FIELD));
            vmwriter.writeCall("Memory.alloc", 1);
            vmwriter.writePop(VMWriter.Segment.POINTER, 0);
        } else if (inMethod) { // Handle "this" argument for methods
            vmwriter.writePush(VMWriter.Segment.ARGUMENT, 0);
            vmwriter.writePop(VMWriter.Segment.POINTER, 0);
            inMethod = false;
        }

        // Compile the subroutine body
        compileSubroutineBody();
    }

    /**
     * Compiles a (possibly empty) parameter list.
     * Does not handle the enclosing parentheses tokens ( and ).
     */
    public void compileParameterList() throws IOException {
        String name, type;

        // Add "this" argument for methods
        if (inMethod) {
            subroutineSymbolTable.define("this", className, SymbolTable.Kind.ARG);
        }

        // If parameter list is empty, return
        if (tokenizer.getCurrToken().equals(")")) {
            return;
        }

        // Parse the first parameter
        type = tokenizer.getCurrToken(); // Get type
        tokenizer.advance(); // Advance type
        name = tokenizer.getCurrToken(); // Get varName
        subroutineSymbolTable.define(name, type, SymbolTable.Kind.ARG);
        tokenizer.advance(); // Advance varName

        // Parse additional parameters (if any)
        while (tokenizer.getCurrToken().equals(",")) {
            tokenizer.advance(); // Advance ,
            type = tokenizer.getCurrToken(); // Get type
            tokenizer.advance(); // Advance type
            name = tokenizer.getCurrToken(); // Get varName
            subroutineSymbolTable.define(name, type, SymbolTable.Kind.ARG);
            tokenizer.advance(); // Advance varName
        }
    }

    /**
     * Compiles a subroutine's body.
     */
    public void compileSubroutineBody() throws IOException {
        compileStatements(); // Compile statements
        tokenizer.advance(); // Advance }
    }

    /**
     * Compiles a var declaration.
     */
    public void compileVarDec() throws IOException {
        String name, type;

        tokenizer.advance(); // Advance var
        type = tokenizer.getCurrToken(); // Get type
        tokenizer.advance(); // Advance type
        name = tokenizer.getCurrToken(); // Get varName

        // Add declared local variables to the table
        subroutineSymbolTable.define(name, type, SymbolTable.Kind.VAR);
        tokenizer.advance(); // Advance varName

        // Handle additional variables (e.g., var x, y, z;)
        while (tokenizer.getCurrToken().equals(",")) {
            tokenizer.advance(); // Advance ,
            name = tokenizer.getCurrToken(); // Get varName
            subroutineSymbolTable.define(name, type, SymbolTable.Kind.VAR);
            tokenizer.advance(); // Advance varName
        }

        tokenizer.advance(); // Advance ;
    }

/**
     * Compiles a sequence of statements,
     * Does not handle the enclosing curly bracket tokes { and }.
     */
    public void compileStatements() throws IOException {


        while (tokenizer.getCurrToken().equals("let") || tokenizer.getCurrToken().equals("if") || tokenizer.getCurrToken().equals("while") || tokenizer.getCurrToken().equals("do") || tokenizer.getCurrToken().equals("return")) {
            switch (tokenizer.getCurrToken()) {
                case "let":
                    compileLet();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "do":
                    compileDo();
                    break;
                case "return":
                    compileReturn();
                    break;
            }
        }

    }

    /**
     * Compiles a let statement.
     */
    public void compileLet() throws IOException {

        //Advance let
        tokenizer.advance();
        boolean isArray = false;

        //Advance varName
        String identifier = tokenizer.getCurrToken();
        tokenizer.advance();

        if (tokenizer.getCurrToken().equals("[")) {
            isArray = true;
            //Advance [
            tokenizer.advance();
            compileExpression();
            identifierPush(identifier);
            vmwriter.writeArithmetic(VMWriter.Command.ADD);
            //Advance ]
            tokenizer.advance();

        }

        //Advance =
        tokenizer.advance();

        compileExpression();
        if (isArray) {

            vmwriter.writePop(VMWriter.Segment.TEMP, 0); // temp 0 = b[j]
            vmwriter.writePop(VMWriter.Segment.POINTER, 1); // THAT = address of a[i]
            vmwriter.writePush(VMWriter.Segment.TEMP, 0); // stack top = b[j]
            vmwriter.writePop(VMWriter.Segment.THAT, 0); // a[i] = b[j]
        } else {
            identifierPop(identifier);
        }
        //Advance ;
        tokenizer.advance();
    }

    /**
     * Compiles an if statement, possibly with a trailing else clause.
     * VM code explanation: first negate the if condition. if it's true (meaning the condition failed
     * and will go to else) jump to the VM code generated by the second compileStatements (else statements).
     * If it's false (the condition is true), continue to the VM code generated by the first compileStatements (if statements).
     */
    public void compileIf() throws IOException {
        inIf = true;
        //Advance if
        tokenizer.advance();

        compileIfExpression();


        int currentLabel = labelNum;
        labelNum += 2;
        //First negate
        vmwriter.writeArithmetic(VMWriter.Command.NOT);
        //Print if-goto label Lx
        vmwriter.writeIf("L" + currentLabel);
        //Advance {
        tokenizer.advance();

        compileStatements(); //if compileStatements
        //print goto Lx+1
        vmwriter.writeGoto("L" + (currentLabel + 1));
        //Advance }
        tokenizer.advance();

        //print label Lx
        vmwriter.writeLabel("L" + currentLabel);

        if (tokenizer.getCurrToken().equals("else")) {
            //Advance else
            tokenizer.advance();
            //Advance {
            tokenizer.advance();
            compileStatements(); //else compileStatements
            //Advance }
            tokenizer.advance();
        }

        //print label Lx+1
        vmwriter.writeLabel("L" + (currentLabel + 1));
        inIf = false;

    }

    /**
     * Compiles a while statement.
     */
    public void compileWhile() throws IOException {
        inWhile = true;
        vmwriter.writeLabel("L" + labelNum);
        //Advance while
        tokenizer.advance();

        int currentLabel = labelNum;
        labelNum += 2;

        compileIfExpression();
        vmwriter.writeArithmetic(VMWriter.Command.NOT);
        vmwriter.writeIf("L" + (currentLabel + 1));

        //Advance {
        tokenizer.advance();


        compileStatements();
        vmwriter.writeGoto("L" + currentLabel);
        vmwriter.writeLabel("L" + (currentLabel + 1));
        //Advance }
        tokenizer.advance();

        inWhile = false;

    }

    /**
     * Compiles a do statement
     */
    public void compileDo() throws IOException {
        //Advance do
        tokenizer.advance();

        compileExpression();
        //Advance ;
        tokenizer.advance();
        vmwriter.writePop(VMWriter.Segment.TEMP, 0);

    }

    /**
     * Compiles a return statement
     */
    public void compileReturn() throws IOException {

        //Advance return
        tokenizer.advance();

        if (!tokenizer.getCurrToken().equals(";")) {
            compileExpression();
            //Advance ;
            tokenizer.advance();
            vmwriter.writeReturn();
        } else {
            //push dummy value in case nothing is returned
            //Advance ;
            tokenizer.advance();

            vmwriter.writePush(VMWriter.Segment.CONSTANT, 0);
            vmwriter.writeReturn();
        }
    }

    public void compileExpression() throws IOException {
        compileExpressionBase(false);
    }

    public void compileIfExpression() throws IOException {
        compileExpressionBase(true);
    }

    /**
     * Compiles an expression
     */
    public void compileExpressionBase(boolean isIfExp) throws IOException {
        compileTerm();

        while ((isIfExp ? "+-*/&|<>=" : "+-*/&|<>").contains(tokenizer.getCurrToken())) {
            String op = tokenizer.getCurrToken();

            //Advance op
            tokenizer.advance();
            compileTerm();

            VMWriter.Command cmd = VMWriter.Command.matchCommand(op);

            if (cmd != null) {
                vmwriter.writeArithmetic(cmd);
            } else {
                if (op.equals("*")) {
                    vmwriter.writeCall("Math.multiply", 2);
                } else if (op.equals("/")) {
                    vmwriter.writeCall("Math.divide", 2);
                }
            }

        }

    }

    /**
     * Compiles a term. If the current token is an identifier,
     * the routine must resolve it into a variable, an array entry or a subroutine call.
     * A single lookahead token, which may be [, (, or ., suffices to distinguish between
     * the possibilities. Any other token is not part of this term and should not
     * be advanced over
     */
    public void compileTerm() throws IOException {

        if (tokenizer.tokenType() == JackTokenizer.Tokens.INT_CONST) {
            //push constant (int) currToken
            vmwriter.writePush(VMWriter.Segment.CONSTANT, Integer.parseInt(tokenizer.getCurrToken()));
            //Advance int_const
            tokenizer.advance();

        } else if (tokenizer.tokenType() == JackTokenizer.Tokens.STRING_CONST) {

            String stringConst = tokenizer.getCurrToken();
            stringConst = stringConst.substring(1, stringConst.length() - 1);
            vmwriter.writePush(VMWriter.Segment.CONSTANT, stringConst.length());
            vmwriter.writeCall("String.new", 1);
            for (int i = 0; i < stringConst.length(); i++) {
                vmwriter.writePush(VMWriter.Segment.CONSTANT, stringConst.charAt(i));
                vmwriter.writeCall("String.appendChar", 2);
            }

            identifierPop(stringConst);

            //Advance String_Const
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.Tokens.KEYWORD) {
            switch (tokenizer.getCurrToken()) {
                case "true": {
                    vmwriter.writePush(VMWriter.Segment.CONSTANT, 1);
                    vmwriter.writeArithmetic(VMWriter.Command.NEG);
                    break;
                }
                case "false", "null": {
                    vmwriter.writePush(VMWriter.Segment.CONSTANT, 0);
                    break;
                }
                case "this": {
                    vmwriter.writePush(VMWriter.Segment.POINTER, 0);
                    break;
                }
            }

            //Advance keyword
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.Tokens.IDENTIFIER) {

                //will be used in for object and method compilation
                String identifier = tokenizer.getCurrToken();
                String functionName ="";

                //Advance identifier
                tokenizer.advance();
                if(tokenizer.tokenType() == JackTokenizer.Tokens.SYMBOL) {
                    if(tokenizer.getCurrToken().equals("[")){
                        //Advance [
                        tokenizer.advance();
                        compileExpression();
                        //Advance ]
                        tokenizer.advance();
                        identifierPush(identifier);

                        vmwriter.writeArithmetic(VMWriter.Command.ADD);
                        vmwriter.writePop(VMWriter.Segment.POINTER, 1);
                        vmwriter.writePush(VMWriter.Segment.THAT, 0);

                    }
                    else if(tokenizer.getCurrToken().equals("(")){
                        //Advance (
                        tokenizer.advance();
                        if(!subroutineSymbolTable.symbolTable.containsKey(identifier) && !classSymbolTable.symbolTable.containsKey(identifier)) {
                            vmwriter.writePush(VMWriter.Segment.POINTER,0);
                            vmwriter.writeCall(className+"."+identifier, compileExpressionList()+1);
                        }else{
                            vmwriter.writeCall(className+"."+identifier, compileExpressionList());
                        }
                        //Advance )
                        tokenizer.advance();

                    }

                    //subroutineCall is for className \ varName
                    else if(tokenizer.getCurrToken().equals(".")){
                        //Advance .
                        tokenizer.advance();
                        //Advance subroutineName
                        functionName = tokenizer.getCurrToken();
                        tokenizer.advance();
                        //Advance (
                        tokenizer.advance();

                        String type="";
                        if(subroutineSymbolTable.symbolTable.containsKey(identifier)){
                            type = subroutineSymbolTable.typeOf(identifier);
                            vmwriter.writePush(subroutineSymbolTable.kindOf(identifier).matchSegment(), subroutineSymbolTable.indexOf(identifier));
                            vmwriter.writeCall(type+"."+functionName, compileExpressionList()+1);

                        }
                        else if(classSymbolTable.symbolTable.containsKey(identifier)){
                            type = classSymbolTable.typeOf(identifier);
                            vmwriter.writePush(classSymbolTable.kindOf(identifier).matchSegment(), classSymbolTable.indexOf(identifier));
                            vmwriter.writeCall(type+"."+functionName, compileExpressionList()+1);

                        }

                        if(type.equals("")){

                            type=identifier;
                            vmwriter.writeCall(type+"."+functionName, compileExpressionList());
                        }


                        //Advance )
                        tokenizer.advance();

                    }
                    else{
                        //Push segment index
                        identifierPush(identifier);

                    }
                } else{
                    //Push segment index
                    identifierPush(identifier);
                }


            } else if (tokenizer.tokenType() == JackTokenizer.Tokens.SYMBOL) {
                if (tokenizer.getCurrToken().equals("(")) {
                    //Advance (
                    tokenizer.advance();
                    if (inIf || inWhile) {
                        compileIfExpression();
                    } else {
                        compileExpression();
                    }
                    //Advance )
                    tokenizer.advance();
                } else if (tokenizer.getCurrToken().equals("-")) {

                    //Advance -
                    tokenizer.advance();
                    compileTerm();
                    vmwriter.writeArithmetic(VMWriter.Command.NEG);
                } else if (tokenizer.getCurrToken().equals("~")) {
                    //Advance ~
                    tokenizer.advance();
                    compileTerm();
                    vmwriter.writeArithmetic(VMWriter.Command.NOT);
                }
            }

        }

    /**
     * Compiles a (possibly empty) comma-seperated list of expressions.
     * @return the number of expressions in the list
     */
    public int compileExpressionList () throws IOException {

        //If expression list empty:
        if (tokenizer.getCurrToken().equals(")")) {
            return 0;
        }

        //At least 1 expression
        int numOfExpressions = 1;
        compileExpression();

        //Continue processing all expressions
        while (tokenizer.getCurrToken().equals(",")) {
            //Advance ,
            tokenizer.advance();
            compileExpression();
            numOfExpressions++;
        }

        return numOfExpressions;
    }

    private void identifierPush(String identifier) throws IOException {
        if (subroutineSymbolTable.symbolTable.containsKey(identifier)) {
            //push segment index
            vmwriter.writePush(subroutineSymbolTable.kindOf(identifier).matchSegment(), subroutineSymbolTable.indexOf(identifier));
        } else if (classSymbolTable.symbolTable.containsKey(identifier)) {
            //push segment index
            vmwriter.writePush(classSymbolTable.kindOf(identifier).matchSegment(), classSymbolTable.indexOf(identifier));
        }
    }

    private void identifierPop(String identifier) throws IOException {
        if (subroutineSymbolTable.symbolTable.containsKey(identifier)) {
            //push segment index
            vmwriter.writePop(subroutineSymbolTable.kindOf(identifier).matchSegment(), subroutineSymbolTable.indexOf(identifier));
        } else if (classSymbolTable.symbolTable.containsKey(identifier)) {
            //push segment index
            vmwriter.writePop(classSymbolTable.kindOf(identifier).matchSegment(), classSymbolTable.indexOf(identifier));
        }
    }
}






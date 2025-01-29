import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides routines that skip comments and white space, get the next token, and advance
 * the input exactly beyond it. Other routines return the type of the current token, and its value.
 */
public class JackTokenizer {

    protected String currToken;
    private BufferedReader reader;
    private File file;
    public List<String> fileLines = new ArrayList<String>();
    public List<String> tokens;
    private int tokenPointer = 0;


    /**
     * Constructor, opens the input .jack file and tokenizes it.
     * Creates fileLines list - a list containing all lines whom are not comments or spaces.
     * @param File
     * @throws FileNotFoundException
     */
    public JackTokenizer(File File) throws IOException {
        this.reader = new BufferedReader(new FileReader(File));
        this.file = File;


        String line = reader.readLine();
        while (line != null) {
            line = line.trim();
            if (!line.startsWith("//") && !line.startsWith("*") && !line.isBlank() && !line.startsWith("/**") && !line.startsWith("*/") && !line.endsWith("*/")) {
                fileLines.add(line);
            }
            line = reader.readLine();
        }


        tokens = composeTokens();
    }

    /**
     * Creates tokens list.
     * Iterates over fileLines list, for each line extracts the tokens in it, adds them to tokens.
     * @return A list of all tokens in the file.
     */
    private List<String> composeTokens() {
        List<String> tokens = new ArrayList<>();
        int i = 0;

        while (i < fileLines.size()) {
            String line = fileLines.get(i);
            int commentIndex = line.indexOf("//");

            // If there's a comment, process only the part before it
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }

            // Process the line character by character
            StringBuilder token = new StringBuilder();
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);

                if (Character.isWhitespace(c)) {
                    // Whitespace ends the current token
                    addTokenIfNotEmpty(tokens, token);
                } else if (isSymbol(c)) {
                    // Add the current token, then the symbol as its own token
                    addTokenIfNotEmpty(tokens, token);
                    tokens.add(String.valueOf(c));
                } else if (c == '"') {
                    // Handle string literals
                    token.append(c);
                    j++;
                    while (j < line.length() && line.charAt(j) != '"') {
                        token.append(line.charAt(j));
                        j++;
                    }
                    token.append('"');
                    tokens.add(token.toString());
                    token.setLength(0);
                } else {
                    // Continue building the token
                    token.append(c);
                }
            }
            // Add any remaining token
            addTokenIfNotEmpty(tokens, token);

            i++;
        }

        return tokens;
    }

    /**
     * Helper to add a token to the list if it's not empty
     * @param tokens
     * @param token
     */
    private void addTokenIfNotEmpty(List<String> tokens, StringBuilder token) {
        if (token.length() > 0) {
            tokens.add(token.toString());
            token.setLength(0);
        }
    }

    /**
     * Helper to check if a char is a symbol
     * @param c
     * @return
     */
    private boolean isSymbol(char c) {
        return "{}()[].,;+-*/&|<>=~".indexOf(c) != -1;
    }

    /**
     * Constant values for token types
     */
    enum Tokens{
        KEYWORD,
        SYMBOL,
        IDENTIFIER,
        INT_CONST,
        STRING_CONST
    }

    /**
     * Constant values for KEYWORD types
     */
    enum KeyWords{
        CLASS,
        METHOD,
        FUNCTION,
        CONSTRUCTOR,
        INT,
        BOOLEAN,
        CHAR,
        VOID,
        VAR,
        STATIC,
        FIELD,
        LET,
        DO,
        IF,
        ELSE,
        WHILE,
        RETURN,
        TRUE,
        FALSE,
        NULL,
        THIS
    }

    /**
     * Getter for currToken.
     * @return currToken
     */
    public String getCurrToken(){
        return this.currToken;
    }

    /**
     * Checks if token pointer reached end of token list.
     * @return True if there are more tokens, False if not.
     */
    public boolean hasMoreTokens() {
        return tokenPointer < tokens.size();
    }

    /**
     * Gets the next token from the tokens list, and makes it the current token.
     * Advances token pointer.
     * Calls only if hasMoreTokens is true.
     */
    public void advance(){
        if(hasMoreTokens()){
            currToken = tokens.get(tokenPointer);
            tokenPointer++;
        }
    }

    /**
     * Returns the type of the current token as a constant
     * @return KEYWORD | SYMBOL | STRING_CONST | INT_CONST | IDENTIFIER
     */
    public Tokens tokenType() {
        return switch(this.currToken) {
            case "class", "constructor", "function", "method", "field", "static", "var", "int", "char", "boolean",
                 "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return" -> Tokens.KEYWORD;
            case "{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~" -> Tokens.SYMBOL;
            default -> {
                if (currToken.startsWith("\"") && currToken.endsWith("\"")) {
                    // It's a string literal (starts and ends with a quote)
                    // Check if it contains any additional quotes or newline
                    String innerString = currToken.substring(1, currToken.length() - 1);
                    if (innerString.contains("\"") || innerString.contains("\n")) {
                        throw new IllegalArgumentException("Invalid string literal: contains an extra quote or newline.");
                    }
                    yield Tokens.STRING_CONST;
                } else {
                    // Check if it's an integer and in the range 0 to 32767
                    try {
                        int value = Integer.parseInt(currToken);
                        if (value >= 0 && value <= 32767) {
                            yield Tokens.INT_CONST;
                        } else {
                            // If it's not within the valid integer range, it must be an identifier
                            yield Tokens.IDENTIFIER;
                        }
                    } catch (NumberFormatException e) {
                        // Not an integer, so it must be an identifier (a word that's not a keyword)
                        if (isValidIdentifier(currToken)) {
                            yield Tokens.IDENTIFIER;
                        } else {
                            throw new IllegalArgumentException("Invalid identifier: " + currToken);
                        }
                    }
                }
            }
        };
    }

    /**
     * Helper function for tokenType. Checks if the provided string is a valid identifier.
     * A valid identifier starts with a letter or underscore and contains only letters, digits, and underscores.
     */
    private static boolean isValidIdentifier(String str) {
        // Regular expression: starts with a letter or underscore, followed by letters, digits, or underscores
        return str.matches("[A-Za-z_][A-Za-z0-9_]*");
    }


    /**
     * Returns the type of the current keyword as a constant
     */
    public KeyWords keyWord(){
        if(tokenType() == Tokens.KEYWORD){
            return switch(currToken){
                case "class" -> KeyWords.CLASS;
                case "method" -> KeyWords.METHOD;
                case "function" -> KeyWords.FUNCTION;
                case "constructor" -> KeyWords.CONSTRUCTOR;
                case "int" -> KeyWords.INT;
                case "boolean" -> KeyWords.BOOLEAN;
                case "char" -> KeyWords.CHAR;
                case "void" -> KeyWords.VOID;
                case "var" -> KeyWords.VAR;
                case "static" -> KeyWords.STATIC;
                case "field" -> KeyWords.FIELD;
                case "let" -> KeyWords.LET;
                case "do" -> KeyWords.DO;
                case "if" -> KeyWords.IF;
                case "else" -> KeyWords.ELSE;
                case "while" -> KeyWords.WHILE;
                case "return" -> KeyWords.RETURN;
                case "true" -> KeyWords.TRUE;
                case "false" -> KeyWords.FALSE;
                case "null" -> KeyWords.NULL;
                case "this" -> KeyWords.THIS;
                default -> null;
            };
        }
        return null;
    }

    /**
     *If the current token type is a symbol, returns it as a char
     */
    public char symbol() {
        if(tokenType() == Tokens.SYMBOL){
            return (currToken.charAt(0));
        } else{
            throw new IllegalStateException("Current token is not a SYMBOL");
        }
    }

    /**
     *If the current token type is an identifier, returns it as a String
     */
    public String identifier(){
        if(tokenType() == Tokens.IDENTIFIER){
            return currToken;
        } else{
            throw new IllegalStateException("Current token is not an IDENTIFIER");
        }
    }

    /**
     *If the current token type is an integer constant, returns it as an Int
     */
    public int intVal(){
        if(tokenType() == Tokens.INT_CONST) {
            return Integer.parseInt(currToken);
        } else{
            throw new IllegalStateException("Current token is not an INT_CONST");
        }
    }

    /**
     *If the current token type is a string constant, returns it as a String
     */
    public String stringVal(){
        if(tokenType() == Tokens.STRING_CONST){
            return currToken.substring(1, currToken.length()-2);
        } else{
            throw new IllegalStateException("Current token is not an STRING_CONST");
        }
    }

}

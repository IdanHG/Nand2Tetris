import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: JackAnalyzer <fileName.jack | folderName>");
            return;
        }

        File input = new File(args[0]);

        if (input.isFile() && input.getName().endsWith(".jack")) {
            // Process a single .jack file
            processFile(input);
        } else if (input.isDirectory()) {
            // Process all .jack files in the folder
            File[] files = input.listFiles((dir, name) -> name.endsWith(".jack"));
            if (files != null) {
                for (File file : files) {
                    processFile(file);
                }
            } else {
                System.err.println("No .jack files found in the folder.");
            }
        } else {
            System.err.println("Invalid input: Must be a .jack file or a folder containing .jack files.");
        }

    }

    private static void processFile(File jackFile) throws IOException {
        String inputFileName = jackFile.getName();
        String outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf(".")) + ".vm";
        File outputFile = new File(jackFile.getParent(), outputFileName);

        // Pass the input file and output file to the CompilationEngine
        CompilationEngine engine = new CompilationEngine(jackFile, outputFile);

        SymbolTable classSymbolTable = engine.classSymbolTable;
        SymbolTable methodSymbolTable = engine.subroutineSymbolTable;


    }

}

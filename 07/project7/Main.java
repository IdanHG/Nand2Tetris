import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <path-to-vm-file-or-directory>");
            return;
        }

        File input = new File(args[0]);
        try {
            if (input.isFile() && args[0].endsWith(".vm")) {
                // Process a single .vm file
                VMTr.VMTr(input.getAbsolutePath());
            } else if (input.isDirectory()) {
                // Process all .vm files in the directory
                File[] vmFiles = input.listFiles((dir, name) -> name.endsWith(".vm"));
                if (vmFiles != null && vmFiles.length > 0) {
                    // Generate a single .asm file for the directory
                    String dirAsmFile = input.getAbsolutePath() + File.separator + input.getName() + ".asm";

                    // Collect output from all .vm files
                    List<String> outputFiles = new ArrayList<>();
                    for (File vmFile : vmFiles) {
                        VMTr.VMTr(vmFile.getAbsolutePath());
                        outputFiles.add(vmFile.getAbsolutePath().replace(".vm", ".asm"));
                    }

                    // Merge all generated .asm files into a single file
                    mergeFiles(outputFiles, dirAsmFile);
                } else {
                    System.err.println("No .vm files found in the specified directory.");
                }
            } else {
                System.err.println("Invalid input. Please provide a .vm file or a directory containing .vm files.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void mergeFiles(List<String> sourceFiles, String destinationFile) throws IOException {
        try (FileWriter writer = new FileWriter(destinationFile)) {
            for (String sourceFile : sourceFiles) {
                File file = new File(sourceFile);
                if (file.exists()) {
                    // Add a comment to indicate the source file being merged
                    writer.write("// Contents of: " + sourceFile + "\n");
                    writer.write(java.nio.file.Files.readString(file.toPath()));
                    writer.write("\n");
                    file.delete(); // Remove the intermediate .asm file after merging
                }
            }
        }
    }
}

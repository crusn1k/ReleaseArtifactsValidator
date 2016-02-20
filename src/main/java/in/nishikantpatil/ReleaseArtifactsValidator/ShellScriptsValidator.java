package in.nishikantpatil.ReleaseArtifactsValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Implementation for shell scripts validator
 */
public class ShellScriptsValidator implements ReleaseArtifactsValidator {
    private Pattern jarPathVarPattern = Pattern.compile("^jar_path");
    private Pattern jarPathPattern = Pattern.compile("~/\\$env/jars/?");
    @Override
    public boolean isInValidLine(String line) {
        if(jarPathVarPattern.matcher(line).find() && !jarPathPattern.matcher(line).find()) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> isValid(File file) throws InvalidFileException, FileNotFoundException {
        if (null == file || !file.getName().toLowerCase().endsWith(".sh")) {
            throw new InvalidFileException(file + " is not a valid shell script.");
        }

        List<String> invalidLines = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            String line = scanner.nextLine();
            if (isInValidLine(line)) {
                invalidLines.add(line);
            }
        }

        return invalidLines;
    }
}

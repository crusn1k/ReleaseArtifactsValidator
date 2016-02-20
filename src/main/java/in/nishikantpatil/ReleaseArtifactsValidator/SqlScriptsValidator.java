package in.nishikantpatil.ReleaseArtifactsValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation for sql scripts validator
 */
public class SqlScriptsValidator extends ReleaseArtifactsValidator {
    Pattern commitPattern = Pattern.compile("(?)commit");

    @Override
    public boolean isInValidLine(String line) {
        return commitPattern.matcher(line).find();
    }

    @Override
    public List<String> isValid(File file) throws InvalidFileException, FileNotFoundException {
        return isValid(file, ".sql");
    }
}

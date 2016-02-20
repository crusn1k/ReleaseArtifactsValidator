package in.nishikantpatil.ReleaseArtifactsValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation for shell scripts validator
 */
public class ShellScriptsValidator extends ReleaseArtifactsValidator {
    private Pattern jarPathVarPattern = Pattern.compile("^jar_path", Pattern.CASE_INSENSITIVE);
    private Pattern jarPathPattern = Pattern.compile("~/\\$env/jars/?", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isInValidLine(String line) {
        return jarPathVarPattern.matcher(line).find() && !jarPathPattern.matcher(line).find();
    }

    @Override
    public List<String> isValid(File file) throws InvalidFileException, FileNotFoundException {
        return isValid(file, ".sh");
    }
}

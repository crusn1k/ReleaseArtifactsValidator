package in.nishikantpatil.ReleaseArtifactsValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Contract for validator.
 */
public interface ReleaseArtifactsValidator {

    boolean isInValidLine(String line);

    List<String> isValid(File file) throws InvalidFileException, FileNotFoundException;
}

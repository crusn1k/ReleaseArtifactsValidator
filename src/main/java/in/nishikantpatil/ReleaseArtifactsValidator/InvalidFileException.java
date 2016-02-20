package in.nishikantpatil.ReleaseArtifactsValidator;

/**
 * Custom exception for invalid file format.
 */
public class InvalidFileException extends Exception {
    public InvalidFileException(String errText) {
        super(errText);
    }
}

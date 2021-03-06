package in.nishikantpatil.ReleaseArtifactsValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Contract for validator.
 */
public abstract class ReleaseArtifactsValidator {

    protected ThreadLocal<List<String>> lines = new ThreadLocal<>();

    public abstract boolean isInValidLine(String line);

    protected void init(File file) throws FileNotFoundException {
        lines.set(new ArrayList<>());
        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (!"".equals(line.trim())) {
                    lines.get().add(line);
                }
            }
        }

    }

    public List<String> isValid(File file, String extension) throws InvalidFileException, FileNotFoundException {
        if (null == file || !file.getName().toLowerCase().endsWith(extension)) {
            throw new InvalidFileException(file + " is not a valid script.");
        }
        init(file);
        List<String> invalidLines = new ArrayList<>();
        lines.get().parallelStream().forEach((line) -> {
            if (isInValidLine(line)) {
                invalidLines.add("File " + file.getName() + " has invalid keywords: " + line);
            }
        });



        return invalidLines;
    }

    public abstract List<String> isValid(File file) throws InvalidFileException, FileNotFoundException;
}

package in.nishikantpatil.ReleaseArtifactsValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Launcher for the validators.
 */
public class Main {

    private static ConcurrentMap<String, List<String>> fileResults = new ConcurrentHashMap<>();

    private static void validate(ReleaseArtifactsValidator validator, String dirPath) {
        File scriptsDir = new File(dirPath);
        if (!scriptsDir.isDirectory() || null == scriptsDir.listFiles()) {
            throw new IllegalArgumentException(dirPath + " is not a directory.");
        }
        Arrays.stream(scriptsDir.listFiles()).parallel().forEach((file -> {
            if (!file.isDirectory()) {
                try {
                    fileResults.put(file.getAbsolutePath(), validator.isValid(file));
                } catch (InvalidFileException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                validate(validator, file.getAbsolutePath());
            }
        }));
    }

    public static void main(String... args) {
        String dirPath = args[0];
        String fileType = args[1];

        if ("shell".equals(fileType)) {
            validate(new ShellScriptsValidator(), dirPath);
        } else {
            validate(new SqlScriptsValidator(), dirPath);
        }
        for (Map.Entry<String, List<String>> entry : fileResults.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.println(entry.getKey() + " has the following errors.");
                for (String invalidLine : entry.getValue()) {
                    System.out.println("\t\t" + invalidLine);
                }
            }
        }
    }
}

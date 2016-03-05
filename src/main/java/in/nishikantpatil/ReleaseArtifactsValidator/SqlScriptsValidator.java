package in.nishikantpatil.ReleaseArtifactsValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Implementation for sql scripts validator
 */
public class SqlScriptsValidator extends ReleaseArtifactsValidator {
    Pattern invalidSqlKeywordsPattern = Pattern.compile("(commit|rollback)", Pattern.CASE_INSENSITIVE);
    Pattern createObjectPattern = Pattern.compile("^create( +or +replace)? +(procedure|function|type|package|package_body) +[a-zA-Z_]+$", Pattern.CASE_INSENSITIVE);
    Pattern endPattern = Pattern.compile("^end( +[a-zA-Z_]+)?;?$", Pattern.CASE_INSENSITIVE);
    Pattern declarePattern = Pattern.compile("^declare( +)?$", Pattern.CASE_INSENSITIVE);
    Pattern ddlPattern = Pattern.compile("alter|create|drop|truncate|rename", Pattern.CASE_INSENSITIVE);
    Pattern dmlPattern = Pattern.compile("insert|update|merge|delete", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isInValidLine(String line) {
        return invalidSqlKeywordsPattern.matcher(line).find();
    }

    @Override
    public List<String> isValid(File file) throws InvalidFileException, FileNotFoundException {
        List<String> invalidLines = isValid(file, ".sql");
        invalidLines.addAll(validateDdlDml(file));
        invalidLines.addAll(validatePlSqlBlocks(file));
        invalidLines.addAll(validateDbObjectSyntax(file));
        return invalidLines;
    }

    private List<String> validatePlSqlBlocks(File file) throws FileNotFoundException {
        List<String> invalidLines = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            boolean declareFound = false;
            boolean endFound = false;
            boolean semiColonFound = false;
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if ("".equals(line.trim())) {
                    continue;
                }
                if (!declareFound) {
                    declareFound = declarePattern.matcher(line).find();
                } else {
                    if (!endFound) {
                        endFound = endPattern.matcher(line).find();
                        if (endFound) {
                            if (line.trim().endsWith(";")) {
                                semiColonFound = true;
                            }
                        }
                    } else {
                        if (semiColonFound && "/".equals(line.trim())) {
                            invalidLines.add("File " + file.getName() + " contains ; and / after PL/SQL block.");
                        } else {
                            semiColonFound = false;
                        }
                        declareFound = false;
                        endFound = false;
                    }
                }
            }
        }
        return invalidLines;
    }

    private List<String> validateDdlDml(File file) throws FileNotFoundException {
        List<String> invalidLines = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            boolean ddlFound = false;
            boolean dmlFound = false;
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if ("".equals(line.trim())) {
                    continue;
                }
                if (!dmlFound) {
                    dmlFound = dmlPattern.matcher(line).find();
                }
                if (!ddlFound) {
                    ddlFound = ddlPattern.matcher(line).find();
                }
                if (ddlFound && dmlFound) {
                    invalidLines.add("File " + file.getName() + " has DDL and DML in the same file. Please verify.");
                    break;
                }

            }
        }
        return invalidLines;
    }

    private List<String> validateDbObjectSyntax(File file) throws FileNotFoundException {
        List<String> invalidLines = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            String objectCreated = "";
            boolean createFound = false;
            boolean endFound = false;
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if ("".equals(line.trim())) {
                    continue;
                }
                if (!createFound && !endFound) {
                    createFound = createObjectPattern.matcher(line).find();
                    objectCreated = line;
                } else {
                    if (!endFound) {
                        endFound = endPattern.matcher(line).find();
                        createFound = !endFound;
                    } else {
                        if (!"/".equals(line.trim())) {
                            invalidLines.add(objectCreated + " does not end with /");
                        }
                        endFound = false;
                    }
                }
            }
            if (createFound) {
                invalidLines.add(objectCreated + " does not end");
            } else if (endFound) {
                invalidLines.add(objectCreated + " does not end with / ");
            }
        }

        return invalidLines;
    }
}

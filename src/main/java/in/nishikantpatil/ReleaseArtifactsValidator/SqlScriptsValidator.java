package in.nishikantpatil.ReleaseArtifactsValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
        invalidLines.addAll(validateDbObjectSyntax());
        return invalidLines;
    }

    private List<String> validatePlSqlBlocks(File file) throws FileNotFoundException {
        List<String> invalidLines = new ArrayList<>();

        boolean declareFound = false;
        boolean endFound = false;
        boolean semiColonFound = false;
        for (String line : lines.get()) {
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

        return invalidLines;
    }

    private List<String> validateDdlDml(File file) throws FileNotFoundException {
        List<String> invalidLines = new ArrayList<>();
        boolean ddlFound = false;
        boolean dmlFound = false;
        for (String line : lines.get()) {
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

        return invalidLines;
    }

    private List<String> validateDbObjectSyntax() throws FileNotFoundException {
        List<String> invalidLines = new ArrayList<>();
        String objectCreated = "";
        boolean createFound = false;
        boolean endFound = false;
        for (String line : lines.get()) {
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
        return invalidLines;
    }
}

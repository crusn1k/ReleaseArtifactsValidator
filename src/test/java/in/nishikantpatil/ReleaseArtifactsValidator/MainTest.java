package in.nishikantpatil.ReleaseArtifactsValidator;

import org.junit.Test;

/**
 * Test for the validator
 */
public class MainTest {

    private static final String sqlPath = "/home/nishikant/GitHub/ReleaseArtifactsValidator/src/test/resources/";

    @Test
    public void testMain() {
        Main.main(sqlPath, "sql");
    }
}

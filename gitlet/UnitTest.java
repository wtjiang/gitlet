package gitlet;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.junit.Test;
import org.junit.Assert;

import ucb.junit.textui;

/**
 * The suite of all JUnit tests for the gitlet package.
 * @author Winston
 */
public class UnitTest {
    /**
     * Run the JUnit tests in the loa package. Add xxxTest.class entries to the
     * arguments of runClasses to runCommand other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    @Test
    public void testPut() {
        HashMap<String, String> firstBlobs = new HashMap<>();
        firstBlobs.put("a.file", "a");
        firstBlobs.put("b.file", "b");
    }

    @Test
    public void testDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss yyyy Z");
        String format = dateFormat.format(0);
        Assert.assertEquals(format, "Wed Dec 31 16:00:00 1969 -0800");
    }

}


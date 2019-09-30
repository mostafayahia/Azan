package free.elmasry.azan.utilities;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

public class MyStringUtilsTest {

    @Test
    public void testSplitString() {
        String input = "04:37ZZZ06:05ZZZ11:45ZZZ15:00ZZZ17:26ZZZ18:44ZZZ";
        String[] actualOutput = MyStringUtils.splitString(input, "ZZZ");
        String[] expectedOutput = {"04:37", "06:05", "11:45", "15:00", "17:26", "18:44"};
        for (int i = 0; i < expectedOutput.length; i++)
            Assert.assertEquals(expectedOutput[i], actualOutput[i]);
    }
}

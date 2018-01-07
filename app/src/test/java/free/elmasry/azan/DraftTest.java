package free.elmasry.azan;

import org.junit.Test;

import java.util.Locale;

/**
 * Created by yahia on 12/31/17.
 */

public class DraftTest {

    @Test
    public void test() {

        // there is no problem, Integer.parseInt can convert the number if the given number in
        // arabic format
        long fakeHour = Integer.parseInt("۱۲");
        long fakeMinute = Integer.parseInt("۲۳");
        System.out.println(fakeHour + ":" + fakeMinute);
    }
}

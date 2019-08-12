package free.elmasry.azan.utilities;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by yahia on 12/29/17.
 */

public class AzanAppTimeUtilsTest {

    @Test
    public void testConvertDateStringToMillis() {

        String dateString = "25 dec 2016";
        long dateInMillis = AzanAppTimeUtils.convertDateToMillis(dateString);

        long localDateInMillis = dateInMillis + TimeZone.getDefault().getOffset(System.currentTimeMillis());
        long hour = localDateInMillis / (1000 * 60 * 60) % 24;
        long minute = localDateInMillis / (1000 * 60) % 60;
        long second = localDateInMillis / (1000) % 60;

        String hourAndMinuteAndSecond = String.format(new Locale("en"), "%02d:%02d:%02d", hour, minute, second);

        Assert.assertEquals("time returned doesn't represent the midnight in your region", "00:00:00", hourAndMinuteAndSecond);

        //System.out.println("before thread sleeping");
        try {

            Thread.sleep(5);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("after thread sleeping");

        Assert.assertEquals("We didn't get the same dateInMillis for the same date string", dateInMillis, AzanAppTimeUtils.convertDateToMillis(dateString));

    }

    @Test
    public void testConvertMonthFromStringToInt() {

        String[] allMonthsStrings =
                {"january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"};

        for (int i = 0; i < 12; i++) {

            String monthString = allMonthsStrings[i];

            // NOTE January representing 1 and December representing 12

            Assert.assertEquals("Can't convert " + monthString + " to " + (i+1), i+1, AzanAppTimeUtils.convertMonthFromStringToInt(monthString));

            String monthStringUpperCase = monthString.toUpperCase();
            Assert.assertEquals("Can't convert " + monthStringUpperCase + " to " + (i+1), i+1, AzanAppTimeUtils.convertMonthFromStringToInt(monthStringUpperCase));

            String monthString3Letters = monthString.substring(0, 3);
            Assert.assertEquals("Can't convert " + monthString3Letters + " to " + (i+1), i+1, AzanAppTimeUtils.convertMonthFromStringToInt(monthString3Letters));

            String monthString3LettersUpperCase = monthString.substring(0, 3).toUpperCase();
            Assert.assertEquals("Can't convert " + monthString3LettersUpperCase + " to " + (i+1), i+1, AzanAppTimeUtils.convertMonthFromStringToInt(monthString3LettersUpperCase));
        }
    }

    @Test
    public void testConvertMillisToDateTimeString() {
        long timeInMillis = 1565525341000L;
        String dateTimeString = AzanAppTimeUtils.convertMillisToDateTimeString(timeInMillis);
        // Note: this time represents GMT+02:00
        Assert.assertEquals("wrong value for converting", "11 Aug 2019 14:09:01", dateTimeString);
    }

    @Test
    public void testGetDateStringFromDateTimeString() {
        String dateTimeString = AzanAppTimeUtils.getDateStringFromDateTimeString("11 Aug 2019 14:09:01");
        Assert.assertEquals("wrong value for extracting", "11 Aug 2019", dateTimeString);
    }

}

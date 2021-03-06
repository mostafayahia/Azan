package free.elmasry.azan.utilities;

import org.junit.Assert;

import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

import static free.elmasry.azan.utilities.AzanAppTimeUtils.DAY_IN_MILLIS;
import static free.elmasry.azan.utilities.AzanAppTimeUtils.HourMinute;
import static free.elmasry.azan.utilities.AzanAppTimeUtils.addMinutes;

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

            Assert.assertEquals("Can't convert " + monthString + " to " + (i + 1), i + 1, AzanAppTimeUtils.convertMonthFromStringToInt(monthString));

            String monthStringUpperCase = monthString.toUpperCase();
            Assert.assertEquals("Can't convert " + monthStringUpperCase + " to " + (i + 1), i + 1, AzanAppTimeUtils.convertMonthFromStringToInt(monthStringUpperCase));

            String monthString3Letters = monthString.substring(0, 3);
            Assert.assertEquals("Can't convert " + monthString3Letters + " to " + (i + 1), i + 1, AzanAppTimeUtils.convertMonthFromStringToInt(monthString3Letters));

            String monthString3LettersUpperCase = monthString.substring(0, 3).toUpperCase();
            Assert.assertEquals("Can't convert " + monthString3LettersUpperCase + " to " + (i + 1), i + 1, AzanAppTimeUtils.convertMonthFromStringToInt(monthString3LettersUpperCase));
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

    @Test
    public void testConvertMillisToDate() {
        long timeInMillis = 1565525341000L - DAY_IN_MILLIS * 2;
        long timeInMillis2 = 1565525341000L;
        String dateTimeString = AzanAppTimeUtils.convertMillisToDateString(timeInMillis);
        String dateTimeString2 = AzanAppTimeUtils.convertMillisToDateString(timeInMillis2);
        // Note: this time represents GMT+02:00
        Assert.assertEquals("wrong value for converting", "09 Aug 2019", dateTimeString);
        Assert.assertEquals("wrong value for converting", "11 Aug 2019", dateTimeString2);
    }


    @Test
    public void testGetDayAfterDateString() {
        String dateString = "25 Oct 2019";
        String expectedDayAfterDateString = "";
        String actualDayAfterDateString = "";
        for (int i = 0; i < 10 * 365; i++) {
            final long dayAfterInMillis = AzanAppTimeUtils.convertDateToMillis(dateString) + DAY_IN_MILLIS;
            expectedDayAfterDateString = AzanAppTimeUtils.convertMillisToDateString(dayAfterInMillis);
            actualDayAfterDateString = AzanAppTimeUtils.getDayAfterDateString(dateString);
            Assert.assertEquals(expectedDayAfterDateString, actualDayAfterDateString);
            dateString = expectedDayAfterDateString;
        }

        Assert.assertEquals("02 Nov 2019", AzanAppTimeUtils.getDayAfterDateString("01 Nov 2019"));
        Assert.assertEquals("17 Jul 2019", AzanAppTimeUtils.getDayAfterDateString("16 Jul 2019"));
        Assert.assertEquals("30 Nov 2019", AzanAppTimeUtils.getDayAfterDateString("29 Nov 2019"));
        Assert.assertEquals("30 Apr 2019", AzanAppTimeUtils.getDayAfterDateString("29 Apr 2019"));
        Assert.assertEquals("29 Mar 2019", AzanAppTimeUtils.getDayAfterDateString("28 Mar 2019"));
        Assert.assertEquals("31 Aug 2019", AzanAppTimeUtils.getDayAfterDateString("30 Aug 2019"));

        Assert.assertEquals("01 Dec 2019", AzanAppTimeUtils.getDayAfterDateString("30 Nov 2019"));
        Assert.assertEquals("01 Nov 2019", AzanAppTimeUtils.getDayAfterDateString("31 Oct 2019"));
        Assert.assertEquals("01 Sep 2019", AzanAppTimeUtils.getDayAfterDateString("31 Aug 2019"));
        Assert.assertEquals("01 Mar 2019", AzanAppTimeUtils.getDayAfterDateString("28 Feb 2019"));
        Assert.assertEquals("29 Feb 2020", AzanAppTimeUtils.getDayAfterDateString("28 Feb 2020"));
        Assert.assertEquals("01 Mar 2020", AzanAppTimeUtils.getDayAfterDateString("29 Feb 2020"));

        Assert.assertEquals("01 Jan 2021", AzanAppTimeUtils.getDayAfterDateString("31 Dec 2020"));
        Assert.assertEquals("01 Jan 2020", AzanAppTimeUtils.getDayAfterDateString("31 Dec 2019"));

    }

    @Test
    public void testAddMinutes() {
        HourMinute hourMinute = addMinutes(new HourMinute(11, 15), 20);
        Assert.assertEquals(hourMinute.hour, 11);
        Assert.assertEquals(hourMinute.minute, 35);

        hourMinute = addMinutes(new HourMinute(22, 55), 14);
        Assert.assertEquals(hourMinute.hour, 23);
        Assert.assertEquals(hourMinute.minute, 9);

        hourMinute = addMinutes(new HourMinute(23, 50), 20);
        Assert.assertEquals(hourMinute.hour, 23);
        Assert.assertEquals(hourMinute.minute, 59);

        hourMinute = addMinutes(new HourMinute(15, 59), 59);
        Assert.assertEquals(hourMinute.hour, 16);
        Assert.assertEquals(hourMinute.minute, 58);
    }

    @Test
    public void testAddMinutesWithStringParams() {
        String hourMinute = addMinutes("11 : 15", "20");
        Assert.assertEquals(hourMinute, "11:35");

        hourMinute = addMinutes(" 22:55", "14");
        Assert.assertEquals(hourMinute, "23:09");

        hourMinute = addMinutes("23:50", "20");
        Assert.assertEquals(hourMinute, "23:59");

        hourMinute = addMinutes("15:59", "59");
        Assert.assertEquals(hourMinute, "16:58");
    }
}

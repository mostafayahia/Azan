package free.elmasry.azan.utilities;

import java.util.ArrayList;
import java.util.List;

public class MyStringUtils {

    /**
     * split the string like "abc11cd11" with splitter "11" to {"abc", "cd"}
     * @param input the string which has splitter
     * @param splitter the string you want the input string to split based on it
     * @return array of strings after splitting the input
     */
    public static String[] splitString(String input, String splitter) {
        if (input == null || input.isEmpty())
            return new String[0];

        if (splitter == null || splitter.isEmpty())
            return new String[]{input};

        List<String> outputList = new ArrayList<>();
        int splitterLen = splitter.length();

        int index = 0;
        StringBuilder sb = new StringBuilder();
        while (index < input.length()) {
            if (input.substring(index, index + splitterLen).equals(splitter)) {
                outputList.add(sb.toString());
                index += splitterLen;
                sb = new StringBuilder();
                continue;
            }
            sb.append(input.charAt(index++));
        }

        return outputList.toArray(new String[outputList.size()]);
    }
}

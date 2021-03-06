package split;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The main class for the Write Split assignment. This assignment is just to implement the {@link String#split(String)}
 * method for 1 character. My code does this and more. It supports a full string of any length as a regex, however it
 * does not support a real regex (ex. "abC" works as the regex while "a+" does not ("a+" will work, however not as a
 * regex would, it will only split at the first occurrence of "a+", not at any amount of a's more than 1)). The
 * {@link #split(String, String)} is the function main function we were tasked with implementing, however as I found
 * myself needing multiple implementations I created the {@link #split(String, String, BiFunction)} which takes a full
 * implementation of the split function as input. These are built into the class (ex.
 * {@link #boyerMooreSubstringSearchSplit(String, BoyerMoore)}). These are also public fields (ex {@link #KMPSubstring}).
 * Through testing, I believe I found that for some strings and regex the {@link #boyerMooreSubstringSearchSplit(String,
 * String)} is faster than the {@link String#split(String)}. However, I do not know if this is just for certain cases.
 *
 * @author luca
 */
public class WriteSplit {

    public final BiFunction<String, String, String[]> bruteForceSubstring = this::bruteForceSubstringSearchSplit;
    public final BiFunction<String, String, String[]> KMPSubstring = this::knuthMorisPrattSubstringSearchSplit;
    public final BiFunction<String, String, String[]> boyerMooreSubstring = this::boyerMooreSubstringSearchSplit;

    private final BiFunction<String, String, String[]> defaultBackend = boyerMooreSubstring;

    /**
     * The main function has some examples of my code and its use cases
     */
    public static void main(String[] args) {
        final WriteSplit split = new WriteSplit();
        final int tests = 10000;
        final int patternSize = 1000;
        final int stringSize = 1000000;

        // Generating a very large regex for this example, all 1000 test cases will be done using this regex.
        String regex = split.generateRandomString(patternSize);
        BoyerMoore boyerMoore = new BoyerMoore(regex);

        // For this example we will be testing 3 functions, str.split, boyerMooreSplit, and boyerMooreSplit with a
        // predefined regex.
        long[][] times = new long[tests][3];
        for (int i = 0; i < tests; i++) {
            String string = split.generateRandomString(stringSize);

            long startTime;
            long endTime;

            // Testing out str.split
            startTime = System.nanoTime();
            string.split(regex);
            endTime = System.nanoTime();
            times[i][0] = endTime - startTime;

            // Testing out default implementation of split (which is boyer-moore split)
            startTime = System.nanoTime();
            split.split(string, regex);
            endTime = System.nanoTime();
            times[i][1] = endTime - startTime;

            // Testing out boyer-moore split with a predefined BoyerMoore class
            startTime = System.nanoTime();
            split.boyerMooreSubstringSearchSplit(string, boyerMoore);
            endTime = System.nanoTime();
            times[i][2] = endTime - startTime;
        }

        double[] averages = new double[3];
        for (int i = 0; i < 3; i++) {
            long sum = 0;
            for (int j = 0; j < tests; j++) {
                sum += times[j][i];
            }
            averages[i] = (double) sum / tests;
        }
        System.out.printf("The average time for the three methods after %d test cases with a string of size %d and a" +
                " pattern of size %d \n", tests, stringSize, patternSize);
        System.out.printf("str.split: %.3fns\nmy implementation using Boyer-Moore substring search: %.3fns\n" +
                        "Boyer-Moore with predefined regex: %.3fns\n",
                averages[0], averages[1], averages[2]);

        System.out.printf("My implementation of split using Boyer-Moore algorithm for substring search is %.2f%% faster than str.split\n", (averages[0] - averages[1]) / averages[0] * 100);
        System.out.printf("Same algorithm expect using a predefined Boyer-Moore class is %.2f%% faster than str.split\n", (averages[0] - averages[2]) / averages[0] * 100);

        // The "optimized" code is not that useful as it is not common to have to split different strings with the same
        // patter, but if you need too, it is a lot quicker.
    }

    /**
     * To keep to the spirit of coding the split method from scratch, I am also coding the substring search from scratch.
     * Should do the same thing as {@link String#indexOf(String, int)}.
     */
    private int bruteForceSubstringSearch(String str, String regex, int start) {
        for (int i = start; i <= str.length() - regex.length(); i++) {
            for (int j = 0; j <= regex.length(); j++) {
                if (regex.charAt(j) != str.charAt(i + j)) {
                    break;
                } else if (j == regex.length() - 1) {
                    return i;
                }
            }
        }
        // The regex was not found
        return -1;
    }

    /**
     * Returns what a regular str.split does, taking the same inputs.
     * The algorithms used works like this, it uses {@link #bruteForceSubstringSearch(String, String, int)} to find the
     * first occurrence of the regex, and the puts the substring from the last split (or 0 if it is the first split) to
     * the location of the substring into an {@link ArrayList<String>}. At the end the {@link ArrayList<String>} is
     * turned into a String[] and trimmed to return the same value as the split method in Strings.
     */
    private String[] bruteForceSubstringSearchSplit(String str, String regex) {
        ArrayList<String> split = new ArrayList<>();

        int i = 0;
        int regexFound;
        while (true) {
            regexFound = bruteForceSubstringSearch(str, regex, i);
            // Break out of the loop if there are no more cases of the regex
            if (regexFound == -1) {
                break;
            }
            // Split the string at the first occurrence of the regex
            split.add(str.substring(i, regexFound));
            // Increase the current letter we are at at to after where the previous regex occurrence was
            i = regexFound + regex.length();
        }

        // If we have not gotten to the end of the string, add it as well
        if (i < str.length()) {
            split.add(str.substring(i));
        } else {
            // Remove strings of size 0 at the to match the output of str.split
            for (int j = split.size() - 1; j >= 0; j--) {
                if (split.get(j).length() == 0) {
                    split.remove(j);
                } else {
                    break;
                }
            }
        }

        return split.toArray(String[]::new);
    }

    /**
     * A easy to use function that takes the same input as all the other split functions. All it does is call the
     * {@link #knuthMorisPrattSubstringSearchSplit(String, KMP)} which takes a already defined {@link KMP}. The only
     * reason you would call the other function is if you have already done the preprocessing necessary for KMP.
     * @param str String to split
     * @param regex String to split it at, does not support full regex
     * @return An array split at each location of regex
     */
    private String[] knuthMorisPrattSubstringSearchSplit(String str, String regex) {
        return  knuthMorisPrattSubstringSearchSplit(str, new KMP(regex));
    }

    /**
     * Same algorithm as {@link #bruteForceSubstringSearchSplit(String, String)} except uses {@link KMP#search(String)}
     * instead of {@link #bruteForceSubstringSearch(String, String, int)}.
     */
    private String[] knuthMorisPrattSubstringSearchSplit(String str, KMP regex) {
        ArrayList<String> split = new ArrayList<>();

        int i = 0;
        int regexFound;
        while (true) {
            regexFound = regex.search(str, i);
            // Break out of the loop if there are no more cases of the regex
            if (regexFound == -1) {
                break;
            }
            // Split the string at the first occurrence of the regex
            split.add(str.substring(i, regexFound));
            // Increase the current letter we are at at to after where the previous regex occurrence was
            i = regexFound + regex.getPattern().length();
        }

        // If we have not gotten to the end of the string, add it as well
        if (i < str.length()) {
            split.add(str.substring(i));
        } else {
            // Remove strings of size 0 at the to match the output of str.split
            for (int j = split.size() - 1; j >= 0; j--) {
                if (split.get(j).length() == 0) {
                    split.remove(j);
                } else {
                    break;
                }
            }
        }

        return split.toArray(String[]::new);
    }

    /**
     * A easy to use function that takes the same input as all the other split functions. All it does is call the actual
     * {@link #boyerMooreSubstringSearchSplit(String, BoyerMoore)} instead which takes a already defined
     * {@link BoyerMoore}. The only reason you would call the other function is if you have already done the
     * preprocessing necessary for Boyer-Moore.
     * @param str String to split
     * @param regex String to split it at, does not support full regex
     * @return An array split at each location of regex
     */
    private String[] boyerMooreSubstringSearchSplit(String str, String regex) {
        return boyerMooreSubstringSearchSplit(str, new BoyerMoore(regex));
    }

    private String[] boyerMooreSubstringSearchSplit(String str, BoyerMoore regex) {
        ArrayList<String> split = new ArrayList<>();

        int i = 0;
        int regexFound;
        while (true) {
            regexFound = regex.search(str, i);
            // Break out of the loop if there are no more cases of the regex
            if (regexFound == -1) {
                break;
            }
            // Split the string at the first occurrence of the regex
            split.add(str.substring(i, regexFound));
            // Increase the current letter we are at at to after where the previous regex occurrence was
            i = regexFound + regex.getPattern().length();
        }

        // If we have not gotten to the end of the string, add it as well
        if (i < str.length()) {
            split.add(str.substring(i));
        } else {
            // Remove strings of size 0 at the to match the output of str.split
            for (int j = split.size() - 1; j >= 0; j--) {
                if (split.get(j).length() == 0) {
                    split.remove(j);
                } else {
                    break;
                }
            }
        }

        return split.toArray(String[]::new);
    }

    /**
     * Should do the same thing as {@link String#split(String)}. Uses {@link WriteSplit#defaultBackend} as the backend
     * @param str The string to be split
     * @param regex The pattern to match (does not work with regex at the moment, needs to be a String of any length)
     * @return An String array of the substrings split at every occurrence of the regex
     */
    public String[] split(String str, String regex) {
        return split(str, regex, defaultBackend);
    }

    /**
     * Splits a string using a custom backend
     * @param str The string to be split
     * @param regex The pattern to match (does not work with regex at the moment, needs to be a String of any length)
     * @param backend The function to use to split it
     * @return An String array of the substrings split at every occurrence of the regex
     */
    public String[] split(String str, String regex, BiFunction<String, String, String[]> backend) {
        return backend.apply(str, regex);
    }

    @Test
    public void testSplit() {
        // TODO: add manual test cases

        for (int i = 0; i < 1000; i++) {
            String randomString = generateRandomString(100);

            String regex1 = generateRandomString(1);
            assertArrayEquals(randomString.split(regex1), split(randomString, regex1));

            String regex2 = generateRandomString(2);
            assertArrayEquals(randomString.split(regex2), split(randomString, regex2));
        }

        // Larger test cases
        for (int i = 1; i < 100000; i += 10) {
            String randomString = generateRandomString(i);
            String regex = generateRandomString(i / 10 + 1);

            assertArrayEquals(randomString.split(regex), split(randomString, regex));
        }
    }

    /**
     * Generates a random string from the alphabet provided.
     * @param length The length of the random string to be generated
     * @param alphabet The alphabet to use
     * @return the array of strings computed by splitting this string around matches of the given regular expression
     */
    private String generateRandomString(int length, String alphabet) {
        Random random = new Random();

        StringBuilder string = new StringBuilder();

        for (int i = 0; i < length; i++) {
            string.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }

        return string.toString();
    }

    /**
     * A function used for testing, generates a random string with the lowercase letters of the alphabet
     * @param length The length of the string
     * @return The random string
     */
    private String generateRandomString(int length) {
        return generateRandomString(length, "qwertyuiopasdfghjklzxcvbnm");
    }

    @Test
    public void testSearch() {
        String pattern = "cat";
        KMP kmp = new KMP(pattern);
        BoyerMoore boyer = new BoyerMoore(pattern);

        String[] testStrings = {"The short brown cat jumps over the fence",
                "The two cats like to be cats because they are cats",
                "A cat is a cat I guess"};
        for (String i : testStrings) {
            assertEquals(i.indexOf(pattern), kmp.search(i));
            assertEquals(i.indexOf(pattern), boyer.search(i));
            assertEquals(i.indexOf(pattern), bruteForceSubstringSearch(i, pattern, 0));
        }

        for (int i = 0; i < 100000; i += 10) {
            String randomString = generateRandomString(i);
            String regex = generateRandomString(i / 10 + 1);

            kmp = new KMP(regex);
            boyer = new BoyerMoore(regex);

            assertEquals(randomString.indexOf(regex), kmp.search(randomString));
            assertEquals(randomString.indexOf(regex), boyer.search(randomString));
            assertEquals(randomString.indexOf(regex), bruteForceSubstringSearch(randomString, regex, 0));
        }
    }

    @Test
    public void testSpeed() {
        final int tests = 1000;

        BiFunction<String, String, String[]>[] splitFunctions = new BiFunction[3];
        splitFunctions[0] = defaultBackend;
        splitFunctions[1] = KMPSubstring;
        splitFunctions[2] = bruteForceSubstring;

        // The tests might take a while (such as 3+ minutes), so if it does not run quickly, it is not because of an
        // infinite loop. To make them run faster reduce the generated string length and the regex length, you can also
        // change the number of tests done. To lower the length of the strings generated, change the parameter of
        // generateRandomString.
        long[][] timesSplit = new long[tests][splitFunctions.length + 1]; // + 1 to allow space for str.split()
        for (int i = 0; i < tests; i++) {
            String string = generateRandomString(10000000);
            String regex = generateRandomString(100000);

            long startTime;
            long endTime;

            int j = 0;
            for (BiFunction<String, String, String[]> fun : splitFunctions) {
                startTime = System.nanoTime();
                split(string, regex, fun);
                endTime = System.nanoTime();
                timesSplit[i][j] = endTime - startTime;

                j++;
            }

            startTime = System.nanoTime();
            string.split(regex);
            endTime = System.nanoTime();
            timesSplit[i][timesSplit[0].length - 1] = endTime - startTime;
        }

        double[] average = new double[timesSplit[0].length];
        double min = Double.MAX_VALUE;
        for (int i = 0; i < timesSplit[0].length; i++) {
            long sum = 0;
            for (int j = 0; j < tests; j++) {
                sum += timesSplit[j][i];
            }
            average[i] = (double) sum / tests;

            // Make sure that the minimum is not the str.split()
            if (i != average.length - 1 && average[i] < min) {
                min = average[i];
            }
        }
        System.out.println(Arrays.toString(average));

        // Tests that our algorithm is faster than String.split
        assertTrue(average[average.length - 1] - min > 0);
    }
}

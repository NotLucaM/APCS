package split;

/**
 * An implementation of Knuth-Morris-Pratt substring search which I coded some time ago.
 *
 * @author luca
 */
public class KMP {
    private static final int R = 128; // size of our alphabet.

    private String pattern;
    private int[][] dfa;

    public KMP(String pattern) {
        this.pattern = pattern;
        int patternSize = pattern.length();
        this.dfa = new int[R][patternSize];

       dfa[pattern.charAt(0)][0] = 1;
       int i = 0;
        for (int j = 1; j < patternSize; j++) {
            for (int k = 0; k < R; k++) {
                dfa[k][j] = dfa[k][i];
            }

            dfa[pattern.charAt(j)][j] = j + 1;
            i = dfa[pattern.charAt(j)][i];
        }
    }

    /**
     * Use the pattern to search through String str
     * @param str The string to be searched
     * @param i The starting location for the search to begin at
     * @return The first location of the pattern or -1, if not found
     */
    public int search(String str, int i) {
        int j = 0;
        int stringLength = str.length();
        int patternLength = pattern.length();

        while (i < stringLength && j < patternLength) {
            j = dfa[str.charAt(i)][j];
            i++;
        }
        if (j == patternLength) {
            // Found
            return i - patternLength;
        } else {
            // not found
            return -1;
        }
    }

    /**
     * Use the pattern to search through String str, starts at index 0
     * @param str The string to be searched
     * @return The first location of the pattern or -1, if not found
     */
    public int search(String str) {
        return search(str, 0);
    }

    public String getPattern() {
        return pattern;
    }
}
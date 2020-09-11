public class LogicalOperator {

    // A regular expression to find if a string is a legal logical expression
    private static final String legalSentenceRegex = "([~]*[a-z]{1}){1}((\\s)*(=>|<=|<=*>|[|,&]){1}(\\s)*([~]*[a-z]{1}){1})*";
    private static final String legalSimpleRegex = "[a-z]{1}";
    private static final String negationRegex = "[~]*[a-z]{1}";
    private static final String splitRegex = "[=*>|<=*|<=*>|[|,&]]";

    public static void main(String[] args) {

    }

    private static boolean legalSimple(String simple) {
        return simple.matches(legalSimpleRegex);
    }

    /**
     * Returns true if the sentence is a legal complex sentence. It will return true if it is a simple
     * sentence. Some examples of legal complex sentences are ~~a or a => ~~b.
     * @param complex The sentence to check
     * @return If the sentence is a legal logical sentence
     */
    private static boolean legalComplex(String complex) {
        if (negation(complex) || legalSimple(complex)) {
            return true;
        }

        // We know the sentence is not a negation, therefore it must have a biconditional, implication,
        // conjunction, or a disjunction
        String[] splitComplex = complex.split(splitRegex, 1); // We split the string into 2 parts

        return legalComplex(splitComplex[0]) || legalComplex(splitComplex[1]);
    }

    /**
     * Finds if a string contains exactly one negation. A negation can be in the form of anything
     * similar to ~a or ~~~~z.
     * @param negation The string to check
     * @return If the string is a negation
     */
    public static boolean negation(String negation) {
        return negation.matches(negationRegex);
    }
}

package edu.stanford.slac.aida.test;

public class AidaClientTestUtils {
    /**
     * Show an abbreviated version of the error message
     *
     * @param message the error message
     * @return abbreviated version of the given message
     */
    public static String abbreviate(String message) {
        int end = message.indexOf(".");
        int endC = message.indexOf(", cause:");
        if (end == -1) {
            return message;
        }
        if (endC == -1) {
            endC = end;
        }
        return message.substring(0, Math.min(end, endC));
    }

}

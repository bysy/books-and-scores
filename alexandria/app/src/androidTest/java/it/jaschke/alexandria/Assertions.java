package it.jaschke.alexandria;

import static junit.framework.Assert.fail;


/** Provide additional assertion. */
class Assertions {

    // http://stackoverflow.com/questions/7552587/how-to-test-assert-throws-exception-in-android/15603133#15603133

    public interface Testable {
        public void run() throws Exception;
    }

    public static <T extends Exception> T assertThrows(
            final Class<T> expected,
            final Testable codeUnderTest) throws Exception {
        T result = null;
        try {
            codeUnderTest.run();
            fail("Expecting exception but none was thrown.");
        } catch (final Exception actual) {
            if (expected.isInstance(actual)) {
                result = expected.cast(actual);
            } else {
                throw actual;
            }
        }
        return result;
    }
}

package it.jaschke.alexandria;

import android.test.AndroidTestCase;

/**
 * Test AddBook.
 */
public class TestAddBook extends AndroidTestCase {
    public void testExtractDigits() {
        assertEquals("0123456", AddBook.extractDigits("0 1--2...3abc456"));
    }
}

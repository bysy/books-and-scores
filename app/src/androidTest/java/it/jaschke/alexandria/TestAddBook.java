package it.jaschke.alexandria;

import android.test.AndroidTestCase;

/**
 * Test AddBook.
 */
public class TestAddBook extends AndroidTestCase {
    public void testExtractDigits() {
        assertEquals("0123456", AddBook.extractDigits("0 1--2...3abc456"));
    }

    public void testStartsWithIsbn13Prefix() {
        assertTrue(AddBook.startsWithIsbn13Prefix("9781234567890"));
        assertTrue(AddBook.startsWithIsbn13Prefix("9790000000000"));
        assertFalse(AddBook.startsWithIsbn13Prefix("4242424242424"));
    }
}

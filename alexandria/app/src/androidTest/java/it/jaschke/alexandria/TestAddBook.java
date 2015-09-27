package it.jaschke.alexandria;

import android.test.AndroidTestCase;

/**
 * Test AddBook.
 */
public class TestAddBook extends AndroidTestCase {
    public void testExtractDigits() {
        assertEquals("0123456", AddBook.extractDigits("0 1--2...3abc456"));
    }

    public void testGetIntAt() {
        assertEquals(1, AddBook.getIntAt("1234", 0));
        assertEquals(3, AddBook.getIntAt("1234", 2));
        assertEquals(4, AddBook.getIntAt("1234", 3));
    }

    public void testStartsWithIsbn13Prefix() {
        assertTrue(AddBook.startsWithIsbn13Prefix("9781234567890"));
        assertTrue(AddBook.startsWithIsbn13Prefix("9790000000000"));
        assertFalse(AddBook.startsWithIsbn13Prefix("4242424242424"));
    }

    public void testIsValidIsbn13() {
        assertFalse(AddBook.isValidIsbn13(""));
        assertTrue(AddBook.isValidIsbn13("9790901679177"));  // ISMN
        assertTrue(AddBook.isValidIsbn13("9790060115615"));

        assertTrue( AddBook.isValidIsbn13("9783161484100"));  // ISBN
        assertFalse(AddBook.isValidIsbn13("9783161484102"));
        assertFalse(AddBook.isValidIsbn13("9783261484100"));
    }
}

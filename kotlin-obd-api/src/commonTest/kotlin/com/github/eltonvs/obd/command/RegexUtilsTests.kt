package com.github.eltonvs.obd.command

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoveAllTests {

    @Test
    fun `test removeAll with single pattern`() {
        val result = removeAll(RegexPatterns.WHITESPACE_PATTERN, "hello world")
        assertEquals("helloworld", result)
    }

    @Test
    fun `test removeAll with multiple whitespace occurrences`() {
        val result = removeAll(RegexPatterns.WHITESPACE_PATTERN, "  a  b  c  ")
        assertEquals("abc", result)
    }

    @Test
    fun `test removeAll with no matches returns original`() {
        val result = removeAll(RegexPatterns.WHITESPACE_PATTERN, "nospaces")
        assertEquals("nospaces", result)
    }

    @Test
    fun `test removeAll with empty string`() {
        val result = removeAll(RegexPatterns.WHITESPACE_PATTERN, "")
        assertEquals("", result)
    }

    @Test
    fun `test removeAll with multiple patterns`() {
        val input = "SEARCHING... 41 00 00 00 00"
        val result = removeAll(input, RegexPatterns.SEARCHING_PATTERN, RegexPatterns.WHITESPACE_PATTERN)
        assertEquals("...4100000000", result)
    }

    @Test
    fun `test removeAll with multiple patterns in order`() {
        val input = "BUS INIT OK\r\n"
        val result = removeAll(input, RegexPatterns.BUS_INIT_PATTERN, RegexPatterns.CARRIAGE_PATTERN)
        assertEquals(" OK", result)
    }
}

class WhitespacePatternTests {

    @Test
    fun `test matches space`() {
        assertTrue(RegexPatterns.WHITESPACE_PATTERN.containsMatchIn(" "))
    }

    @Test
    fun `test matches tab`() {
        assertTrue(RegexPatterns.WHITESPACE_PATTERN.containsMatchIn("\t"))
    }

    @Test
    fun `test matches newline`() {
        assertTrue(RegexPatterns.WHITESPACE_PATTERN.containsMatchIn("\n"))
    }

    @Test
    fun `test does not match letters`() {
        assertFalse(RegexPatterns.WHITESPACE_PATTERN.containsMatchIn("abc"))
    }
}

class BusInitPatternTests {

    @Test
    fun `test matches BUS INIT`() {
        assertTrue(RegexPatterns.BUS_INIT_PATTERN.containsMatchIn("BUS INIT"))
    }

    @Test
    fun `test matches BUSINIT`() {
        assertTrue(RegexPatterns.BUS_INIT_PATTERN.containsMatchIn("BUSINIT"))
    }

    @Test
    fun `test matches period`() {
        assertTrue(RegexPatterns.BUS_INIT_PATTERN.containsMatchIn("."))
    }

    @Test
    fun `test matches multiple periods`() {
        assertTrue(RegexPatterns.BUS_INIT_PATTERN.containsMatchIn("..."))
    }
}

class SearchingPatternTests {

    @Test
    fun `test matches SEARCHING`() {
        assertTrue(RegexPatterns.SEARCHING_PATTERN.containsMatchIn("SEARCHING"))
    }

    @Test
    fun `test matches SEARCHING in context`() {
        assertTrue(RegexPatterns.SEARCHING_PATTERN.containsMatchIn("SEARCHING..."))
    }

    @Test
    fun `test does not match lowercase`() {
        assertFalse(RegexPatterns.SEARCHING_PATTERN.containsMatchIn("searching"))
    }
}

class CarriagePatternTests {

    @Test
    fun `test matches carriage return`() {
        assertTrue(RegexPatterns.CARRIAGE_PATTERN.containsMatchIn("\r"))
    }

    @Test
    fun `test matches newline`() {
        assertTrue(RegexPatterns.CARRIAGE_PATTERN.containsMatchIn("\n"))
    }

    @Test
    fun `test matches both in string`() {
        assertTrue(RegexPatterns.CARRIAGE_PATTERN.containsMatchIn("\r\n"))
    }
}

class CarriageColonPatternTests {

    @Test
    fun `test matches carriage return followed by colon pattern`() {
        assertTrue(RegexPatterns.CARRIAGE_COLON_PATTERN.containsMatchIn("\r0:"))
    }

    @Test
    fun `test matches newline followed by colon pattern`() {
        assertTrue(RegexPatterns.CARRIAGE_COLON_PATTERN.containsMatchIn("\n1:"))
    }
}

class ColonPatternTests {

    @Test
    fun `test matches colon`() {
        assertTrue(RegexPatterns.COLON_PATTERN.containsMatchIn(":"))
    }

    @Test
    fun `test matches multiple colons`() {
        assertEquals(2, RegexPatterns.COLON_PATTERN.findAll("a:b:c").count())
    }
}

class DigitsLettersPatternTests {

    @Test
    fun `test matches hex digits`() {
        assertTrue(RegexPatterns.DIGITS_LETTERS_PATTERN.containsMatchIn("41010080"))
    }

    @Test
    fun `test matches hex with letters`() {
        assertTrue(RegexPatterns.DIGITS_LETTERS_PATTERN.containsMatchIn("4101AABB"))
    }

    @Test
    fun `test matches hex with colons`() {
        assertTrue(RegexPatterns.DIGITS_LETTERS_PATTERN.containsMatchIn("41:01:00:80"))
    }

    @Test
    fun `test does not match lowercase hex`() {
        assertFalse(RegexPatterns.DIGITS_LETTERS_PATTERN.matches("41aabb"))
    }
}

class StartsWithAlphanumPatternTests {

    @Test
    fun `test matches special characters`() {
        assertTrue(RegexPatterns.STARTS_WITH_ALPHANUM_PATTERN.containsMatchIn("!@#"))
    }

    @Test
    fun `test does not match alphanumeric`() {
        assertFalse(RegexPatterns.STARTS_WITH_ALPHANUM_PATTERN.containsMatchIn("abc123"))
    }

    @Test
    fun `test matches in mixed string`() {
        assertTrue(RegexPatterns.STARTS_WITH_ALPHANUM_PATTERN.containsMatchIn("hello!world"))
    }
}

class ErrorMessagePatternTests {

    @Test
    fun `test BUSINIT_ERROR_MESSAGE_PATTERN matches`() {
        assertTrue("BUS INIT... ERROR".contains(RegexPatterns.BUSINIT_ERROR_MESSAGE_PATTERN))
    }

    @Test
    fun `test MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN matches`() {
        assertTrue("?".contains(RegexPatterns.MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN))
    }

    @Test
    fun `test NO_DATA_MESSAGE_PATTERN matches`() {
        assertTrue("NO DATA".contains(RegexPatterns.NO_DATA_MESSAGE_PATTERN))
    }

    @Test
    fun `test STOPPED_MESSAGE_PATTERN matches`() {
        assertTrue("STOPPED".contains(RegexPatterns.STOPPED_MESSAGE_PATTERN))
    }

    @Test
    fun `test UNABLE_TO_CONNECT_MESSAGE_PATTERN matches`() {
        assertTrue("UNABLE TO CONNECT".contains(RegexPatterns.UNABLE_TO_CONNECT_MESSAGE_PATTERN))
    }

    @Test
    fun `test ERROR_MESSAGE_PATTERN matches`() {
        assertTrue("ERROR".contains(RegexPatterns.ERROR_MESSAGE_PATTERN))
    }

    @Test
    fun `test UNSUPPORTED_COMMAND_MESSAGE_PATTERN matches 7F 01 11`() {
        assertTrue("7F 01 11".matches(RegexPatterns.UNSUPPORTED_COMMAND_MESSAGE_PATTERN.toRegex()))
    }

    @Test
    fun `test UNSUPPORTED_COMMAND_MESSAGE_PATTERN matches 7F 0A 12`() {
        assertTrue("7F 0A 12".matches(RegexPatterns.UNSUPPORTED_COMMAND_MESSAGE_PATTERN.toRegex()))
    }

    @Test
    fun `test UNSUPPORTED_COMMAND_MESSAGE_PATTERN does not match valid response`() {
        assertFalse("41 01 00".matches(RegexPatterns.UNSUPPORTED_COMMAND_MESSAGE_PATTERN.toRegex()))
    }
}

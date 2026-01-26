package com.github.eltonvs.obd.command

public object RegexPatterns {
    public val WHITESPACE_PATTERN: Regex = "\\s".toRegex()
    public val BUS_INIT_PATTERN: Regex = "(BUS INIT)|(BUSINIT)|(\\.)".toRegex()
    public val SEARCHING_PATTERN: Regex = "SEARCHING".toRegex()
    public val CARRIAGE_PATTERN: Regex = "[\r\n]".toRegex()
    public val CARRIAGE_COLON_PATTERN: Regex = "[\r\n].:".toRegex()
    public val COLON_PATTERN: Regex = ":".toRegex()
    public val DIGITS_LETTERS_PATTERN: Regex = "([0-9A-F:])+".toRegex()
    public val STARTS_WITH_ALPHANUM_PATTERN: Regex = "[^a-z0-9 ]".toRegex(RegexOption.IGNORE_CASE)

    // Error patterns
    public const val BUSINIT_ERROR_MESSAGE_PATTERN: String = "BUS INIT... ERROR"
    public const val MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN: String = "?"
    public const val NO_DATE_MESSAGE_PATTERN: String = "NO DATA"
    public const val STOPPED_MESSAGE_PATTERN: String = "STOPPED"
    public const val UNABLE_TO_CONNECT_MESSAGE_PATTERN: String = "UNABLE TO CONNECT"
    public const val ERROR_MESSAGE_PATTERN: String = "ERROR"
    public const val UNSUPPORTED_COMMAND_MESSAGE_PATTERN: String = "7F 0[0-A] 1[1-2]"
}

public fun removeAll(pattern: Regex, input: String): String {
    return pattern.replace(input, "")
}

public fun removeAll(input: String, vararg patterns: Regex): String =
    patterns.fold(input) { acc, pattern -> removeAll(pattern, acc) }

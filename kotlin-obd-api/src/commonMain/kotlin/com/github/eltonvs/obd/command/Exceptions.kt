package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.command.RegexPatterns.BUSINIT_ERROR_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.DIGITS_LETTERS_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.ERROR_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.NO_DATA_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.STOPPED_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.UNABLE_TO_CONNECT_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.UNSUPPORTED_COMMAND_MESSAGE_PATTERN
import com.github.eltonvs.obd.command.RegexPatterns.WHITESPACE_PATTERN


private fun String.sanitize(): String = removeAll(WHITESPACE_PATTERN, this).uppercase()

public abstract class BadResponseException(public val command: ObdCommand, public val response: ObdRawResponse) :
    RuntimeException() {
    public companion object {
        public fun checkForExceptions(command: ObdCommand, response: ObdRawResponse): ObdRawResponse =
            with(response.value.sanitize()) {
                when {
                    contains(BUSINIT_ERROR_MESSAGE_PATTERN.sanitize()) ->
                        throw BusInitException(command, response)
                    contains(MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN.sanitize()) ->
                        throw MisunderstoodCommandException(command, response)
                    contains(NO_DATA_MESSAGE_PATTERN.sanitize()) ->
                        throw NoDataException(command, response)
                    contains(STOPPED_MESSAGE_PATTERN.sanitize()) ->
                        throw StoppedException(command, response)
                    contains(UNABLE_TO_CONNECT_MESSAGE_PATTERN.sanitize()) ->
                        throw UnableToConnectException(command, response)
                    contains(ERROR_MESSAGE_PATTERN.sanitize()) ->
                        throw UnknownErrorException(command, response)
                    matches(UNSUPPORTED_COMMAND_MESSAGE_PATTERN.toRegex()) ->
                        throw UnSupportedCommandException(command, response)
                    !command.skipDigitCheck && !matches(DIGITS_LETTERS_PATTERN) ->
                        throw NonNumericResponseException(command, response)
                    else -> response
                }
            }
    }

    override fun toString(): String =
        "Error while executing command [${command.tag}], response [${response.value}]"
}


private typealias BRE = BadResponseException

public class NonNumericResponseException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
public class BusInitException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
public class MisunderstoodCommandException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
public class NoDataException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
public class StoppedException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
public class UnableToConnectException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
public class UnknownErrorException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
public class UnSupportedCommandException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)

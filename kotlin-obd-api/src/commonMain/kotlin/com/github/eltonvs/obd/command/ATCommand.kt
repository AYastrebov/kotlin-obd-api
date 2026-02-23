package com.github.eltonvs.obd.command


public abstract class ATCommand : ObdCommand() {
    override val mode: String = "AT"
    override val skipDigitCheck: Boolean = true
    override val category: CommandCategory = CommandCategory.AT_CONFIGURATION

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<String> =
        TypedValue.StringValue(rawResponse.value)
}

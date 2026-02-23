package com.github.eltonvs.obd.command


public abstract class ObdCommand {
    public abstract val tag: String
    public abstract val name: String
    public abstract val mode: String
    public abstract val pid: String

    public open val defaultUnit: String = ""
    public open val skipDigitCheck: Boolean = false
    public open val category: CommandCategory = CommandCategory.UNKNOWN

    public val rawCommand: String
        get() = "$mode $pid"

    public abstract fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<*>

    public open fun handleResponse(rawResponse: ObdRawResponse): ObdResponse {
        val checkedRawResponse = BadResponseException.checkForExceptions(this, rawResponse)
        val typedValue = parseTypedValue(checkedRawResponse)
        return ObdResponse(
            command = this,
            rawResponse = checkedRawResponse,
            value = typedValue.stringValue,
            unit = defaultUnit,
            typedValue = typedValue
        )
    }

    public open fun format(response: ObdResponse): String = "${response.value}${response.unit}"
}

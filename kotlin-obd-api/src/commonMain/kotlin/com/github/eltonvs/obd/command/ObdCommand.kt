package com.github.eltonvs.obd.command


public abstract class ObdCommand {
    public abstract val tag: String
    public abstract val name: String
    public abstract val mode: String
    public abstract val pid: String

    public open val defaultUnit: String = ""
    public open val skipDigitCheck: Boolean = false
    public open val handler: (ObdRawResponse) -> String = { it.value }

    public val rawCommand: String
        get() = listOf(mode, pid).joinToString(" ")

    public open fun handleResponse(rawResponse: ObdRawResponse): ObdResponse {
        val checkedRawResponse = BadResponseException.checkForExceptions(this, rawResponse)
        return ObdResponse(
            command = this,
            rawResponse = checkedRawResponse,
            value = handler(checkedRawResponse),
            unit = defaultUnit
        )
    }

    public open fun format(response: ObdResponse): String = "${response.value}${response.unit}"
}

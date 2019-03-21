package com.github.eltonvs.obd.command

import com.github.eltonvs.obd.domain.ObdRawResponse
import com.github.eltonvs.obd.domain.ObdResponse

abstract class ObdCommand {
    abstract val tag: String
    abstract val name: String
    abstract val mode: String
    abstract val pid: String

    open val defaultUnit: String = ""
    open val handler: (String) -> String = { x: String -> x }

    fun handleResponse(rawResponse: ObdRawResponse): ObdResponse =
        ObdResponse(
            command = this,
            rawResponse = rawResponse,
            value = handler(rawResponse.value),
            unit = defaultUnit
        )

    open fun format(response: ObdResponse): String = "${response.value}${response.unit}"
}
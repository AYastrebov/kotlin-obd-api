package com.github.eltonvs.obd.command.at

import com.github.eltonvs.obd.command.ATCommand
import com.github.eltonvs.obd.command.ObdProtocols
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.TypedValue


public class DescribeProtocolCommand : ATCommand() {
    override val tag: String = "DESCRIBE_PROTOCOL"
    override val name: String = "Describe Protocol"
    override val pid: String = "DP"
}

public class DescribeProtocolNumberCommand : ATCommand() {
    override val tag: String = "DESCRIBE_PROTOCOL_NUMBER"
    override val name: String = "Describe Protocol Number"
    override val pid: String = "DPN"

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<String> {
        val protocol = parseProtocolNumber(rawResponse)
        return TypedValue.StringValue(protocol.displayName)
    }

    private fun parseProtocolNumber(rawResponse: ObdRawResponse): ObdProtocols {
        val result = rawResponse.value
        val protocolNumber = result[if (result.length == 2) 1 else 0].toString()
        return ObdProtocols.values().find { it.command == protocolNumber } ?: ObdProtocols.UNKNOWN
    }
}

public class IgnitionMonitorCommand : ATCommand() {
    override val tag: String = "IGNITION_MONITOR"
    override val name: String = "Ignition Monitor"
    override val pid: String = "IGN"

    override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<String> =
        TypedValue.StringValue(rawResponse.value.trim().uppercase())
}

public class AdapterVoltageCommand : ATCommand() {
    override val tag: String = "ADAPTER_VOLTAGE"
    override val name: String = "OBD Adapter Voltage"
    override val pid: String = "RV"
}

package com.github.eltonvs.obd.command.at

import com.github.eltonvs.obd.command.ATCommand
import com.github.eltonvs.obd.command.AdaptiveTimingMode
import com.github.eltonvs.obd.command.ObdProtocols
import com.github.eltonvs.obd.command.Switcher


public class SelectProtocolCommand(protocol: ObdProtocols) : ATCommand() {
    private val _protocol: ObdProtocols = if (protocol == ObdProtocols.UNKNOWN) ObdProtocols.AUTO else protocol
    override val tag: String = "SELECT_PROTOCOL_${_protocol.name}"
    override val name: String = "Select Protocol - ${_protocol.displayName}"
    override val pid: String = "SP ${_protocol.command}"
}

public class SetAdaptiveTimingCommand(value: AdaptiveTimingMode) : ATCommand() {
    override val tag: String = "SET_ADAPTIVE_TIMING_${value.name}"
    override val name: String = "Set Adaptive Timing Control ${value.displayName}"
    override val pid: String = "AT ${value.command}"
}

public class SetEchoCommand(value: Switcher) : ATCommand() {
    override val tag: String = "SET_ECHO_${value.name}"
    override val name: String = "Set Echo ${value.name}"
    override val pid: String = "E${value.command}"
}

public class SetHeadersCommand(value: Switcher) : ATCommand() {
    override val tag: String = "SET_HEADERS_${value.name}"
    override val name: String = "Set Headers ${value.name}"
    override val pid: String = "H${value.command}"
}

public class SetLineFeedCommand(value: Switcher) : ATCommand() {
    override val tag: String = "SET_LINE_FEED_${value.name}"
    override val name: String = "Set Line Feed ${value.name}"
    override val pid: String = "L${value.command}"
}

public class SetSpacesCommand(value: Switcher) : ATCommand() {
    override val tag: String = "SET_SPACES_${value.name}"
    override val name: String = "Set Spaces ${value.name}"
    override val pid: String = "S${value.command}"
}

public class SetTimeoutCommand(timeout: Int) : ATCommand() {
    override val tag: String = "SET_TIMEOUT"
    override val name: String = "Set Timeout - $timeout"
    override val pid: String = "ST ${timeout.toHex()}"
}

public fun Int.toHex(): String {
    return this.and(0xFF).toString(16).padStart(2, '0').uppercase()
}

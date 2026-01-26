package com.github.eltonvs.obd.command.at

import com.github.eltonvs.obd.command.ATCommand


public class ResetAdapterCommand : ATCommand() {
    override val tag: String = "RESET_ADAPTER"
    override val name: String = "Reset OBD Adapter"
    override val pid: String = "Z"
}

public class WarmStartCommand : ATCommand() {
    override val tag: String = "WARM_START"
    override val name: String = "OBD Warm Start"
    override val pid: String = "WS"
}

public class SlowInitiationCommand : ATCommand() {
    override val tag: String = "SLOW_INITIATION"
    override val name: String = "OBD Slow Initiation"
    override val pid: String = "SI"
}

public class LowPowerModeCommand : ATCommand() {
    override val tag: String = "LOW_POWER_MODE"
    override val name: String = "OBD Low Power Mode"
    override val pid: String = "LP"
}

public class BufferDumpCommand : ATCommand() {
    override val tag: String = "BUFFER_DUMP"
    override val name: String = "OBD Buffer Dump"
    override val pid: String = "BD"
}

public class BypassInitializationCommand : ATCommand() {
    override val tag: String = "BYPASS_INITIALIZATION"
    override val name: String = "OBD Bypass Initialization Sequence"
    override val pid: String = "BI"
}

public class ProtocolCloseCommand : ATCommand() {
    override val tag: String = "PROTOCOL_CLOSE"
    override val name: String = "OBD Protocol Close"
    override val pid: String = "PC"
}

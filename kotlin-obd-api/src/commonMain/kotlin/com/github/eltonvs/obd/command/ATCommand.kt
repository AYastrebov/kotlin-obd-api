package com.github.eltonvs.obd.command


public abstract class ATCommand : ObdCommand() {
    override val mode: String = "AT"
    override val skipDigitCheck: Boolean = true
}

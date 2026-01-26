package com.github.eltonvs.obd.command.at

import com.github.eltonvs.obd.command.AdaptiveTimingMode
import com.github.eltonvs.obd.command.ObdProtocols
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.Switcher
import kotlin.test.Test
import kotlin.test.assertEquals

// Action Commands Tests

class ResetAdapterCommandTests {
    @Test
    fun `test reset adapter command properties`() {
        val command = ResetAdapterCommand()
        assertEquals("RESET_ADAPTER", command.tag)
        assertEquals("Reset OBD Adapter", command.name)
        assertEquals("AT Z", command.rawCommand)
    }
}

class WarmStartCommandTests {
    @Test
    fun `test warm start command properties`() {
        val command = WarmStartCommand()
        assertEquals("WARM_START", command.tag)
        assertEquals("OBD Warm Start", command.name)
        assertEquals("AT WS", command.rawCommand)
    }
}

class SlowInitiationCommandTests {
    @Test
    fun `test slow initiation command properties`() {
        val command = SlowInitiationCommand()
        assertEquals("SLOW_INITIATION", command.tag)
        assertEquals("OBD Slow Initiation", command.name)
        assertEquals("AT SI", command.rawCommand)
    }
}

class LowPowerModeCommandTests {
    @Test
    fun `test low power mode command properties`() {
        val command = LowPowerModeCommand()
        assertEquals("LOW_POWER_MODE", command.tag)
        assertEquals("OBD Low Power Mode", command.name)
        assertEquals("AT LP", command.rawCommand)
    }
}

class BufferDumpCommandTests {
    @Test
    fun `test buffer dump command properties`() {
        val command = BufferDumpCommand()
        assertEquals("BUFFER_DUMP", command.tag)
        assertEquals("OBD Buffer Dump", command.name)
        assertEquals("AT BD", command.rawCommand)
    }
}

class BypassInitializationCommandTests {
    @Test
    fun `test bypass initialization command properties`() {
        val command = BypassInitializationCommand()
        assertEquals("BYPASS_INITIALIZATION", command.tag)
        assertEquals("OBD Bypass Initialization Sequence", command.name)
        assertEquals("AT BI", command.rawCommand)
    }
}

class ProtocolCloseCommandTests {
    @Test
    fun `test protocol close command properties`() {
        val command = ProtocolCloseCommand()
        assertEquals("PROTOCOL_CLOSE", command.tag)
        assertEquals("OBD Protocol Close", command.name)
        assertEquals("AT PC", command.rawCommand)
    }
}

// Info Commands Tests

class DescribeProtocolCommandTests {
    @Test
    fun `test describe protocol command properties`() {
        val command = DescribeProtocolCommand()
        assertEquals("DESCRIBE_PROTOCOL", command.tag)
        assertEquals("Describe Protocol", command.name)
        assertEquals("AT DP", command.rawCommand)
    }
}

class DescribeProtocolNumberCommandTests {
    @Test
    fun `test describe protocol number command properties`() {
        val command = DescribeProtocolNumberCommand()
        assertEquals("DESCRIBE_PROTOCOL_NUMBER", command.tag)
        assertEquals("Describe Protocol Number", command.name)
        assertEquals("AT DPN", command.rawCommand)
    }

    @Test
    fun `test describe protocol number handler`() {
        listOf(
            "0" to "Auto",
            "A0" to "Auto",
            "1" to "SAE J1850 PWM",
            "2" to "SAE J1850 VPW",
            "3" to "ISO 9141-2",
            "4" to "ISO 14230-4 (KWP 5BAUD)",
            "5" to "ISO 14230-4 (KWP FAST)",
            "6" to "ISO 15765-4 (CAN 11/500)",
            "7" to "ISO 15765-4 (CAN 29/500)",
            "8" to "ISO 15765-4 (CAN 11/250)",
            "9" to "ISO 15765-4 (CAN 29/250)",
            "A" to "SAE J1939 (CAN 29/250)",
            "X" to "Unknown Protocol"
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = DescribeProtocolNumberCommand().run {
                handleResponse(rawResponse)
            }
            assertEquals(expected, obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class IgnitionMonitorCommandTests {
    @Test
    fun `test ignition monitor command properties`() {
        val command = IgnitionMonitorCommand()
        assertEquals("IGNITION_MONITOR", command.tag)
        assertEquals("Ignition Monitor", command.name)
        assertEquals("AT IGN", command.rawCommand)
    }

    @Test
    fun `test ignition monitor handler`() {
        listOf(
            "ON" to "ON",
            "off" to "OFF",
            " On " to "ON"
        ).forEach { (rawValue, expected) ->
            val rawResponse = ObdRawResponse(value = rawValue, elapsedTime = 0)
            val obdResponse = IgnitionMonitorCommand().run {
                handleResponse(rawResponse)
            }
            assertEquals(expected, obdResponse.formattedValue, "Failed for: $rawValue")
        }
    }
}

class AdapterVoltageCommandTests {
    @Test
    fun `test adapter voltage command properties`() {
        val command = AdapterVoltageCommand()
        assertEquals("ADAPTER_VOLTAGE", command.tag)
        assertEquals("OBD Adapter Voltage", command.name)
        assertEquals("AT RV", command.rawCommand)
    }
}

// Mutation Commands Tests

class SelectProtocolCommandTests {
    @Test
    fun `test select protocol command for each protocol`() {
        listOf(
            ObdProtocols.AUTO to Pair("SELECT_PROTOCOL_AUTO", "AT SP 0"),
            ObdProtocols.SAE_J1850_PWM to Pair("SELECT_PROTOCOL_SAE_J1850_PWM", "AT SP 1"),
            ObdProtocols.SAE_J1850_VPW to Pair("SELECT_PROTOCOL_SAE_J1850_VPW", "AT SP 2"),
            ObdProtocols.ISO_9141_2 to Pair("SELECT_PROTOCOL_ISO_9141_2", "AT SP 3"),
            ObdProtocols.ISO_14230_4_KWP to Pair("SELECT_PROTOCOL_ISO_14230_4_KWP", "AT SP 4"),
            ObdProtocols.ISO_14230_4_KWP_FAST to Pair("SELECT_PROTOCOL_ISO_14230_4_KWP_FAST", "AT SP 5"),
            ObdProtocols.ISO_15765_4_CAN to Pair("SELECT_PROTOCOL_ISO_15765_4_CAN", "AT SP 6"),
            ObdProtocols.ISO_15765_4_CAN_B to Pair("SELECT_PROTOCOL_ISO_15765_4_CAN_B", "AT SP 7"),
            ObdProtocols.ISO_15765_4_CAN_C to Pair("SELECT_PROTOCOL_ISO_15765_4_CAN_C", "AT SP 8"),
            ObdProtocols.ISO_15765_4_CAN_D to Pair("SELECT_PROTOCOL_ISO_15765_4_CAN_D", "AT SP 9"),
            ObdProtocols.SAE_J1939_CAN to Pair("SELECT_PROTOCOL_SAE_J1939_CAN", "AT SP A"),
            ObdProtocols.UNKNOWN to Pair("SELECT_PROTOCOL_AUTO", "AT SP 0")
        ).forEach { (protocol, expected) ->
            val command = SelectProtocolCommand(protocol)
            assertEquals(expected.first, command.tag, "Failed tag for: $protocol")
            assertEquals(expected.second, command.rawCommand, "Failed rawCommand for: $protocol")
        }
    }
}

class SetAdaptiveTimingCommandTests {
    @Test
    fun `test set adaptive timing command for each mode`() {
        listOf(
            AdaptiveTimingMode.OFF to Pair("SET_ADAPTIVE_TIMING_OFF", "AT AT 0"),
            AdaptiveTimingMode.AUTO_1 to Pair("SET_ADAPTIVE_TIMING_AUTO_1", "AT AT 1"),
            AdaptiveTimingMode.AUTO_2 to Pair("SET_ADAPTIVE_TIMING_AUTO_2", "AT AT 2")
        ).forEach { (mode, expected) ->
            val command = SetAdaptiveTimingCommand(mode)
            assertEquals(expected.first, command.tag, "Failed tag for: $mode")
            assertEquals(expected.second, command.rawCommand, "Failed rawCommand for: $mode")
        }
    }
}

class SetEchoCommandTests {
    @Test
    fun `test set echo command for on and off`() {
        listOf(
            Switcher.ON to Pair("SET_ECHO_ON", "AT E1"),
            Switcher.OFF to Pair("SET_ECHO_OFF", "AT E0")
        ).forEach { (value, expected) ->
            val command = SetEchoCommand(value)
            assertEquals(expected.first, command.tag, "Failed tag for: $value")
            assertEquals(expected.second, command.rawCommand, "Failed rawCommand for: $value")
        }
    }
}

class SetHeadersCommandTests {
    @Test
    fun `test set headers command for on and off`() {
        listOf(
            Switcher.ON to Pair("SET_HEADERS_ON", "AT H1"),
            Switcher.OFF to Pair("SET_HEADERS_OFF", "AT H0")
        ).forEach { (value, expected) ->
            val command = SetHeadersCommand(value)
            assertEquals(expected.first, command.tag, "Failed tag for: $value")
            assertEquals(expected.second, command.rawCommand, "Failed rawCommand for: $value")
        }
    }
}

class SetLineFeedCommandTests {
    @Test
    fun `test set line feed command for on and off`() {
        listOf(
            Switcher.ON to Pair("SET_LINE_FEED_ON", "AT L1"),
            Switcher.OFF to Pair("SET_LINE_FEED_OFF", "AT L0")
        ).forEach { (value, expected) ->
            val command = SetLineFeedCommand(value)
            assertEquals(expected.first, command.tag, "Failed tag for: $value")
            assertEquals(expected.second, command.rawCommand, "Failed rawCommand for: $value")
        }
    }
}

class SetSpacesCommandTests {
    @Test
    fun `test set spaces command for on and off`() {
        listOf(
            Switcher.ON to Pair("SET_SPACES_ON", "AT S1"),
            Switcher.OFF to Pair("SET_SPACES_OFF", "AT S0")
        ).forEach { (value, expected) ->
            val command = SetSpacesCommand(value)
            assertEquals(expected.first, command.tag, "Failed tag for: $value")
            assertEquals(expected.second, command.rawCommand, "Failed rawCommand for: $value")
        }
    }
}

class SetTimeoutCommandTests {
    @Test
    fun `test set timeout command for various values`() {
        listOf(
            0 to "AT ST 00",
            1 to "AT ST 01",
            15 to "AT ST 0F",
            16 to "AT ST 10",
            255 to "AT ST FF",
            256 to "AT ST 00"
        ).forEach { (timeout, expectedRawCommand) ->
            val command = SetTimeoutCommand(timeout)
            assertEquals("SET_TIMEOUT", command.tag)
            assertEquals("Set Timeout - $timeout", command.name)
            assertEquals(expectedRawCommand, command.rawCommand, "Failed for timeout: $timeout")
        }
    }
}

class ToHexTests {
    @Test
    fun `test Int toHex extension function`() {
        listOf(
            0 to "00",
            1 to "01",
            10 to "0A",
            15 to "0F",
            16 to "10",
            255 to "FF",
            256 to "00"
        ).forEach { (value, expected) ->
            assertEquals(expected, value.toHex(), "Failed for value: $value")
        }
    }
}

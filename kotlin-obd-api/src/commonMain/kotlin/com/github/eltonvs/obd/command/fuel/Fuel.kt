package com.github.eltonvs.obd.command.fuel

import com.github.eltonvs.obd.command.*
import com.github.eltonvs.obd.command.formatToDecimalPlaces


public class FuelConsumptionRateCommand : ObdCommand() {
    override val tag: String = "FUEL_CONSUMPTION_RATE"
    override val name: String = "Fuel Consumption Rate"
    override val mode: String = "01"
    override val pid: String = "5E"

    override val defaultUnit: String = "L/h"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(bytesToInt(it.bufferedValue) * 0.05f, 1)
    }
}

public class FuelTypeCommand : ObdCommand() {
    override val tag: String = "FUEL_TYPE"
    override val name: String = "Fuel Type"
    override val mode: String = "01"
    override val pid: String = "51"

    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        getFuelType(bytesToInt(it.bufferedValue, bytesToProcess = 1).toInt())
    }

    private fun getFuelType(code: Int): String = when (code) {
        0x00 -> "Not Available"
        0x01 -> "Gasoline"
        0x02 -> "Methanol"
        0x03 -> "Ethanol"
        0x04 -> "Diesel"
        0x05 -> "GPL/LGP"
        0x06 -> "Natural Gas"
        0x07 -> "Propane"
        0x08 -> "Electric"
        0x09 -> "Biodiesel + Gasoline"
        0x0A -> "Biodiesel + Methanol"
        0x0B -> "Biodiesel + Ethanol"
        0x0C -> "Biodiesel + GPL/LGP"
        0x0D -> "Biodiesel + Natural Gas"
        0x0E -> "Biodiesel + Propane"
        0x0F -> "Biodiesel + Electric"
        0x10 -> "Biodiesel + Gasoline/Electric"
        0x11 -> "Hybrid Gasoline"
        0x12 -> "Hybrid Ethanol"
        0x13 -> "Hybrid Diesel"
        0x14 -> "Hybrid Electric"
        0x15 -> "Hybrid Mixed"
        0x16 -> "Hybrid Regenerative"
        else -> "Unknown"
    }
}

public class FuelLevelCommand : ObdCommand() {
    override val tag: String = "FUEL_LEVEL"
    override val name: String = "Fuel Level"
    override val mode: String = "01"
    override val pid: String = "2F"

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculatePercentage(it.bufferedValue, bytesToProcess = 1), 1)
    }
}

public class EthanolLevelCommand : ObdCommand() {
    override val tag: String = "ETHANOL_LEVEL"
    override val name: String = "Ethanol Level"
    override val mode: String = "01"
    override val pid: String = "52"

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(calculatePercentage(it.bufferedValue, bytesToProcess = 1), 1)
    }
}

public class FuelTrimCommand(fuelTrimBank: FuelTrimBank) : ObdCommand() {
    override val tag: String = fuelTrimBank.name
    override val name: String = fuelTrimBank.displayName
    override val mode: String = "01"
    override val pid: String = fuelTrimBank.pid

    override val defaultUnit: String = "%"
    override val handler: (ObdRawResponse) -> String = { it: ObdRawResponse ->
        formatToDecimalPlaces(bytesToInt(it.bufferedValue, bytesToProcess = 1) * (100f / 128) - 100, 1)
    }

    public enum class FuelTrimBank(public val displayName: String, internal val pid: String) {
        SHORT_TERM_BANK_1("Short Term Fuel Trim Bank 1", "06"),
        SHORT_TERM_BANK_2("Short Term Fuel Trim Bank 2", "07"),
        LONG_TERM_BANK_1("Long Term Fuel Trim Bank 1", "08"),
        LONG_TERM_BANK_2("Long Term Fuel Trim Bank 2", "09"),
    }
}

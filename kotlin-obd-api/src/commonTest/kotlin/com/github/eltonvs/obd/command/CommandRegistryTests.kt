package com.github.eltonvs.obd.command

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommandRegistryTests {

    @Test
    fun `register and get command`() = withTemporaryRegistry {
        CommandRegistry.register("TEST_CMD", CommandCategory.ENGINE, "01", "0D") {
            TestCommand()
        }

        val command = CommandRegistry.get("TEST_CMD")
        assertNotNull(command)
        assertEquals("TEST_CMD", command.tag)
    }

    @Test
    fun `get returns null for unknown tag`() = withTemporaryRegistry {
        assertNull(CommandRegistry.get("UNKNOWN"))
    }

    @Test
    fun `register command instance`() = withTemporaryRegistry {
        val command = TestCommand()
        CommandRegistry.register(command)

        val retrieved = CommandRegistry.get("TEST_CMD")
        assertNotNull(retrieved)
        assertEquals("TEST_CMD", retrieved.tag)
    }

    @Test
    fun `unregister removes command`() = withTemporaryRegistry {
        CommandRegistry.register("TEST_CMD", CommandCategory.ENGINE) { TestCommand() }
        assertTrue(CommandRegistry.contains("TEST_CMD"))

        val removed = CommandRegistry.unregister("TEST_CMD")
        assertTrue(removed)
        assertFalse(CommandRegistry.contains("TEST_CMD"))
    }

    @Test
    fun `unregister returns false for unknown`() = withTemporaryRegistry {
        assertFalse(CommandRegistry.unregister("UNKNOWN"))
    }

    @Test
    fun `getByCategory returns matching commands`() = withTemporaryRegistry {
        CommandRegistry.register("ENGINE_1", CommandCategory.ENGINE) { TestCommand() }
        CommandRegistry.register("ENGINE_2", CommandCategory.ENGINE) { TestCommand() }
        CommandRegistry.register("TEMP_1", CommandCategory.TEMPERATURE) { TestCommand() }

        val engineCommands = CommandRegistry.getByCategory(CommandCategory.ENGINE)
        assertEquals(2, engineCommands.size)
    }

    @Test
    fun `getByCategory returns empty for no matches`() = withTemporaryRegistry {
        CommandRegistry.register("ENGINE_1", CommandCategory.ENGINE) { TestCommand() }

        val pressureCommands = CommandRegistry.getByCategory(CommandCategory.PRESSURE)
        assertTrue(pressureCommands.isEmpty())
    }

    @Test
    fun `findByPid returns matching commands`() = withTemporaryRegistry {
        CommandRegistry.register("SPEED", CommandCategory.ENGINE, "01", "0D") { TestCommand() }
        CommandRegistry.register("RPM", CommandCategory.ENGINE, "01", "0C") { TestCommand() }

        val speedCommands = CommandRegistry.findByPid("01", "0D")
        assertEquals(1, speedCommands.size)
        assertEquals("TEST_CMD", speedCommands[0].tag)
    }

    @Test
    fun `findByMode returns all commands for mode`() = withTemporaryRegistry {
        CommandRegistry.register("CMD_1", CommandCategory.ENGINE, "01", "0D") { TestCommand() }
        CommandRegistry.register("CMD_2", CommandCategory.ENGINE, "01", "0C") { TestCommand() }
        CommandRegistry.register("CMD_3", CommandCategory.ENGINE, "22", "1234") { TestCommand() }

        val mode01Commands = CommandRegistry.findByMode("01")
        assertEquals(2, mode01Commands.size)
    }

    @Test
    fun `getAllTags returns all registered tags`() = withTemporaryRegistry {
        CommandRegistry.register("TAG_A", CommandCategory.ENGINE) { TestCommand() }
        CommandRegistry.register("TAG_B", CommandCategory.FUEL) { TestCommand() }

        val tags = CommandRegistry.getAllTags()
        assertEquals(setOf("TAG_A", "TAG_B"), tags)
    }

    @Test
    fun `getRegisteredCategories returns unique categories`() = withTemporaryRegistry {
        CommandRegistry.register("A", CommandCategory.ENGINE) { TestCommand() }
        CommandRegistry.register("B", CommandCategory.ENGINE) { TestCommand() }
        CommandRegistry.register("C", CommandCategory.TEMPERATURE) { TestCommand() }

        val categories = CommandRegistry.getRegisteredCategories()
        assertEquals(setOf(CommandCategory.ENGINE, CommandCategory.TEMPERATURE), categories)
    }

    @Test
    fun `contains returns true for registered`() = withTemporaryRegistry {
        CommandRegistry.register("EXISTS", CommandCategory.ENGINE) { TestCommand() }

        assertTrue(CommandRegistry.contains("EXISTS"))
        assertFalse(CommandRegistry.contains("NOT_EXISTS"))
    }

    @Test
    fun `size returns count`() = withTemporaryRegistry {
        assertEquals(0, CommandRegistry.size())

        CommandRegistry.register("A", CommandCategory.ENGINE) { TestCommand() }
        CommandRegistry.register("B", CommandCategory.FUEL) { TestCommand() }

        assertEquals(2, CommandRegistry.size())
    }

    @Test
    fun `clear removes all commands`() = withTemporaryRegistry {
        CommandRegistry.register("A", CommandCategory.ENGINE) { TestCommand() }
        CommandRegistry.register("B", CommandCategory.FUEL) { TestCommand() }

        CommandRegistry.clear()

        assertEquals(0, CommandRegistry.size())
    }

    @Test
    fun `factory creates new instances`() = withTemporaryRegistry {
        var instanceCount = 0
        CommandRegistry.register("COUNTER", CommandCategory.ENGINE) {
            instanceCount++
            TestCommand()
        }

        CommandRegistry.get("COUNTER")
        CommandRegistry.get("COUNTER")

        assertEquals(2, instanceCount)
    }

    @Test
    fun `registerCommands DSL`() = withTemporaryRegistry {
        registerCommands {
            command("CMD_A", CommandCategory.ENGINE, "01", "0A") { TestCommand() }
            command("CMD_B", CommandCategory.TEMPERATURE, "01", "05") { TestCommand() }
        }

        assertEquals(2, CommandRegistry.size())
        assertTrue(CommandRegistry.contains("CMD_A"))
        assertTrue(CommandRegistry.contains("CMD_B"))
    }

    @Test
    fun `withTemporaryRegistry restores state`() {
        val initialSize = CommandRegistry.size()
        val initialTags = CommandRegistry.getAllTags()

        withTemporaryRegistry {
            CommandRegistry.register("TEMP_TAG", CommandCategory.CUSTOM) { TestCommand() }
            assertTrue(CommandRegistry.contains("TEMP_TAG"))
        }

        assertEquals(initialSize, CommandRegistry.size())
        assertEquals(initialTags, CommandRegistry.getAllTags())
        assertFalse(CommandRegistry.contains("TEMP_TAG"))
    }

    private class TestCommand : ObdCommand() {
        override val tag = "TEST_CMD"
        override val name = "Test Command"
        override val mode = "01"
        override val pid = "0D"
        override fun parseTypedValue(rawResponse: ObdRawResponse): TypedValue<*> =
            TypedValue.StringValue(rawResponse.value)
    }
}

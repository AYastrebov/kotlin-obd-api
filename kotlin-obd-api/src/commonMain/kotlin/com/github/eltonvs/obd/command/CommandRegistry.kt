package com.github.eltonvs.obd.command

/**
 * Registry for discovering and managing OBD commands.
 *
 * Provides command discovery by tag, category, and PID, along with
 * registration of custom commands. Commands are stored as factories
 * to allow creating fresh instances on each retrieval.
 *
 * Example usage:
 * ```kotlin
 * // Register a custom command with explicit metadata
 * CommandRegistry.register("CUSTOM_BOOST", CommandCategory.PRESSURE, "01", "0A") {
 *     CustomBoostCommand()
 * }
 *
 * // Register using command instance (extracts metadata automatically)
 * CommandRegistry.register(MySpeedCommand()) { MySpeedCommand() }
 *
 * // Get command by tag
 * val command = CommandRegistry.get("CUSTOM_BOOST")
 *
 * // Get all commands in a category
 * val engineCommands = CommandRegistry.getByCategory(CommandCategory.ENGINE)
 *
 * // Find commands by PID
 * val speedCommands = CommandRegistry.findByPid("01", "0D")
 * ```
 *
 * @see CommandCategory
 * @see registerCommands
 * @see withTemporaryRegistry
 */
public object CommandRegistry {
    private val commandFactories = mutableMapOf<String, CommandEntry>()

    /**
     * Internal entry containing command metadata and factory function.
     */
    private data class CommandEntry(
        val tag: String,
        val category: CommandCategory,
        val mode: String?,
        val pid: String?,
        val factory: () -> ObdCommand
    )

    /**
     * Registers a command factory with explicit metadata.
     *
     * Use this method when you want to specify all metadata explicitly,
     * or when registering commands that don't extend [TypedObdCommand].
     *
     * @param tag Unique identifier for the command (used for lookups)
     * @param category Category for grouping commands
     * @param mode Optional OBD mode for PID-based lookup (e.g., "01", "22")
     * @param pid Optional PID for PID-based lookup (e.g., "0D", "1234")
     * @param factory Factory function that creates new command instances
     */
    public fun register(
        tag: String,
        category: CommandCategory,
        mode: String? = null,
        pid: String? = null,
        factory: () -> ObdCommand
    ) {
        commandFactories[tag] = CommandEntry(
            tag = tag,
            category = category,
            mode = mode,
            pid = pid,
            factory = factory
        )
    }

    /**
     * Registers a command using an instance to extract metadata.
     *
     * This method automatically extracts tag, mode, pid, and category
     * from the provided command instance.
     *
     * @param command Command instance used as a template for metadata
     * @param factory Optional factory function; if not provided, the same instance is returned
     *                (which may cause issues if the command has mutable state)
     */
    public fun register(command: ObdCommand, factory: (() -> ObdCommand)? = null) {
        val actualFactory = factory ?: { command }
        register(
            tag = command.tag,
            category = command.category,
            mode = command.mode,
            pid = command.pid,
            factory = actualFactory
        )
    }

    /**
     * Unregisters a command by its tag.
     *
     * @param tag The tag of the command to remove
     * @return true if a command was removed, false if no command with that tag existed
     */
    public fun unregister(tag: String): Boolean {
        return commandFactories.remove(tag) != null
    }

    /**
     * Gets a command by its unique tag.
     *
     * Each call creates a new instance using the registered factory.
     *
     * @param tag The unique identifier of the command
     * @return A new instance of the command, or null if no command with that tag is registered
     */
    public fun get(tag: String): ObdCommand? {
        return commandFactories[tag]?.factory?.invoke()
    }

    /**
     * Gets all commands in a specific category.
     *
     * Each call creates new instances using the registered factories.
     *
     * @param category The category to filter by
     * @return List of new command instances in the specified category
     */
    public fun getByCategory(category: CommandCategory): List<ObdCommand> {
        return commandFactories.values
            .filter { it.category == category }
            .map { it.factory() }
    }

    /**
     * Finds commands by OBD mode and PID.
     *
     * This is useful for discovering which commands handle a specific PID.
     * Multiple commands may be registered for the same mode/PID combination.
     *
     * @param mode The OBD mode (e.g., "01" for current data, "22" for manufacturer-specific)
     * @param pid The Parameter ID (e.g., "0D" for speed, "0C" for RPM)
     * @return List of new command instances matching the mode/PID combination
     */
    public fun findByPid(mode: String, pid: String): List<ObdCommand> {
        return commandFactories.values
            .filter { it.mode == mode && it.pid == pid }
            .map { it.factory() }
    }

    /**
     * Finds all commands for a specific OBD mode.
     *
     * @param mode The OBD mode (e.g., "01", "02", "09", "22")
     * @return List of new command instances for the specified mode
     */
    public fun findByMode(mode: String): List<ObdCommand> {
        return commandFactories.values
            .filter { it.mode == mode }
            .map { it.factory() }
    }

    /**
     * Gets all registered command tags.
     *
     * @return Set of all registered tags
     */
    public fun getAllTags(): Set<String> {
        return commandFactories.keys.toSet()
    }

    /**
     * Gets all categories that have at least one registered command.
     *
     * @return Set of categories with registered commands
     */
    public fun getRegisteredCategories(): Set<CommandCategory> {
        return commandFactories.values.map { it.category }.toSet()
    }

    /**
     * Checks if a command with the given tag is registered.
     *
     * @param tag The tag to check
     * @return true if a command with that tag is registered
     */
    public fun contains(tag: String): Boolean {
        return tag in commandFactories
    }

    /**
     * Gets the total number of registered commands.
     *
     * @return Number of commands in the registry
     */
    public fun size(): Int = commandFactories.size

    /**
     * Clears all registered commands from the registry.
     *
     * Use with caution in production code. Primarily useful for testing.
     */
    public fun clear() {
        commandFactories.clear()
    }

    /**
     * Creates a snapshot of the current registry state.
     *
     * Use this with [restore] to save and restore registry state,
     * particularly useful in testing scenarios.
     *
     * @return An opaque snapshot object that can be passed to [restore]
     * @see restore
     * @see withTemporaryRegistry
     */
    public fun snapshot(): RegistrySnapshot {
        return RegistrySnapshot(commandFactories.toMap())
    }

    /**
     * Restores registry state from a previously captured snapshot.
     *
     * All current registrations are replaced with those from the snapshot.
     *
     * @param snapshot Previously captured snapshot from [snapshot]
     * @see snapshot
     * @see withTemporaryRegistry
     */
    public fun restore(snapshot: RegistrySnapshot) {
        commandFactories.clear()
        @Suppress("UNCHECKED_CAST")
        commandFactories.putAll(snapshot.data as Map<String, CommandEntry>)
    }

    /**
     * Opaque snapshot of registry state for save/restore operations.
     *
     * This class is intentionally opaque to prevent external manipulation
     * of registry internals.
     *
     * @see CommandRegistry.snapshot
     * @see CommandRegistry.restore
     */
    public class RegistrySnapshot internal constructor(internal val data: Any)
}

/**
 * DSL builder for bulk registration of commands.
 *
 * Use this with [registerCommands] to register multiple commands
 * in a clean, declarative style.
 *
 * Example:
 * ```kotlin
 * registerCommands {
 *     command("SPEED", CommandCategory.ENGINE, "01", "0D") { SpeedCommand() }
 *     command("RPM", CommandCategory.ENGINE, "01", "0C") { RPMCommand() }
 *     command("COOLANT_TEMP", CommandCategory.TEMPERATURE, "01", "05") { CoolantTempCommand() }
 * }
 * ```
 *
 * @see registerCommands
 */
public class CommandRegistrationBuilder {
    internal val registrations = mutableListOf<() -> Unit>()

    /**
     * Registers a command with explicit metadata.
     *
     * @param tag Unique identifier for the command
     * @param category Category for grouping
     * @param mode Optional OBD mode for PID-based lookup
     * @param pid Optional PID for PID-based lookup
     * @param factory Factory function that creates command instances
     */
    public fun command(
        tag: String,
        category: CommandCategory,
        mode: String? = null,
        pid: String? = null,
        factory: () -> ObdCommand
    ) {
        registrations.add {
            CommandRegistry.register(tag, category, mode, pid, factory)
        }
    }

    /**
     * Registers a command using an instance for metadata extraction.
     *
     * @param command Command instance used as template
     * @param factory Optional factory function for creating instances
     */
    public fun command(command: ObdCommand, factory: (() -> ObdCommand)? = null) {
        registrations.add {
            CommandRegistry.register(command, factory)
        }
    }
}

/**
 * Bulk registers commands using DSL syntax.
 *
 * This function provides a clean way to register multiple commands at once.
 *
 * Example:
 * ```kotlin
 * registerCommands {
 *     command("SPEED", CommandCategory.ENGINE, "01", "0D") { SpeedCommand() }
 *     command("RPM", CommandCategory.ENGINE, "01", "0C") { RPMCommand() }
 *     command(MyCoolantTempCommand()) { MyCoolantTempCommand() }
 * }
 * ```
 *
 * @param block Builder block for registering commands
 * @see CommandRegistrationBuilder
 */
public fun registerCommands(block: CommandRegistrationBuilder.() -> Unit) {
    val builder = CommandRegistrationBuilder()
    builder.block()
    builder.registrations.forEach { it() }
}

/**
 * Executes a block with a temporary registry state.
 *
 * The current registry state is saved before executing the block and
 * automatically restored after the block completes (even if an exception is thrown).
 *
 * This is particularly useful for testing scenarios where you need to
 * register test commands without affecting other tests.
 *
 * Example:
 * ```kotlin
 * @Test
 * fun testMyCommand() = withTemporaryRegistry {
 *     // Register test commands
 *     CommandRegistry.register("TEST", CommandCategory.CUSTOM) { TestCommand() }
 *
 *     // Run tests
 *     val cmd = CommandRegistry.get("TEST")
 *     assertNotNull(cmd)
 * } // Original registry state is restored here
 * ```
 *
 * @param T The return type of the block
 * @param block The block to execute with temporary registry state
 * @return The result of executing the block
 */
public fun <T> withTemporaryRegistry(block: () -> T): T {
    val snapshot = CommandRegistry.snapshot()
    return try {
        block()
    } finally {
        CommandRegistry.restore(snapshot)
    }
}

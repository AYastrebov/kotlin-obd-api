package com.github.eltonvs.obd.command

/**
 * Registry for discovering and managing OBD commands.
 *
 * Provides command discovery by tag, category, and PID, along with
 * registration of custom commands.
 *
 * Example usage:
 * ```kotlin
 * // Register a custom command
 * CommandRegistry.register("CUSTOM_BOOST", CommandCategory.PRESSURE) {
 *     CustomBoostCommand()
 * }
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
 */
public object CommandRegistry {
    private val commandFactories = mutableMapOf<String, CommandEntry>()

    /**
     * Entry containing command metadata and factory
     */
    private data class CommandEntry(
        val tag: String,
        val category: CommandCategory,
        val mode: String?,
        val pid: String?,
        val factory: () -> ObdCommand
    )

    /**
     * Register a command factory with the registry.
     *
     * @param tag Unique identifier for the command
     * @param category Category for grouping
     * @param mode Optional OBD mode (for PID-based lookup)
     * @param pid Optional PID (for PID-based lookup)
     * @param factory Factory function that creates command instances
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
     * Register a command instance (extracts metadata automatically).
     *
     * @param command Command to register (used as a template)
     * @param factory Optional factory function; if not provided, a new instance is created via reflection-like behavior
     */
    public fun register(command: ObdCommand, factory: (() -> ObdCommand)? = null) {
        val category = when (command) {
            is TypedObdCommand<*> -> command.category
            else -> CommandCategory.UNKNOWN
        }
        val actualFactory = factory ?: { command }
        register(
            tag = command.tag,
            category = category,
            mode = command.mode,
            pid = command.pid,
            factory = actualFactory
        )
    }

    /**
     * Unregister a command by its tag.
     *
     * @param tag The tag of the command to remove
     * @return true if a command was removed, false if no command with that tag existed
     */
    public fun unregister(tag: String): Boolean {
        return commandFactories.remove(tag) != null
    }

    /**
     * Get a command by its tag.
     *
     * @param tag The unique identifier of the command
     * @return A new instance of the command, or null if not found
     */
    public fun get(tag: String): ObdCommand? {
        return commandFactories[tag]?.factory?.invoke()
    }

    /**
     * Get all commands in a specific category.
     *
     * @param category The category to filter by
     * @return List of new command instances in the category
     */
    public fun getByCategory(category: CommandCategory): List<ObdCommand> {
        return commandFactories.values
            .filter { it.category == category }
            .map { it.factory() }
    }

    /**
     * Find commands by mode and PID.
     *
     * @param mode The OBD mode (e.g., "01", "22")
     * @param pid The Parameter ID
     * @return List of new command instances matching the mode/PID
     */
    public fun findByPid(mode: String, pid: String): List<ObdCommand> {
        return commandFactories.values
            .filter { it.mode == mode && it.pid == pid }
            .map { it.factory() }
    }

    /**
     * Find commands by mode only.
     *
     * @param mode The OBD mode (e.g., "01", "22")
     * @return List of new command instances for the mode
     */
    public fun findByMode(mode: String): List<ObdCommand> {
        return commandFactories.values
            .filter { it.mode == mode }
            .map { it.factory() }
    }

    /**
     * Get all registered command tags.
     *
     * @return Set of all registered tags
     */
    public fun getAllTags(): Set<String> {
        return commandFactories.keys.toSet()
    }

    /**
     * Get all registered categories that have at least one command.
     *
     * @return Set of categories with registered commands
     */
    public fun getRegisteredCategories(): Set<CommandCategory> {
        return commandFactories.values.map { it.category }.toSet()
    }

    /**
     * Check if a command with the given tag is registered.
     *
     * @param tag The tag to check
     * @return true if a command with that tag is registered
     */
    public fun contains(tag: String): Boolean {
        return tag in commandFactories
    }

    /**
     * Get the total number of registered commands.
     */
    public fun size(): Int = commandFactories.size

    /**
     * Clear all registered commands.
     */
    public fun clear() {
        commandFactories.clear()
    }

    /**
     * Create a snapshot of the current registry state.
     * Useful for testing or temporary modifications.
     *
     * @return A map of tag to CommandEntry
     */
    internal fun snapshot(): Map<String, CommandEntry> {
        return commandFactories.toMap()
    }

    /**
     * Restore registry state from a snapshot.
     *
     * @param snapshot Previously captured snapshot
     */
    internal fun restore(snapshot: Map<String, CommandEntry>) {
        commandFactories.clear()
        commandFactories.putAll(snapshot)
    }
}

/**
 * DSL for bulk registration of commands.
 *
 * Example:
 * ```kotlin
 * registerCommands {
 *     command("SPEED", CommandCategory.ENGINE, "01", "0D") { SpeedCommand() }
 *     command("RPM", CommandCategory.ENGINE, "01", "0C") { RPMCommand() }
 *     command("COOLANT_TEMP", CommandCategory.TEMPERATURE, "01", "05") { EngineCoolantTemperatureCommand() }
 * }
 * ```
 */
public class CommandRegistrationBuilder {
    internal val registrations = mutableListOf<() -> Unit>()

    /**
     * Register a command with full metadata.
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
     * Register a command instance.
     */
    public fun command(command: ObdCommand, factory: (() -> ObdCommand)? = null) {
        registrations.add {
            CommandRegistry.register(command, factory)
        }
    }
}

/**
 * Bulk register commands using DSL syntax.
 */
public fun registerCommands(block: CommandRegistrationBuilder.() -> Unit) {
    val builder = CommandRegistrationBuilder()
    builder.block()
    builder.registrations.forEach { it() }
}

/**
 * Execute a block with a temporary registry state.
 * Restores the original state after the block completes.
 *
 * Useful for testing:
 * ```kotlin
 * withTemporaryRegistry {
 *     CommandRegistry.register("TEST", CommandCategory.CUSTOM) { TestCommand() }
 *     // test code here
 * } // original registry state restored
 * ```
 */
public inline fun <T> withTemporaryRegistry(block: () -> T): T {
    val snapshot = CommandRegistry.snapshot()
    return try {
        block()
    } finally {
        CommandRegistry.restore(snapshot)
    }
}

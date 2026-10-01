# Maintenance contract

Target Minecraft/Paper 1.21.11+ and Java 21 bytecode. Prioritize Paper/Purpur and preserve existing Folia/Leaf scheduling boundaries.

Keep player UUID paths, display/waila preference semantics, commands, placeholders, handlers and all item/data identities. Refuse unreadable player.yml before starting HUD tasks. Unchanged shutdown must not rewrite an existing settings file. Failed writes remain pending for retry.

Preserve unknown saved keys and use the original YAML format. Serialize fully before staged replacement; retain file symlinks and existing POSIX modes. Do not claim atomic replacement protects against all power loss or concurrent external writers. Run the full project tests, supported-version compilation and real startup/restart checks. Test-only libraries must not enter the plugin JAR.

Use scoped branches, preserve concurrent work, and record exact tested commits and remaining limitations. Do not publish a stable release or bump versions before coordinated validation. Produce raw installable JARs, with the aggregate bundle managed in Slimefun-Legacy.

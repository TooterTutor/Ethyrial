package io.github.tootertutor.ethyrial.registry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.commands.EthyrialCommand; // your dispatcher
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterCommand;
import io.github.tootertutor.ethyrial.interfaces.CommandMeta;
import io.github.tootertutor.ethyrial.interfaces.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Mirrors ItemRegister but for Subcommands.
 * Scans a package for classes implementing Subcommand + AutoRegisterCommand,
 * instantiates them (prefers (Ethyrial) constructor), and registers with the
 * dispatcher.
 */
public class CommandRegister {

    private final Ethyrial plugin;
    private final EthyrialCommand dispatcher;

    // Keep local maps for diagnostics/quick lookup (not strictly required to
    // function)
    private final Map<String, Subcommand> byName = new HashMap<>();
    private final Map<String, Subcommand> byAlias = new HashMap<>();

    // Package to scan (adjust if your commands live elsewhere)
    private final String packageName;

    public CommandRegister(Ethyrial plugin, EthyrialCommand dispatcher) {
        this(plugin, dispatcher, "io.github.tootertutor.ethyrial.commands");
    }

    public CommandRegister(Ethyrial plugin, EthyrialCommand dispatcher, String packageName) {
        this.plugin = plugin;
        this.dispatcher = dispatcher;
        this.packageName = packageName;
    }

    /** Entry point: scan, instantiate, and register all commands. */
    public void autoRegisterCommands() {
        ClassLoader cl = plugin.getClass().getClassLoader();
        String path = packageName.replace('.', '/');

        try {
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("jar".equals(resource.getProtocol())) {
                    processJar(resource, packageName);
                } else {
                    processDirectory(new File(resource.toURI()), packageName);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to scan for commands: " + e.getMessage());
        }
    }

    private void processJar(URL jarUrl, String basePackage) throws IOException {
        JarURLConnection jarConn = (JarURLConnection) jarUrl.openConnection();
        try (JarFile jar = jarConn.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class"))
                    continue;
                if (!name.startsWith(basePackage.replace('.', '/')))
                    continue;
                if (name.contains("$"))
                    continue; // skip inner classes

                String className = name.substring(0, name.length() - 6).replace('/', '.');
                loadAndMaybeRegister(className);
            }
        }
    }

    private void processDirectory(File directory, String basePackage) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file, basePackage + "." + file.getName());
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = basePackage + '.' + file.getName().replace(".class", "");
                loadAndMaybeRegister(className);
            }
        }
    }

    private void loadAndMaybeRegister(String className) {
        try {
            Class<?> clazz = Class.forName(className, false, plugin.getClass().getClassLoader());
            if (!Subcommand.class.isAssignableFrom(clazz))
                return;
            if (!AutoRegisterCommand.class.isAssignableFrom(clazz))
                return;

            @SuppressWarnings("unchecked")
            Class<? extends Subcommand> subClazz = (Class<? extends Subcommand>) clazz;
            registerCommand(subClazz);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Class not found: " + className);
        }
    }

    /** Instantiate and wire a single Subcommand class. */
    public void registerCommand(Class<? extends Subcommand> subClass) {
        try {
            Subcommand sub = instantiate(subClass);
            if (sub == null)
                return;

            // Resolve canonical name, usage, description, aliases via @CommandMeta (if
            // present)
            CommandMeta meta = subClass.getAnnotation(CommandMeta.class);
            String canonical = (meta != null && !meta.name().isBlank()) ? meta.name() : sub.getName();
            if (canonical == null || canonical.isBlank()) {
                warn("Skipping " + subClass.getName() + " — no name provided.");
                return;
            }

            // Register with dispatcher (once per instance)
            dispatcher.register(sub);

            // Wire usage (meta.usage overrides sub.getUsage)
            String usage = (meta != null && !meta.usage().isBlank()) ? meta.usage() : safe(sub.getUsage());
            if (!usage.isBlank()) {
                dispatcher.usage(canonical.toLowerCase(Locale.ROOT), usage);
            }

            // Description (optional—your dispatcher may show it from sub.getDescription())
            // Aliases: merge meta + sub.getAliases()
            Set<String> aliases = new LinkedHashSet<>();
            if (meta != null)
                aliases.addAll(Arrays.asList(meta.aliases()));
            if (sub.getAliases() != null)
                aliases.addAll(sub.getAliases());

            // Track for lookups; also add usage override for each alias
            Subcommand previous = byName.putIfAbsent(canonical.toLowerCase(Locale.ROOT), sub);
            if (previous != null && previous != sub) {
                warn("Name collision for '" + canonical + "' between "
                        + previous.getClass().getSimpleName() + " and " + subClass.getSimpleName());
            }
            for (String alias : aliases) {
                if (alias == null || alias.isBlank())
                    continue;
                String key = alias.toLowerCase(Locale.ROOT);
                Subcommand p = byAlias.putIfAbsent(key, sub);
                if (p != null && p != sub) {
                    warn("Alias '" + alias + "' collision between "
                            + p.getClass().getSimpleName() + " and " + subClass.getSimpleName());
                }
                if (!usage.isBlank()) {
                    dispatcher.usage(key, usage);
                }
            }

        } catch (Exception exception) {
            plugin.getLogger().severe("Failed to register command: " + subClass.getName());
            exception.printStackTrace();
        }
    }

    private Subcommand instantiate(Class<? extends Subcommand> subClass) {
        try {
            // Prefer (Ethyrial) ctor
            for (Constructor<?> constructor : subClass.getDeclaredConstructors()) {
                Class<?>[] pt = constructor.getParameterTypes();
                if (pt.length == 1 && Ethyrial.class.isAssignableFrom(pt[0])) {
                    constructor.setAccessible(true);
                    return (Subcommand) constructor.newInstance(plugin);
                }
            }
            // Fallback: no-arg
            Constructor<? extends Subcommand> constructor = subClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            warn("Could not construct " + subClass.getName() + ": " + exception.getClass().getSimpleName() + " "
                    + exception.getMessage());
            return null;
        }
    }

    private static String safe(String safety) {
        return (safety == null) ? "" : safety;
    }

    private void warn(String message) {
        plugin.getComponentLogger().warn(Component.text("[CommandRegister] " + message, NamedTextColor.YELLOW));
    }

    // --- Optional helpers (if you want quick lookups similar to ItemRegister) ---

    public Subcommand get(String nameOrAlias) {
        String name = nameOrAlias.toLowerCase(Locale.ROOT);
        Subcommand subcommand = byName.get(name);
        if (subcommand != null)
            return subcommand;
        return byAlias.get(name);
    }

    public Collection<Subcommand> all() {
        return new LinkedHashSet<>(byName.values()); // unique
    }

    /** For diagnostics/telemetry. */
    public int size() {
        return new LinkedHashSet<>(byName.values()).size();
    }
}

package io.github.tootertutor.ethyrial.registry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.tootertutor.ethyrial.Ethyrial;
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterSpell;
import io.github.tootertutor.ethyrial.spells.Spell;

public class SpellRegister implements Registry<Keyed> {
    protected Ethyrial plugin = Ethyrial.getInstance();
    private final Map<NamespacedKey, Spell> spellMap = new HashMap<>(); // Map to store spells by NamespacedKey

    public SpellRegister(Ethyrial plugin) {
        this.plugin = plugin;
    }

    public void autoRegisterSpells() {
        // Get the plugin's class loader
        ClassLoader classLoader = plugin.getClass().getClassLoader();

        // Define your spell package
        String packageName = "io.github.tootertutor.ethyrial.spells";

        // Convert package name to path
        String path = packageName.replace('.', '/');

        try {
            // Get all class files in the package
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("jar")) {
                    processJar(resource, packageName);
                } else {
                    processDirectory(new File(resource.toURI()), packageName);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to scan for spells: " + e.getMessage());
        }
    }

    private void processJar(URL jarUrl, String packageName) throws IOException {
        JarURLConnection jarConn = (JarURLConnection) jarUrl.openConnection();
        try (JarFile jar = jarConn.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(packageName.replace('.', '/')) && name.endsWith(".class")) {
                    loadClass(name.replace('/', '.').substring(0, name.length() - 6));
                }
            }
        }
    }

    private void processDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                loadClass(packageName + '.' + file.getName().replace(".class", ""));
            }
        }
    }

    private void loadClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (AutoRegisterSpell.class.isAssignableFrom(clazz) &&
                    Spell.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                Class<? extends Spell> spellClass = (Class<? extends Spell>) clazz;
                registerSpell(spellClass);
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Class not found: " + className);
        }
    }

    public void registerSpell(NamespacedKey key, Spell spell) {
        spellMap.put(key, spell);
        SpellRegistry.register(spell);

        if (spell instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) spell, plugin);
        }
    }

    public void registerSpell(Class<? extends Spell> spellClass) {
        try {
            // Get the declared constructor (even if it's not public)
            Constructor<? extends Spell> constructor = spellClass.getDeclaredConstructor(Ethyrial.class);

            // Make it accessible
            constructor.setAccessible(true);

            // Create instance with the actual plugin reference
            Spell spell = constructor.newInstance(Ethyrial.getInstance());

            spellMap.put(spell.getKey(), spell);
            SpellRegistry.register(spell);

            if (spell instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) spell, plugin);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register spell: " + spellClass.getName());
            e.printStackTrace();
        }
    }

    public Spell getSpell(NamespacedKey key) {
        return spellMap.get(key);
    }

    public List<Spell> getSpells() {
        return new ArrayList<>(spellMap.values());
    }

    @Override
    public @Nullable Keyed get(@NotNull NamespacedKey key) {
        return getSpell(key);
    }

    // @Override
    public @NotNull Keyed getOrThrow(@NotNull NamespacedKey key) {
        Spell spell = getSpell(key);
        if (spell == null) {
            throw new IllegalArgumentException("Spell not found for key: " + key);
        }
        return spell;
    }

    @Override
    public @NotNull Stream<Keyed> stream() {
        return spellMap.values().stream().map(spell -> (Keyed) spell);
    }

    @Override
    public Iterator<Keyed> iterator() {
        return spellMap.values().stream().map(spell -> (Keyed) spell).iterator();
    }
}

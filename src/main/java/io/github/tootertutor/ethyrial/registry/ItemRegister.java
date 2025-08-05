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
import io.github.tootertutor.ethyrial.interfaces.AutoRegisterItem;
import io.github.tootertutor.ethyrial.items.Item;

public class ItemRegister implements Registry<Keyed> {
    protected final Ethyrial plugin;
    private final Map<NamespacedKey, Item> itemMap = new HashMap<>(); // Map to store items by NamespacedKey

    public ItemRegister(Ethyrial plugin) {
        this.plugin = plugin;
    }

    public void autoRegisterItems() {
        // Get the plugin's class loader
        ClassLoader classLoader = plugin.getClass().getClassLoader();

        // Define your item package
        String packageName = "io.github.tootertutor.ethyrial.items";

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
            plugin.getLogger().severe("Failed to scan for items: " + e.getMessage());
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
            if (AutoRegisterItem.class.isAssignableFrom(clazz) &&
                    Item.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                Class<? extends Item> itemClass = (Class<? extends Item>) clazz;
                registerItem(itemClass);
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Class not found: " + className);
        }
    }

    public void registerItem(NamespacedKey key, Item item) {
        itemMap.put(key, item);

        if (item instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) item, plugin);
        }
    }

    public void registerItem(Class<? extends Item> itemClass) {
        try {
            // Get the declared constructor (even if it's not public)
            Constructor<? extends Item> constructor = itemClass.getDeclaredConstructor(Ethyrial.class);

            // Make it accessible
            constructor.setAccessible(true);

            // Create instance with the actual plugin reference
            Item item = constructor.newInstance((Ethyrial) plugin);

            itemMap.put(item.getKey(), item);

            if (item instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) item, plugin);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register item: " + itemClass.getName());
            e.printStackTrace();
        }
    }

    public Item getItem(NamespacedKey key) {
        return itemMap.get(key);
    }

    public List<Item> getItems() {
        return new ArrayList<>(itemMap.values());
    }

    @Override
    public @Nullable Keyed get(@NotNull NamespacedKey key) {
        return getItem(key);
    }

    // @Override
    public @NotNull Keyed getOrThrow(@NotNull NamespacedKey key) {
        Item item = getItem(key);
        if (item == null) {
            throw new IllegalArgumentException("Item not found for key: " + key);
        }
        return item;
    }

    @Override
    public @NotNull Stream<Keyed> stream() {
        return itemMap.values().stream().map(item -> (Keyed) item);
    }

    @Override
    public Iterator<Keyed> iterator() {
        return itemMap.values().stream().map(item -> (Keyed) item).iterator();
    }
}

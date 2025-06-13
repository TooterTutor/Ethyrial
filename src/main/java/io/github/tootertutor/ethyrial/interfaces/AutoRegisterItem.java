package io.github.tootertutor.ethyrial.interfaces;

import org.bukkit.event.Listener;

import io.github.tootertutor.ethyrial.registry.SpellRegister;

/**
 * Marker interface for automatic registration of custom items within the
 * plugin.
 * <p>
 * Any class implementing this interface will be automatically:
 * <ul>
 * <li>Instantiated during plugin startup</li>
 * <li>Registered in the ItemRegistry</li>
 * <li>Registered as a Bukkit listener if applicable</li>
 * <li>Processed for custom recipe registration</li>
 * </ul>
 * 
 * <h2>Usage Requirements:</h2>
 * Implementing classes must:
 * <ol>
 * <li>Extend {@link CustomItem}</li>
 * <li>Provide a constructor with a single {@link ethyrial} parameter</li>
 * <li>Reside in the {@code io.github.tootertutor.ethyrial.items} package
 * or subpackage</li>
 * </ol>
 * 
 * <h2>Example Implementation:</h2>
 * 
 * <pre>{@code
 * public class ExampleItem extends CustomItem implements AutoRegisterItem {
 *     public ExampleItem(ethyrial plugin) {
 *         super(plugin);
 *         // Initialization code
 *     }
 * 
 *     // Rest of implementation...
 * }
 * }</pre>
 * 
 * <h2>Registration Process:</h2>
 * During plugin enable:
 * <ol>
 * <li>Classpath scanning detects all implementations</li>
 * <li>Each class is instantiated via reflection</li>
 * <li>Instance is added to the ItemRegistry</li>
 * <li>If implements {@link Listener}, registers with Bukkit</li>
 * </ol>
 * 
 * @see SpellRegister#autoRegisterItems() Registration handler
 * @see CustomItem Base class requirements
 * @since 2.0.0
 * @author TooterTutor
 */
public interface AutoRegisterItem {
    /**
     * Marker interface - no methods required.
     * <p>
     * Presence of this interface triggers the automatic registration system.
     * 
     * <h3>Technical Notes:</h3>
     * Registration is handled through reflection-based classpath scanning.
     * Classes are discovered using Java's built-in resource loading mechanisms,
     * making this solution Bukkit-compatible without external dependencies.
     */
}
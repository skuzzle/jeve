package de.skuzzle.jeve.annotation;

/**
 * Specifies the kind of a {@link de.skuzzle.jeve.Listener Listener} definition. This
 * defines the signature to which each method in the target interface must conform.
 * 
 * @author Simon Taddiken
 * @since 1.1.0
 * @see ListenerInterface#value()
 */
public enum ListenerKind {
    /**
     * A normal listener as an interface which extends 
     * {@link de.skuzzle.jeve.Listener Listener} and which only contains methods adhering
     * to the following conditions:
     * <ul>
     *   <li>The return value is <tt>void</tt></li>
     *   <li>The only parameter is a sub type of {@link de.skuzzle.jeve.Event Event}</li>
     *   <li>No checked exceptions are thrown</li>
     * </ul>
     */
    NORMAL,
    
    /**
     * An abortable listener as an interface which extends 
     * {@link de.skuzzle.jeve.Listener Listener} and which only contains methods adhering
     * to the following conditions:
     * <ul>
     *   <li>The return value is <tt>boolean</tt> or <tt>Boolean</tt></li>
     *   <li>The only parameter is a su btype of {@link de.skuzzle.jeve.Event Event}</li>
     *   <li>No checked exceptions are thrown</li>
     * </ul>
     */
    ABORTABLE,
    
    /**
     * A tagging listener is an interface which extends 
     * {@link de.skuzzle.jeve.Listener Listener} and does not contain any methods but 
     * exists as super interface for other listeners. 
     */
    TAGGING,
    
    /**
     * A mixed listener as an interface which extends 
     * {@link de.skuzzle.jeve.Listener Listener} and which only contains methods adhering
     * to the following conditions:
     * <ul>
     *   <li>The return value is <tt>void</tt>, <tt>boolean</tt> or <tt>Boolean</tt></li>
     *   <li>The only parameter is a su btype of {@link de.skuzzle.jeve.Event Event}</li>
     *   <li>No checked exceptions are thrown</li>
     * </ul>
     * 
     * This allows you to specify both normal listening as well as abortable listening 
     * methods.
     */
    MIXED
}

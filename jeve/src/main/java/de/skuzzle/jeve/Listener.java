package de.skuzzle.jeve;

import java.util.EventListener;

/**
 * This is the base interface for event listeners. It specifies a default method
 * which can be used to automatically remove instances of this listener from a
 * certain parent and further methods that are notified when the listener is
 * registered or removed to or from an {@link EventProvider}.
 *
 * Normally, you create an interface extending {@code Listener} and add some
 * <em>listening methods</em>. By default, those methods must adhere to the
 * signature:
 *
 * <pre>
 * public void &lt;listeningName&gt;(&lt;subclass of Event&gt; e);
 * </pre>
 *
 * This allows you to provide a method reference conforming to the
 * {@link java.util.function.BiConsumer BiConsumer} functional interface to the
 * {@link EventProvider#dispatch(Event, java.util.function.BiConsumer) dispatch}
 * method of an EventProvider.
 *
 * <pre>
 * eventProvider.dispatch(MyListener.class, someEventInstance, MyListener::listeningMethod);
 * </pre>
 *
 * <h2>ListenerInterface Annotation</h2> To enable compile time checks for
 * whether your listener definition adheres to the different kind of listening
 * methods, you may tag it with
 * {@link de.skuzzle.jeve.annotation.ListenerInterface ListenerInterface}. This
 * is completely optional but makes your intentions clear to other programmers.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
public interface Listener extends EventListener {

    /**
     * This method specifies whether this listner's work is done and it should be
     * removed from its parent's {@link EventProvider} after the next time the listener
     * was notified. If this method throws an unchecked exception, it will be covered
     * by the parent EventProvider's {@link ExceptionCallback} mechanism.
     *
     * <p>Note: currently, every listener is at least notified once before checking the
     * result of this method the first time. This might change in future releases.</p>
     *
     * <p>Note: the default implementation always returns <code>false</code>, meaning
     * that the listener never gets removed automatically.</p>
     *
     * @param parent The event provider from which the listener would be removed.
     * @return Whether to remove this listener from its parent after next notification.
     * @deprecated Since 1.1.0 - Deprecated in favor of manual listener removal using
     *          {@code event.getSource().removeSomeListener(this)}
     */
    @Deprecated
    public default boolean workDone(EventProvider parent) {
        return false;
    }



    /**
     * This method is called right after this listener has been registered to a new
     * {@link EventProvider}. If this method throws an unchecked exception, it will be
     * covered by the new EventProvider's {@link ExceptionCallback}.
     *
     * <p>Note: The default implementation does nothing.</p>
     *
     * @param e This event object holds the new parent EventProvider and the class for
     *          which this listener has been registered.
     */
    public default void onRegister(RegistrationEvent e) {
        // default: do nothing
    }



    /**
     * This method is called right after this listener has been removed from an
     * {@link EventProvider}. If this method throws an unchecked exception, it will be
     * covered by the former EventProvider's {@link ExceptionCallback}.
     *
     * <p>Note: The default implementation does nothing.</p>
     *
     * @param e This event object holds the former parent EventProvider and the class for
     *          which this listener has been unregistered.
     */
    public default void onUnregister(RegistrationEvent e) {
        // default: do nothing
    }
}
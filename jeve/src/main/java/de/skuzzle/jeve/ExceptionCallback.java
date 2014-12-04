package de.skuzzle.jeve;

/**
 * Interface for providing errors which occur during event dispatching to the
 * caller.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
public interface ExceptionCallback {

    /**
     * Callback method which gets passed an exception. This method will be
     * called by an {@link EventProvider} if an instance of this interface has
     * been set as exception callback. This method is generally called within
     * the same thread in which the attempt to notify the listener has been
     * made.
     *
     * <p>
     * Note: If this method throws any unchecked exceptions other than
     * {@link AbortionException}, they will be swallowed by the EventProvider
     * during error handling.
     * </p>
     *
     * @param e The exception which occurred during event dispatching.
     * @param source The event listener which caused the exception.
     * @param event The event which is currently being processed.
     * @throws AbortionException can be thrown to make event dispatching
     *             explicitly fail with an exception. No further listeners will
     *             be notified and the caller of
     *             {@link EventProvider#dispatch(Event, java.util.function.BiConsumer)
     *             dispatch} will receive this exception.
     * @deprecated Since 2.1.0 - use
     *             {@link #exception(EventProvider, Exception, Listener, Event)}
     *             instead. This method will not be notified anymore when resp.
     *             method's default implemenation has been overridden.
     */
    @Deprecated
    public void exception(Exception e, Listener source, Event<?, ?> event);

    /**
     * Callback method which gets passed an exception. This method will be
     * called by an {@link EventProvider} if an instance of this interface has
     * been set as exception callback. This method is generally called within
     * the same thread in which the attempt to notify the listener has been
     * made.
     *
     * <p>
     * Note: If this method throws any unchecked exceptions other than
     * {@link AbortionException}, they will be swallowed by the EventProvider
     * during error handling.
     * </p>
     *
     * @param provider The provider which was dispatching the erroneous event.
     * @param e The exception which occurred during event dispatching.
     * @param source The event listener which caused the exception.
     * @param cause The event which is currently being processed.
     * @throws AbortionException can be thrown to make event dispatching
     *             explicitly fail with an exception. No further listeners will
     *             be notified and the caller of
     *             {@link EventProvider#dispatch(Event, java.util.function.BiConsumer)
     *             dispatch} will receive this exception.
     * @since 2.1.0
     */
    public default void exception(EventProvider<?> provider, Exception e,
            Listener source, Event<?, ?> cause) {
        exception(e, source, cause);
    }
}

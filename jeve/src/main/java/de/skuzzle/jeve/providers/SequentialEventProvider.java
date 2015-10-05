package de.skuzzle.jeve.providers;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.EventStack;
import de.skuzzle.jeve.EventStackHelper;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.SequentialEvent;

/**
 * {@link EventProvider} implementation which is always ready for dispatching
 * and simply runs all listeners within the current thread.
 *
 * <h2>Cascading Events</h2>
 * <p>
 * Sometimes, during dispatching an Event of type X, it might happen that a
 * cascaded dispatch action for the same type, or for another type Y is
 * triggered. If you want to prevent cascaded events, you may register listener
 * classes to be prevented on the event to dispatch. For this purpose, instead
 * of extending {@link Event}, your events must extend {@link SequentialEvent}.
 * For example: {@code public class UserEvent extends
 * SequentialEvent<UserManager, UserListener> ...}
 * </p>
 *
 * <pre>
 * UserEvent e = new UserEvent(this, user);
 *
 * // While dispatching 'e', no UIRefreshEvents shall be dispatched.
 * e.preventCascade(UIRefreshEvent.class);
 * eventProvider.dispatch(e, UserListener::userAdded);
 * </pre>
 *
 * <p>
 * With {@link SequentialEvent#preventCascade()} comes a convenience method to
 * prevent cascaded events of the same type. During dispatch, all events which
 * have been suppressed using the prevention mechanism, are collected and can be
 * retrieved with {@link SequentialEvent#getSuppressedEvents()}. This allows
 * you to inspect or re-dispatch them afterwards:
 * </p>
 *
 * <pre>
 * UserEvent e = new UserEvent(this, user);
 *
 * // While dispatching 'e', no UIRefreshEvents shall be dispatched.
 * e.preventCascade(UIRefreshEvent.class);
 * eventProvider.dispatch(e, UserListener::userAdded);
 *
 * // Dispatch all suppressed UIRefreshEvents
 * e.getSuppressedEvents().stream()
 *         .filter(suppressed -&gt; suppressed.getListenerClass() == UIRefreshListener.class)
 *         .forEach(suppressed -&gt; suppressed.redispatch(eventProvider));
 * </pre>
 *
 * @param <S> The type of the ListenerStore this provider uses.
 * @author Simon Taddiken
 * @since 3.0.0
 * @see SequentialEvent
 */
public class SequentialEventProvider<S extends ListenerStore> extends
        AbstractEventProvider<S> {

    private final EventStackImpl eventStack;

    /**
     * Creates a new SynchronousEventProvider.
     *
     * @param store Responsible for storing and retrieving listeners of this
     *            provider.
     */
    public SequentialEventProvider(S store) {
        super(store);
        this.eventStack = new EventStackImpl();
    }

    @Override
    protected <L extends Listener, E extends Event<?, L>> void notifyListeners(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {
        if (EventStackHelper.checkPrevent(this.eventStack, event, bc, ec)) {
            return;
        }

        // HINT: getListeners is thread safe
        final Stream<L> listeners = listeners().get(event.getListenerClass());

        try {
            event.setListenerStore(listeners());
            if (event instanceof SequentialEvent<?, ?>) {
                ((SequentialEvent<?, ?>) event).setEventStack(this.eventStack);
            }
            this.eventStack.pushEvent(event);
            final Iterator<L> it = listeners.iterator();
            while (it.hasNext()) {
                final L listener = it.next();
                if (event.isHandled()) {
                    return;
                }
                notifySingle(listener, event, bc, ec);
            }
        } finally {
            this.eventStack.popEvent(event);
        }
        return;
    }

    /**
     * Gets the current event stack. It holds information about nested
     * dispatches in progress.
     *
     * @return The event stack.
     */
    public EventStack getEventStack() {
        return this.eventStack;
    }

    @Override
    public boolean canDispatch() {
        return true;
    }

    @Override
    protected boolean isImplementationSequential() {
        return true;
    }
}

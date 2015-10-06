package de.skuzzle.jeve.stores;

import java.util.function.Supplier;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

/**
 * This source offers the functionality to wrap around another source and notify
 * listeners in a statically defined order. The order in which listeners are
 * notified is made explicit at creation time of this source. The creator of
 * this source must know the types of the Listeners that must be notified in
 * certain order. It needs not to know where they come from as they are
 * supplied by the wrapped source. The creator of the chain may also choose to
 * only notify the listeners that participate in the chain. Otherwise, the
 * participating listener are notified first and then the remaining in arbitrary
 * order.
 * <p>
 * To create a chain use the builder method {@link #basedOn(ListenerSource)}
 * like in the following example:
 *
 * <pre>
 * ListenerSource globalListeners = ...
 * ListenerSource userAddedChain = ChainOfResponsibility.basedOn(globalListeners)
 *         .withParticipant(CreateUserService.class)
 *         .withParticipant(UpdateUserPropertiesService.class)
 *         .withParticipant(PersistUserService.class)
 *         .create();
 *
 * EventProvider addUserChain = EventProvider.createDefault(userAddedChain);
 *
 * public void addUser(String name) {
 *     User user = new User(name);
 *     UserEvent addEvent = new UserEvent(this, name);
 *
 *     // The first 3 listener to be notified are those defined as chain participants.
 *     // They might modify the event's User object before it is passed to the remaining
 *     // listeners
 *     addUserChain.dispatch(addEvent, UserListener::userAdded);
 * }
 * </pre>
 * <p>
 * Performance notes: This source retrieves listeners from the wrapped source
 * and provides a sorted and optionally filtered stream to the caller of
 * {@link #get(Class)}.
 *
 * @author Simon Taddiken
 */
public interface ChainOfResponsibility extends ListenerSource {

    public static ChainBuilder basedOn(ListenerSource source) {
        return new ChainOfResponsibilityImpl.ChainBuilderImpl(source);
    }

    interface ChainBuilder {
        @SuppressWarnings("unchecked")
        ChainBuilder withParticipants(Class<? extends Listener>... listeners);

        ChainBuilder withParticipant(Class<? extends Listener> listener);

        ChainBuilder enabledFiltering();

        default Supplier<ListenerSource> createSupplier() {
            return () -> create();
        }

        ListenerSource create();
    }

    @Override
    public ChainOfResponsibility synchronizedView();
}

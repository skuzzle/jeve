package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderChooser;

/**
 * Hook in point for custom fluent buidler API to use with
 * {@link EventProvider#configure()}. Default usage pattern is:
 *
 * <pre>
 * EventProvider.configure()
 *     .defaultStore()
 *     .useCustomProvider(new MyCustomConfigurator()).and()
 *     ... // your API follows here
 * </pre>
 *
 * <p>
 * The {@link ProviderChooser#useCustomProvider(CustomConfigurator)} method
 * calls this class' {@link #getConfigurator(Supplier)} method, passing in an
 * object that supplies the listener store which the user selected in the first
 * step. This method should return an instance of your own fluent API
 * implementation.
 * </p>
 *
 * <p>
 * The {@link #createNow(Supplier)} method is called if the user decided to
 * create the EventProvider instance right after choosing the ListenerStore like
 * in:
 * </p>
 *
 * <pre>
 * EventProvier&lt;?&gt; provider = EventProvider.configure()
 *     .defaultStore()
 *     .useCustomProvider(new MyCustomConfigurator)
 *     .create(); // or .createSupplier()
 * </pre>
 *
 * @author Simon Taddiken
 * @param <S> The type of the ListenerStore to use.
 * @param <C> The type of your own fluent API interface.
 * @param <E> The type of the EventProvider this configurator will create.
 * @since 2.0.0
 */
public interface CustomConfigurator<S extends ListenerStore, C, E extends EventProvider<S>> {

    /**
     * Creates the object which will be returned by
     * {@link ProviderChooser#useCustomProvider(CustomConfigurator)}.
     *
     * @param storeSupplier A supplier for the ListenerStore which has been
     *            configured in the first step.
     * @return Your custom fluent API interface object.
     */
    C getConfigurator(Supplier<S> storeSupplier);

    /**
     * Creates a new EventProvider with the ListenerStore configured in the
     * first step.
     *
     * @param storeSupplier A supplier for the ListenerStore which has been
     *            configured in the first step.
     * @return A new EventProvider.
     */
    E createNow(Supplier<S> storeSupplier);
}

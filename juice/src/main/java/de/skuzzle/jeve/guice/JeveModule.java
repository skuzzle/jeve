package de.skuzzle.jeve.guice;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeListener;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

/**
 * Installing this module automatically enables jeve's guice extension. It will
 * install a {@link TypeListener} that indexes all class that directly implement
 * a subclass of {@link Listener}. Using the {@link GuiceListenerSource},
 * instances of those classes can be retrieved without having to register them
 * explicitly on a store/source.
 * <p>
 * Using this module is as easy as either installing it in your own module or
 * explicitly specifying it when creating your guice injector.
 *
 * <pre>
 * public class MyModule extends AbstractModule() {
 *
 *     public void configure() {
 *         install(new JeveModule());
 *     }
 * }
 * </pre>
 * or
 * <pre>
 * Injector injector = Guice.createInjector(new JeveModule(), new MyOtherModule());
 * </pre>
 *
 * Furthermore the module can automatically be looked up as a Java Service
 * Provider Interface by using the {@link ServiceLoader} with the type
 * {@link Module}:
 *
 * <pre>
 * Iterable&lt;Module&gt; modules = ServiceLoader.load(Module.class);
 * </pre>
 * <p>
 * When this module is installed it automatically binds a
 * {@link GuiceListenerSource} implementation to the key of a
 * {@link ListenerSource} annotated with then name "juice". It can thus be
 * injected by using:
 *
 * <pre>
 * &#064;Inject
 * &#064;Named(&quot;juice&quot;)
 * private ListenerSource source;
 * </pre>
 *
 * Alternatively you can create a juice ListenerSource using
 * {@link GuiceListenerSource#create(com.google.inject.Injector)}.
 * <p>
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
public final class JeveModule implements Module {

    private static final Logger LOG = LoggerFactory.getLogger(JeveModule.class);

    /**
     * This instance will be bound to identify whether the jeve module has been
     * installed.
     */
    private static final TestToken TEST_TOKEN = new TestToken();

    static final class TestToken {
    }

    @Override
    public void configure(Binder binder) {
        LOG.info("Configuring jeve for guice - juice");
        final TypeIndex index = new TypeIndex();
        final TypeIndexer indexer = new TypeIndexer(index);
        binder.bindListener(Matchers.any(), indexer);
        binder.bind(TypeIndex.class).toInstance(index);
        binder.bind(ListenerSource.class)
                .annotatedWith(Names.named("juice"))
                .to(GuiceListenerSourceImpl.class)
                .in(Singleton.class);
        binder.bind(TestToken.class).toInstance(TEST_TOKEN);
    }

}

package de.skuzzle.jeve.guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

import de.skuzzle.jeve.ListenerSource;

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

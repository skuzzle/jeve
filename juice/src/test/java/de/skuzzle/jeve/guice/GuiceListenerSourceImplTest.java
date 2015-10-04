package de.skuzzle.jeve.guice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

public class GuiceListenerSourceImplTest {

    private interface SampleListener extends Listener, Cloneable {

    }

    private interface SampleListener2 extends Listener {

    }

    static class SampleController implements SampleListener {

    }

    static class SampleController2 implements SampleListener2 {

    }

    static class SampleController3 extends SampleController {

    }

    @Inject
    @Named("juice")
    private ListenerSource source;

    @Before
    public void setUp() throws Exception {
        Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                install(new JeveModule());
                bind(SampleController.class);
                bind(SampleController3.class);
            }
        }).injectMembers(this);
    }

    @Test(expected = IllegalStateException.class)
    public void testForeignInjector() throws Exception {
        final Injector inj = Guice.createInjector();
        GuiceListenerSource.create(inj);
    }

    @Test
    public void testGet() throws Exception {
        final Stream<SampleListener> s = this.source.get(SampleListener.class);
        final Iterator<SampleListener> it = s.iterator();
        final SampleListener listener = it.next();
        assertTrue(listener instanceof SampleController);
        assertFalse(it.hasNext());

    }

    @Test
    public void testGetJITBinding() throws Exception {
        final Stream<SampleListener2> s = this.source.get(SampleListener2.class);
        assertFalse(s.iterator().hasNext());
    }
}

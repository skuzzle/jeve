package de.skuzzle.jeve;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.util.EventProviderFactory;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

@RunWith(Parameterized.class)
public class SynchronousEventProviderTest extends EventProviderTestBase {

    /**
     * Parameterizes the test instances.
     * @return Collection of parameters for the constructor of
     *          {@link EventProviderTestBase}.
     */
    @Parameters
    public final static Collection<Object[]> getParameters() {
        return Collections.singleton(
                new EventProviderFactory[] { EventProviders::newDefaultEventProvider }
        );
    }



    public SynchronousEventProviderTest(EventProviderFactory factory) {
        super(factory);
    }



    /**
     * Tests whether {@link AbortionException} is delegated to the caller of dispatch.
     * This behavior is only defined for synchronous providers
     */
    @Test(expected = AbortionException.class)
    public void testAbortionExceptionInCallback() {
        final ExceptionCallback ec = (e, l, event) -> { throw new AbortionException(e); };
        final StringListener l = event -> { throw new RuntimeException(); };
        this.subject.setExceptionCallback(ec);
        this.subject.addListener(StringListener.class, l);

        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
    }
}

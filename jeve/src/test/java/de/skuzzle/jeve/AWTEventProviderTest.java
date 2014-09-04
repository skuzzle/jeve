package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.util.EventProviderFactory;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;


/**
 * Tests AWT event providers
 *
 * @author Simon Taddiken
 */
@RunWith(Parameterized.class)
public class AWTEventProviderTest extends EventProviderTestBase {

    /**
     * Parameterizes the test instances.
     * @return Collection of parameters for the constructor of
     *          {@link EventProviderTestBase}.
     */
    @Parameters
    public final static Collection<Object[]> getParameters() {
        return Arrays.asList(
                new EventProviderFactory[] { EventProviders::newWaitingAWTEventProvider },
                new EventProviderFactory[] { EventProviders::newAsynchronousAWTEventProvider }
            );
    }



    /**
     * Creates new AWTEventProviderTests
     *
     * @param factory Factory to create a single provider
     */
    public AWTEventProviderTest(EventProviderFactory factory) {
        super(factory);
    }



    /**
     * Tests whether all events are fired within the AWT event thread.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testIsAWTEventThread() throws Exception {
        final StringListener l = event -> Assert.assertTrue(
                getFailString("Not invoked on AWT thread"),
                SwingUtilities.isEventDispatchThread());
        this.subject.addListener(StringListener.class, l);
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
    }
}

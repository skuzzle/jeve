package de.skuzzle.jeve;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.providers.AWTEventProvider;
import de.skuzzle.jeve.stores.PerformanceListenerStore;
import de.skuzzle.jeve.stores.PriorityListenerStore;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

/**
 * Tests AWT event providers
 *
 * @author Simon Taddiken
 */
@RunWith(Parameterized.class)
public class AWTEventProviderIT extends EventProviderTestBase<ListenerStore> {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] { EventProvider.configure()
                        .store(PerformanceListenerStore.create())
                        .useWaitingAWTEventProvider().and()
                        .synchronizeStore()
                        .createSupplier() },
                new Object[] { EventProvider.configure()
                        .store(PriorityListenerStore.create())
                        .useAsynchronAWTEventProvider().and()
                        .synchronizeStore()
                        .createSupplier() },
                new Object[] { EventProvider.configure()
                        .defaultStore()
                        .useWaitingAWTEventProvider().and()
                        .synchronizeStore().and()
                        .statistics()
                        .createSupplier() },
                new Object[] { EventProvider.configure()
                        .defaultStore()
                        .useAsynchronAWTEventProvider().and()
                        .synchronizeStore().and()
                        .statistics()
                        .createSupplier() }
                );
    }

    /**
     * Creates new AWTEventProviderTests
     *
     * @param factory Factory to create a single provider
     */
    public AWTEventProviderIT(
            Supplier<? extends EventProvider<ListenerStore>> factory) {
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
        this.subject.listeners().add(StringListener.class, l);
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
    }

    @Test
    public void testcanDispatch() {
        Assert.assertTrue(getFailString("canDispatch should always return true"),
                this.subject.canDispatch());
    }

    /**
     * Fires an event from the event thread
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    @Test
    public void testInvokeFromEventThread() throws InvocationTargetException, InterruptedException {
        Assume.assumeTrue(this.subject instanceof AWTEventProvider);
        final AWTEventProvider<?> eventProvider = (AWTEventProvider<?>) this.subject;
        Assume.assumeTrue(eventProvider.isInvokeNow());

        final boolean[] isEventThread = new boolean[1];
        final StringListener listener = se -> isEventThread[0] = SwingUtilities.isEventDispatchThread();
        this.subject.listeners().add(StringListener.class, listener);
        final StringEvent event = new StringEvent(this.subject, "");

        // dispatch from the awt thread
        SwingUtilities.invokeAndWait(() -> this.subject.dispatch(event, StringListener::onStringEvent));

        sleep(); // HACK: give async providers some time to execute
        Assert.assertTrue(getFailString("Listener has not been notified from the AWT event thread"), isEventThread[0]);
    }

    /**
     * Tests whether interrupting an 'invokeNow' leads to an abortion exception
     */
    @Test(expected = AbortionException.class)
    @Ignore
    public void testInterrupt() {
        Assume.assumeTrue(this.subject instanceof AWTEventProvider);
        final AWTEventProvider<?> eventProvider = (AWTEventProvider<?>) this.subject;
        if (!eventProvider.isInvokeNow()) {
            System.out.println("Skipping test for AWTEventProvider because its not set to 'invokeNow'");
        }

        final Thread mainThread = Thread.currentThread();
        final StringListener listener = se -> mainThread.interrupt();
        this.subject.listeners().add(StringListener.class, listener);
        final StringEvent event = new StringEvent(this.subject, "");
        this.subject.dispatch(event, StringListener::onStringEvent);
    }
}

package de.skuzzle.jeve;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import de.skuzzle.jeve.util.AbstractEventProviderTest;
import de.skuzzle.jeve.util.BothListener;
import de.skuzzle.jeve.util.DifferentStringEvent;
import de.skuzzle.jeve.util.DifferentStringListener;
import de.skuzzle.jeve.util.EventProviderFactory;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

/**
 * This class contains basic tests for all event providers
 *
 * @author Simon Taddiken
 */
@Ignore
public abstract class EventProviderTestBase extends AbstractEventProviderTest {

    /**
     * Creates a new Test class instance.
     *
     * @param factory A factory for creating event providers
     */
    public EventProviderTestBase(EventProviderFactory factory) {
        super(factory);
    }

    /**
     * Tests whether exception is thrown when trying to add <code>null</code> as
     * listener.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddListenerException() throws Exception {
        this.subject.addListener(StringListener.class, null);
    }

    /**
     * Tests whether exception is thrown when trying to add <code>null</code> as
     * listener class.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddListenerException2() throws Exception {
        this.subject.addListener(null, null);
    }

    /**
     * Tests whether exception is thrown when specifying <code>null</code> as
     * event.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDispatcException2() throws Exception {
        this.subject.dispatch(null, StringListener::onStringEvent);
    }

    /**
     * Tests whether exception is thrown when specifying <code>null</code> as
     * method to call.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDispatcException3() throws Exception {
        this.subject.dispatch(new StringEvent(this.subject, ""),
                (BiConsumer<StringListener, StringEvent>) null);
    }

    /**
     * Tests whether exception is thrown when specifying <code>null</code> as
     * exception callback to dispatch.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDispatcException4() throws Exception {
        this.subject.dispatch(new StringEvent(this.subject, ""),
                StringListener::onStringEvent, null);
    }

    /**
     * Tests whether the event provider registered itself at the event before
     * dispatching
     * 
     * @throws InterruptedException
     */
    @Test
    public void testEventProviderSet() throws InterruptedException {
        final StringEvent event = new StringEvent(this.subject, "");
        this.subject.dispatch(event, StringListener::onStringEvent);

        // HACK: give async providers some time to execute
        Thread.sleep(THREAD_WAIT_TIME);
        Assert.assertEquals(this.subject, event.getEventProvider());
    }

    /**
     * Tests whether listeners are notified in order they are added.
     *
     * <p>
     * This test case will not be executed for non sequential providers.
     * </p>
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testDelegationOrder() throws Exception {
        if (checkSkipNonSequential()) {
            return;
        }

        final int TESTS = 5;
        final int[] counter = new int[1];
        for (int i = 0; i < TESTS; ++i) {
            final int finalCopy = i;
            final StringListener listener = e -> {
                Assert.assertEquals(finalCopy, counter[0]);
                ++counter[0];
            };
            this.subject.addListener(StringListener.class, listener);
        }
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
    }

    /**
     * Tests whether listeners are returned in order they have been added by
     * {@link EventProvider#getListeners(Class)}.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testListenerOrder() throws Exception {
        final int TESTS = 5;
        for (int i = 0; i < TESTS; ++i) {
            final int finalCopy = i;
            final StringListener listener = new StringListener() {
                @Override
                public void onStringEvent(StringEvent e) {
                    // ignore
                }

                @Override
                public String toString() {
                    return "" + finalCopy;
                };
            };
            this.subject.addListener(StringListener.class, listener);
        }
        final Collection<StringListener> listeners = this.subject.getListeners(
                StringListener.class);
        Assert.assertEquals(getFailString("Listener size differ"), TESTS, listeners.size());
        final Iterator<StringListener> it = listeners.iterator();
        for (int i = 0; i < TESTS; ++i) {
            Assert.assertEquals(getFailString("Wrong order"), "" + i, it.next().toString());
        }
    }

    /**
     * Tests whether further listener is notified if first listener throws an
     * exception.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testExceptionHandling() throws Exception {
        final String SUBJECT = "someString";
        final StringListener first = e -> {
            throw new RuntimeException();
        };
        final StringListener second = Mockito.mock(StringListener.class);

        this.subject.addListener(StringListener.class, first);
        this.subject.addListener(StringListener.class, second);
        final StringEvent e = new StringEvent(this.subject, SUBJECT);
        this.subject.dispatch(e, StringListener::onStringEvent,
                (ex, l, ev) -> {
                }); // swallow exception
        // HACK: give async providers some time to execute
        Thread.sleep(THREAD_WAIT_TIME);

        Mockito.verify(second).onStringEvent(Mockito.eq(e));
    }

    /**
     * Tests the global exception handler of the EventProvider
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testGlobalExceptionHandling() throws Exception {
        final String SUBJECT = "someString";
        final StringListener first = e -> {
            throw new RuntimeException(SUBJECT);
        };
        final StringListener second = Mockito.mock(StringListener.class);

        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);

        this.subject.setExceptionCallback(ec);
        this.subject.addListener(StringListener.class, first);
        this.subject.addListener(StringListener.class, second);

        final StringEvent e = new StringEvent(this.subject, SUBJECT);
        this.subject.dispatch(e, StringListener::onStringEvent);
        // HACK: give async providers some time to execute
        Thread.sleep(THREAD_WAIT_TIME);

        Mockito.verify(second).onStringEvent(Mockito.eq(e));
        Mockito.verify(ec).exception(Mockito.any(), Mockito.any(), Mockito.eq(e));
    }

    /**
     * Tests whether explicit exception handler takes precedence over global
     * handler.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testGlobalExceptionHandlingPrecedence() throws Exception {
        final String SUBJECT = "someString";
        final StringListener first = e -> {
            throw new RuntimeException(SUBJECT);
        };

        final ExceptionCallback globalEc = Mockito.mock(ExceptionCallback.class);
        final ExceptionCallback localEc = Mockito.mock(ExceptionCallback.class);

        this.subject.setExceptionCallback(globalEc);
        this.subject.addListener(StringListener.class, first);

        final StringEvent e = new StringEvent(this.subject, SUBJECT);
        this.subject.dispatch(e, StringListener::onStringEvent, localEc);
        // HACK: give async providers some time to execute
        Thread.sleep(THREAD_WAIT_TIME);

        Mockito.verify(globalEc, Mockito.never())
                .exception(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(localEc).exception(Mockito.any(), Mockito.any(), Mockito.any());
    }

    /**
     * Tests whether only correct listeners are notified if multiple listener
     * classes have been registered.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testMultipleListenerTypes() throws Exception {
        final StringListener string1 = e -> {
        };
        final StringListener string2 = e -> {
        };
        final DifferentStringListener diffString1 =
                e -> Assert.fail(getFailString("Listener should not have been notified"));
        final DifferentStringListener diffString2 =
                e -> Assert.fail(getFailString("Listener should not have been notified"));

        this.subject.addListener(StringListener.class, string1);
        this.subject.addListener(DifferentStringListener.class, diffString1);

        this.subject.addListener(StringListener.class, string2);
        this.subject.addListener(DifferentStringListener.class, diffString2);

        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
    }

    /**
     * Tests whether delegation stops after event has been handled.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testHandleEvent() throws Exception {
        if (checkSkipNonSequential()) {
            return;
        }
        final String SUBJECT = "someString";
        final StringEvent e = new StringEvent(this.subject, SUBJECT);
        // first listener sets event to be handled
        final StringListener firstListener = event -> event.setHandled(true);
        final StringListener secondListener = Mockito.mock(StringListener.class);

        this.subject.addListener(StringListener.class, firstListener);
        this.subject.addListener(StringListener.class, secondListener);
        this.subject.dispatch(e, StringListener::onStringEvent);

        Thread.sleep(THREAD_WAIT_TIME);
        Mockito.verify(secondListener, Mockito.never()).onStringEvent(Mockito.eq(e));
    }

    /**
     * Tests whether AbortionException aborts the delegation process
     */
    @Test
    public void testAbortWithException() {
        if (checkSkipNonSequential()) {
            return;
        }
        final StringListener listener = Mockito.mock(StringListener.class);
        final StringListener listener2 = Mockito.mock(StringListener.class);
        Mockito.doThrow(AbortionException.class).when(listener).onStringEvent(Mockito.any());

        this.subject.addListener(StringListener.class, listener);
        this.subject.addListener(StringListener.class, listener2);
        final StringEvent e = new StringEvent(this.subject, "");

        try {
            this.subject.dispatch(e, StringListener::onStringEvent);
        } catch (AbortionException abort) {
        }
        Mockito.verify(listener2, Mockito.never()).onStringEvent(Mockito.any());
    }

    /**
     * Tests whether removal of a listener works.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testRemoveListener() throws Exception {
        final StringListener listener = Mockito.mock(StringListener.class);
        this.subject.addListener(StringListener.class, listener);
        this.subject.removeListener(StringListener.class, listener);
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        Mockito.verify(listener, Mockito.never()).onStringEvent(Mockito.any());
    }

    /**
     * Tests whether notifying a listener about registration and removal works.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testNotifyListener() throws Exception {
        final StringListener listener = Mockito.mock(StringListener.class);
        this.subject.addListener(StringListener.class, listener);
        this.subject.removeListener(StringListener.class, listener);
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        Mockito.verify(listener, Mockito.never()).onStringEvent(Mockito.any());
        Mockito.verify(listener).onRegister(Mockito.any());
        Mockito.verify(listener).onUnregister(Mockito.any());
    }

    /**
     * Tests exceptions are caught by the default ExceptionCallback of an
     * EventProvider if a listener's onRegister or onUnregister method throws an
     * exception
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testNotifyListenerWithException() throws Exception {
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        this.subject.setExceptionCallback(ec);

        final StringListener listener = Mockito.mock(StringListener.class);
        Mockito.doThrow(RuntimeException.class).when(listener).onRegister(Mockito.any());
        Mockito.doThrow(RuntimeException.class).when(listener).onUnregister(Mockito.any());

        this.subject.addListener(StringListener.class, listener);
        this.subject.removeListener(StringListener.class, listener);
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        Mockito.verify(listener, Mockito.never()).onStringEvent(Mockito.any());
        Mockito.verify(ec, Mockito.times(2)).exception(
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    /**
     * Registers an object which implements two kinds of listeners for both
     * events, then removes it for one again.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void removeMutliListener() throws Exception {
        final BothListener listener = Mockito.mock(BothListener.class);
        this.subject.addListener(StringListener.class, listener);
        this.subject.addListener(DifferentStringListener.class, listener);

        this.subject.removeListener(DifferentStringListener.class, listener);
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
        // HACK: give async providers some time to execute
        Thread.sleep(THREAD_WAIT_TIME);

        Mockito.verify(listener).onStringEvent(Mockito.any());
        Mockito.verify(listener, Mockito.never()).onDifferentStringEvent(Mockito.any());
    }

    /**
     * Tests whether clearing of a certain listener class works.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testClearAllByClass() throws Exception {
        final StringListener string1 = e -> {
        };
        final StringListener string2 = e -> {
        };
        final DifferentStringListener diffString1 = e -> Assert.fail(getFailString("Listener should not have been notified"));
        final DifferentStringListener diffString2 = e -> Assert.fail(getFailString("Listener should not have been notified"));

        this.subject.addListener(StringListener.class, string1);
        this.subject.addListener(DifferentStringListener.class, diffString1);

        this.subject.addListener(StringListener.class, string2);
        this.subject.addListener(DifferentStringListener.class, diffString2);

        this.subject.clearAllListeners(DifferentStringListener.class);
        final StringEvent e = new StringEvent(this.subject, "");
        final DifferentStringEvent e1 = new DifferentStringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
        this.subject.dispatch(e1, DifferentStringListener::onDifferentStringEvent);
    }

    /**
     * Tests whether clearing of a certain listener class works and the cleared
     * listeners are notified
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testClearAllByClassAndNotify() throws Exception {
        final StringListener string1 = Mockito.mock(StringListener.class);
        final DifferentStringListener diffString1 = Mockito.mock(DifferentStringListener.class);

        this.subject.addListener(StringListener.class, string1);
        this.subject.addListener(DifferentStringListener.class, diffString1);

        this.subject.clearAllListeners(DifferentStringListener.class);
        Mockito.verify(string1, Mockito.never()).onUnregister(Mockito.any());
        Mockito.verify(diffString1).onUnregister(Mockito.any());
    }

    /**
     * Tests whether clearing of all listeners works.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testClearAll() throws Exception {
        final StringListener string1 = Mockito.mock(StringListener.class);
        final StringListener string2 = Mockito.mock(StringListener.class);
        final DifferentStringListener diffString1 = Mockito.mock(DifferentStringListener.class);
        final DifferentStringListener diffString2 = Mockito.mock(DifferentStringListener.class);

        this.subject.addListener(StringListener.class, string1);
        this.subject.addListener(DifferentStringListener.class, diffString1);

        this.subject.addListener(StringListener.class, string2);
        this.subject.addListener(DifferentStringListener.class, diffString2);

        this.subject.clearAllListeners();
        final StringEvent e = new StringEvent(this.subject, "");
        final DifferentStringEvent e1 = new DifferentStringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
        this.subject.dispatch(e1, DifferentStringListener::onDifferentStringEvent);

        Mockito.verify(string1, Mockito.never()).onStringEvent(Mockito.any());
        Mockito.verify(string2, Mockito.never()).onStringEvent(Mockito.any());
        Mockito.verify(diffString1, Mockito.never()).onDifferentStringEvent(Mockito.any());
        Mockito.verify(diffString2, Mockito.never()).onDifferentStringEvent(Mockito.any());

    }

    /**
     * Tests whether clearing all listeners works and the cleared listeners are
     * notified
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testClearAllAndNotify() throws Exception {
        final StringListener string1 = Mockito.mock(StringListener.class);
        final DifferentStringListener diffString1 = Mockito.mock(DifferentStringListener.class);

        this.subject.addListener(StringListener.class, string1);
        this.subject.addListener(DifferentStringListener.class, diffString1);

        this.subject.clearAllListeners();

        Mockito.verify(string1).onUnregister(Mockito.any());
        Mockito.verify(diffString1).onUnregister(Mockito.any());
    }

    /**
     * Tests whether no listener is notified after EventProvider has been
     * closed.
     *
     * @throws Exception If an exception occurs during testing.
     */
    @Test
    public void testClose() throws Exception {
        final StringListener l = Mockito.mock(StringListener.class);
        this.subject.addListener(StringListener.class, l);
        this.subject.close();
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);
        // HACK: give async providers some time to execute
        Thread.sleep(THREAD_WAIT_TIME);

        Mockito.verify(l, Mockito.never()).onStringEvent(Mockito.any());
        Assert.assertTrue(getFailString("Listener not removed"),
                this.subject.getListeners(StringListener.class).isEmpty());
    }
}

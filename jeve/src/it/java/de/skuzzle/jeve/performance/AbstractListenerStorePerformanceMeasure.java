package de.skuzzle.jeve.performance;

import org.junit.Before;
import org.junit.Test;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

public abstract class AbstractListenerStorePerformanceMeasure<T extends ListenerStore> {

    protected interface ListenerImpl1 extends Listener {

    }

    protected interface ListenerImpl2 extends Listener {

    }

    protected interface ListenerImpl3 extends Listener {

    }

    protected interface ListenerImpl4 extends Listener {

    }

    private final static int WARM_UP_LOOP_COUNT = 0;
    private final static int LOOP_COUNT = 100000;

    protected T subject;

    protected abstract T createSubject();

    @Before
    public void setup() {
        this.subject = createSubject();
    }

    protected int getTestLoopCount() {
        return LOOP_COUNT;
    }

    @Test
    public void testGetPerformance() {
        final int listenerCount = 100000;
        final StopWatch watch = new LoggingStopWatch("addListeners");
        for (int i = 0; i < listenerCount; i++) {
            this.subject.add(ListenerImpl1.class, new ListenerImpl1() {});
            this.subject.add(ListenerImpl2.class, new ListenerImpl2() {});
            this.subject.add(ListenerImpl3.class, new ListenerImpl3() {});
            this.subject.add(ListenerImpl4.class, new ListenerImpl4() {});
        }
        watch.stop("addListeners");

        watch.start("warmup");
        for (int i = 0; i < WARM_UP_LOOP_COUNT; ++i) {
            this.subject.get(ListenerImpl1.class);
        }
        watch.stop("warmup");

        watch.start("get");
        for (int i = 0; i < getTestLoopCount(); ++i) {
            this.subject.get(ListenerImpl1.class);
        }
        watch.stop("get");

    }
}

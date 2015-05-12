package de.skuzzle.jeve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.skuzzle.jeve.invoke.FailedEventInvocation;

/**
 * This class can be used for collecting failed dispatch attempts during event
 * dispatching. This might preferably be used for debugging purposes to locally collect
 * all failed invocations:
 *
 * <pre>
 * FailedEventsCollector collector = FailedEventsCollecter.create();
 * eventProvider.dispatch(myEvent, MyListener::handle, collector);
 *
 * for (EventInvocation&lt;?,?&gt; failed : collector) {
 *     // ...
 * }
 * </pre>
 *
 * If you need to notify another ExceptionCallback, you can use the second factory
 * method. The following will collect all failed invocations and also delegate them to
 * the given other ExceptionCallback.
 *
 * <pre>
 * ExceptionCallback delegate = ...;
 * FailedEventsCollector collector = FailedEvents.delegatingTo(delegate);
 * ...
 * </pre>
 *
 * @author Simon Taddiken
 * @since 3.0.0
 */
public final class FailedEventsCollector implements ExceptionCallback,
        Iterable<FailedEventInvocation> {

    private final Object mutex = new Object();
    private List<FailedEventInvocation> failedEvents;
    private final ExceptionCallback delegate;

    private FailedEventsCollector(ExceptionCallback delegate) {
        this.delegate = delegate;
    }

    public static FailedEventsCollector delegatingTo(ExceptionCallback ec) {
        if (ec == null) {
            throw new IllegalArgumentException("ec is null");
        }
        return new FailedEventsCollector(ec);
    }

    public static FailedEventsCollector create() {
        return new FailedEventsCollector(null);
    }

    public List<FailedEventInvocation> getFailedInvocations() {
        synchronized (this.mutex) {
            if (this.failedEvents == null) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(this.failedEvents);
        }
    }

    @Override
    public Iterator<FailedEventInvocation> iterator() {
        return getFailedInvocations().iterator();
    }

    @Override
    public void exception(FailedEventInvocation invocation) {
        synchronized (this.mutex) {
            if (this.failedEvents == null) {
                this.failedEvents = new ArrayList<>();
            }
            this.failedEvents.add(invocation);
        }
        if (this.delegate != null) {
            this.delegate.exception(invocation);
        }
    }
}

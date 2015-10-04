package de.skuzzle.jeve.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

/**
 * Extension to the {@link ParallelEventProvider} which notifies all listeners
 * in parallel but blocks the dispatching thread until all listeners have been
 * notified.
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
public class BlockingParallelEventProvider extends ParallelEventProvider {

    /**
     * Creates a new BlockingParallelEventProvider using the provided source and
     * executor.
     *
     * @param source Responsible for storing and retrieving listeners of this
     *            provider.
     * @param executor The executor to use.
     */
    public BlockingParallelEventProvider(ListenerSource source,
            ExecutorService executor) {
        super(source, executor);
    }

    /**
     * Creates a new BlockingParallelEventProvider using the provided source and
     * a cached thread pool for parallel dispatching.
     *
     * @param source Responsible for storing and retrieving listeners of this
     *            provider.
     */
    public BlockingParallelEventProvider(ListenerSource source) {
        super(source);
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc, ExceptionCallback ec) {

        checkDispatchArgs(event, bc, ec);
        if (!canDispatch()) {
            return;
        }

        // Collect listeners into collection to get the count of registered
        // listeners
        final Collection<L> c = getListenerSource()
                .get(event)
                .collect(Collectors.toList());

        final List<Future<?>> futures = new ArrayList<>(c.size());
        final CountDownLatch latch = new CountDownLatch(c.size());
        final Iterator<L> listeners = c.iterator();

        while (listeners.hasNext() && checkInterrupt()) {
            final L listener = listeners.next();
            final Future<?> future = this.executor.submit(() -> {
                try {
                    notifySingle(listener, event, bc, ec);
                } finally {
                    // notifySingle might throw an AbortionException but
                    // countDown must happen anyway
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        try {
            latch.await();
        } catch (final InterruptedException e) {
            LOGGER.error("Interrupted while waiting for listeners to be notified", e);
            for (final Future<?> future : futures) {
                future.cancel(true);
            }
            Thread.currentThread().interrupt();
        }
    }
}

package de.skuzzle.jeve.performance;

import org.junit.Ignore;

import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.stores.PerformanceListenerStore;

@Ignore
public class DefaultListenerStorePerformacneMeasure extends
        AbstractListenerStorePerformanceMeasure<ListenerStore> {

    @Override
    protected ListenerStore createSubject() {
        PerformanceListenerStore tresult = PerformanceListenerStore.create();
        // tresult.optimizeGet();
        return tresult;
    }
}

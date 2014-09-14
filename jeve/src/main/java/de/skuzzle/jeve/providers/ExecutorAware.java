package de.skuzzle.jeve.providers;

import java.util.concurrent.ExecutorService;

public interface ExecutorAware {

    public void setExecutorService(ExecutorService executor);
}

package de.skuzzle.jeve.util;

public class BothListener implements StringListener, DifferentStringListener {

    @Override
    public void onDifferentStringEvent(DifferentStringEvent e) {}

    @Override
    public void onStringEvent(StringEvent e) {}

}

package com.example.project1.pattern.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ConcreteSubject (Lecture 8: TsunamiWarningSystem -> here: music content).
 *
 * Holds the list of observers and the latest change (its "state"). The Artist / Album / Song /
 * DigitalMusic services call {@link #setContentChange} after saving; that stores the change and
 * runs {@link #notifyObservers()} which calls update() on every registered ConcreteObserver.
 * Spring registers all MusicContentObserver beans here at start-up (addObserver), so a new
 * kind of observer is just a new @Component.
 */
@Component
public class MusicContentPublisher implements MusicContentSubject {

    private static final Logger log = LoggerFactory.getLogger(MusicContentPublisher.class);

    private final List<MusicContentObserver> observers = new CopyOnWriteArrayList<>();
    private MusicContentEvent latestChange;

    public MusicContentPublisher(List<MusicContentObserver> registeredObservers) {
        observers.addAll(registeredObservers);
    }

    @Override
    public void addObserver(MusicContentObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(MusicContentObserver observer) {
        observers.remove(observer);
    }

    /** Lecture's setMessage(): change the state, then notify. */
    public synchronized void setContentChange(MusicContentEvent change) {
        this.latestChange = change;
        notifyObservers();
    }

    @Override
    public void notifyObservers() {
        for (MusicContentObserver observer : observers) {
            try {
                observer.update(latestChange);
            } catch (RuntimeException ex) {
                // one failing observer must never break saving an album or song
                log.warn("{} failed to handle {}: {}", observer.getClass().getSimpleName(),
                        latestChange.getType(), ex.getMessage());
            }
        }
    }

    public List<MusicContentObserver> getObservers() {
        return List.copyOf(observers);
    }
}

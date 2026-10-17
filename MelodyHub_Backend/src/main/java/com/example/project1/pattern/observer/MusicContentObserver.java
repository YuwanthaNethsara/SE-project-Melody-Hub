package com.example.project1.pattern.observer;

/**
 * OBSERVER pattern (Behavioral) - Artist & Music Content Management.
 * Lecture 8 terminology: this is the "Observer" interface with update().
 */
public interface MusicContentObserver {

    /** Called by the Subject whenever music content is added, updated or published. */
    void update(MusicContentEvent event);
}

package com.example.project1.pattern.observer;

/** Lecture 8 "Subject" interface: manages the observer list and notifies it. */
public interface MusicContentSubject {

    void addObserver(MusicContentObserver observer);

    void removeObserver(MusicContentObserver observer);

    void notifyObservers();
}

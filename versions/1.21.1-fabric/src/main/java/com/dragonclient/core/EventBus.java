package com.dragonclient.core;

import com.dragonclient.event.Event;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus {
    private final Map<Class<? extends Event>, List<Consumer<? extends Event>>> listeners = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Event> void register(Class<T> eventClass, Consumer<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void post(T event) {
        List<Consumer<? extends Event>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer<? extends Event> listener : eventListeners) {
                try {
                    ((Consumer<T>) listener).accept(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public <T extends Event> void unregister(Class<T> eventClass, Consumer<T> listener) {
        List<Consumer<? extends Event>> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    public void clear() {
        listeners.clear();
    }
}

package com.example.artflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArtworkStore {
    public interface Listener {
        void onArtworkAdded(ArtworkModel model);
    }

    private static final ArtworkStore INSTANCE = new ArtworkStore();

    private final List<ArtworkModel> items = new ArrayList<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private ArtworkStore() {}

    public static ArtworkStore getInstance() { return INSTANCE; }

    public synchronized void add(ArtworkModel model) {
        if (model == null) return;
        items.add(model);
        // notify listeners
        for (Listener l : listeners) {
            try { l.onArtworkAdded(model); } catch (Exception ignored) {}
        }
    }

    public synchronized List<ArtworkModel> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public void addListener(Listener l) {
        if (l != null) listeners.add(l);
    }

    public void removeListener(Listener l) {
        if (l != null) listeners.remove(l);
    }
}


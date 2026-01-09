package com.example.artflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArtworkStore {
    public interface Listener {
        void onArtworkAdded(ArtworkModel model);
        default void onArtworkUpdated(ArtworkModel model) {}
        default void onArtworkRemoved(String id) {}
    }

    private static final ArtworkStore INSTANCE = new ArtworkStore();

    private final List<ArtworkModel> items = new ArrayList<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private ArtworkStore() {
        // initialize from DB
        try {
            DatabaseHelper db = DatabaseHelper.getInstance();
            List<ArtworkModel> fromDb = db.listArtworks();
            if (fromDb != null && !fromDb.isEmpty()) {
                items.addAll(fromDb);
            }
        } catch (Exception e) {
            System.err.println("ArtworkStore: failed to load from DB: " + e.getMessage());
        }
    }

    public static ArtworkStore getInstance() { return INSTANCE; }

    public synchronized void add(ArtworkModel model) {
        if (model == null) return;
        try {
            DatabaseHelper db = DatabaseHelper.getInstance();
            boolean ok = db.insertArtwork(model);
            if (!ok) {
                System.err.println("ArtworkStore: DB insert failed for " + model.getId());
            }
        } catch (Exception e) {
            System.err.println("ArtworkStore: DB insert error: " + e.getMessage());
        }
        items.add(model);
        // notify listeners
        for (Listener l : listeners) {
            try { l.onArtworkAdded(model); } catch (Exception ignored) {}
        }
    }

    public synchronized void update(ArtworkModel model) {
        if (model == null) return;
        try {
            DatabaseHelper db = DatabaseHelper.getInstance();
            boolean ok = db.updateArtwork(model);
            if (!ok) {
                // try insert if update didn't affect rows
                ok = db.insertArtwork(model);
            }
            if (!ok) System.err.println("ArtworkStore: DB update/insert failed for " + model.getId());
        } catch (Exception e) {
            System.err.println("ArtworkStore: DB update error: " + e.getMessage());
        }

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(model.getId())) {
                items.set(i, model);
                break;
            }
        }
        for (Listener l : listeners) {
            try { l.onArtworkUpdated(model); } catch (Exception ignored) {}
        }
    }

    public synchronized void removeById(String id) {
        if (id == null) return;
        try {
            DatabaseHelper db = DatabaseHelper.getInstance();
            boolean ok = db.deleteArtwork(id);
            if (!ok) System.err.println("ArtworkStore: DB delete failed for " + id);
        } catch (Exception e) {
            System.err.println("ArtworkStore: DB delete error: " + e.getMessage());
        }
        items.removeIf(a -> id.equals(a.getId()));
        for (Listener l : listeners) {
            try { l.onArtworkRemoved(id); } catch (Exception ignored) {}
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

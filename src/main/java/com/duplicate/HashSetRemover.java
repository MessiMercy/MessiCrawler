package com.duplicate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HashSetRemover<T> implements DuplicateRemover<T> {
    private Set<T> items = Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());

    public Set<T> getItem() {
        return items;
    }

    @Override
    public void resetDuplicateCheck() {
        this.items.clear();
    }

    @Override
    public int getRemoverCount() {
        return items.size();
    }

    @Override
    public int addCollections(Collection<T> collection) {
        try {
            return getItem().addAll(collection) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public boolean isDuplicate(T o) {
        return !items.add(o);
    }

}

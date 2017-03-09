package com.duplicate;

import java.util.Collection;

public interface DuplicateRemover<T> {
    boolean isDuplicate(T request);

    void resetDuplicateCheck();

    int getRemoverCount();

    int addCollections(Collection<T> collection);

}

package com.library.model;

import java.time.LocalDateTime;

public abstract class LibraryEntity {

    private final String id;
    private final LocalDateTime createdAt;

    public LibraryEntity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public abstract String getDisplayInfo();

    @Override
    public String toString() {
        return getDisplayInfo();
    }
}
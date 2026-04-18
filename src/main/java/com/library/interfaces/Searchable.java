package com.library.interfaces;

import java.util.List;

public interface Searchable<T> {
    List<T> searchByName(String name);
    T searchById(String id);
}
package com.github.brickwall2900.diary.utils;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {
        public Map<K, V> map = new HashMap<>();

        public static <K, V> MapBuilder<K, V> build() {
            return new MapBuilder<>();
        }

        public MapBuilder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Map<K, V> get() {
            return map;
        }
    }
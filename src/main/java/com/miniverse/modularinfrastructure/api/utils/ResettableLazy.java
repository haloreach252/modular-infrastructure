// Originally from Immersive Engineering, adapted under its license
package com.miniverse.modularinfrastructure.api.utils;

import java.util.function.Supplier;

/**
 * A lazy value holder that can be reset
 */
public class ResettableLazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T value;
    private boolean initialized = false;
    
    public ResettableLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    
    @Override
    public T get() {
        if (!initialized) {
            value = supplier.get();
            initialized = true;
        }
        return value;
    }
    
    public void reset() {
        initialized = false;
        value = null;
    }
}
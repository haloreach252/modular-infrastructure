package com.miniverse.modularinfrastructure.api.utils;

/**
 * A field that can only be set once
 */
public class SetRestrictedField<T> {
    private T value;
    private boolean isSet = false;
    
    public static <T> SetRestrictedField<T> common() {
        return new SetRestrictedField<>();
    }
    
    public void set(T value) {
        if (isSet) {
            throw new IllegalStateException("Value already set");
        }
        this.value = value;
        this.isSet = true;
    }
    
    public T get() {
        if (!isSet) {
            throw new IllegalStateException("Value not set");
        }
        return value;
    }
    
    public boolean isSet() {
        return isSet;
    }
}
package com.xkball.let_me_see_see.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowableSupplier<T> {
    
    static <T> T getOrThrow(ThrowableSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    static <T> T getOrElse(ThrowableSupplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return defaultValue;
        }
    }
    
    static <T> T getOrElse(ThrowableSupplier<T> supplier, T defaultValue, Consumer<Throwable> exceptionHandler) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            exceptionHandler.accept(e);
            return defaultValue;
        }
    }
    
    @Nullable
    static <T> T getOrNull(ThrowableSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return null;
        }
    }
    
    @Nullable
    static <T> T getOrNull(ThrowableSupplier<T> supplier, Consumer<Throwable> exceptionHandler) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            exceptionHandler.accept(e);
            return null;
        }
    }
    
    @NotNull
    T get() throws Throwable;
    
}

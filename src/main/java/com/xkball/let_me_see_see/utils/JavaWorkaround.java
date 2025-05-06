package com.xkball.let_me_see_see.utils;

import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandles;

public class JavaWorkaround {
    
    public static final Unsafe UNSAFE = getUnsafe();
    
    @Nullable
    public static final MethodHandles.Lookup TRUSTED_LOOKUP = ThrowableSupplier.getOrNull(JavaWorkaround::getTrustedLookup);
    
    public static Unsafe getUnsafe() {
        for (var field : Unsafe.class.getDeclaredFields()) {
            if (field.getType().equals(Unsafe.class)) {
                field.setAccessible(true);
                try {
                    return (Unsafe) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can not get unsafe instance.", e);
                }
            }
        }
        throw new RuntimeException("Can not get unsafe instance.");
    }
    
    @SuppressWarnings("deprecation")
    //Adapted from https://github.com/Lenni0451/Reflect under MIT license.
    public static MethodHandles.Lookup getTrustedLookup() throws Throwable {
        MethodHandles.Lookup lookup;
        MethodHandles.lookup();
        
        var lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long lookupFieldOffset = UNSAFE.staticFieldOffset(lookupField);
        lookup = (MethodHandles.Lookup) UNSAFE.getObject(UNSAFE.staticFieldBase(lookupField), lookupFieldOffset);
        if (lookup != null) return lookup;
        
        var theLookup = (MethodHandles.Lookup) ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class))
                .newInstance(MethodHandles.Lookup.class);
        return (MethodHandles.Lookup) theLookup.findStaticGetter(MethodHandles.Lookup.class, "IMPL_LOOKUP", MethodHandles.Lookup.class).invokeExact();
    }
    
}

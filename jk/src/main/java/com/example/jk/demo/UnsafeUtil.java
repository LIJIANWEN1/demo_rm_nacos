package com.example.jk.demo;
import sun.misc.Unsafe;
import java.lang.reflect.Field;

public final class UnsafeUtil {
    private static final Unsafe unsafe;

    private UnsafeUtil() {}

    public static Unsafe getUnsafe() {
        return unsafe;
    }

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get((Object)null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
}

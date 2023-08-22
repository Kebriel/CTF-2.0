package kebriel.ctf.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

    public static <T> T getField(Object from, String fieldName) {
        try {
            Field field = from.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(from);
        }catch(IllegalAccessException | NoSuchFieldException | ClassCastException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void setField(Object from, String fieldName, Object arg) {
        try {
            Field field = from.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(from, arg);
        }catch(IllegalAccessException | NoSuchFieldException | ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    public static void invokeVoidMethod(Object from, String methodName, Object... args) {
        try {
            Method method = from.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(from, args);
        }catch(InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public static <T> T invokeMethod(Object from, String methodName, Object... args) {
        try {
            Method method = from.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return (T) method.invoke(from, args);
        }catch(InvocationTargetException | IllegalAccessException | NoSuchMethodException | ClassCastException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static <T> T makeNewInstance(Class<T> type, Object... constructorParams) {
        try {
            return type.getConstructor().newInstance(constructorParams);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}

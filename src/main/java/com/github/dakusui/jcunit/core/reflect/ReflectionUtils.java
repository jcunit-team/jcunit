package com.github.dakusui.jcunit.core.reflect;

import com.github.dakusui.jcunit.core.Checks;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {
  private ReflectionUtils() {
  }

  public static Field getField(Class<?> clazz, String name) {
    try {
      return Checks.checknotnull(clazz).getField(Checks.checknotnull(name));
    } catch (NoSuchFieldException e) {
      String msg = String.format(
          "Field '%s' isn't defined in class '%s' or not public: canonical name='%s'",
          name,
          clazz.getSimpleName(),
          clazz.getCanonicalName());
      throw new IllegalArgumentException(msg, e);
    }
  }

  public static <T> T getFieldValue(Object obj, Field f) {
    try {
      return (T) Checks.checknotnull(f).get(obj);
    } catch (IllegalAccessException e) {
      Checks.rethrow(e);
    }
    return null;
  }

  public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
    try {
      return Checks.checknotnull(clazz).getMethod(Checks.checknotnull(methodName), params);
    } catch (NoSuchMethodException e) {
      Checks.rethrow(e);
    }
    return null;
  }

  public static <T> T create(Class<T> clazz) {
    try {
      return Checks.checknotnull(clazz).newInstance();
    } catch (IllegalAccessException e) {
      Checks.rethrow(e, "A no-parameter constructor of '%s' is too less open. Make it public.", clazz);
    } catch (InstantiationException e) {
      Checks.rethrow(e, "The class '%s' couldn't be instantiated.", clazz);
    }
    return null;
  }

  public static <T> T invokeMethod(Object obj, Method method, Object... args) {
    try {
      return (T) Checks.checknotnull(method).invoke(obj, args);
    } catch (InvocationTargetException e) {
      Checks.rethrow(e.getTargetException(), "Failed to execute method '%s' with ", method, args);
    } catch (IllegalAccessException e) {
      Checks.rethrow(e, "A method '%s' is too less open. Make it public.", method);
    }
    return null;
  }

  public static void setFieldValue(Object obj, Field f, Object value) {
    Checks.checknotnull(obj);
    Checks.checknotnull(f);
    boolean accessible = f.isAccessible();
    try {
      f.setAccessible(true);
      f.set(obj, value);
    } catch (IllegalAccessException e) {
      // This path should never be executed since the field is set accessible.
      Checks.rethrow(e, "Something went wrong.");
    } finally {
      f.setAccessible(accessible);
    }
  }
}
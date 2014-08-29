package com.github.dakusui.jcunit.tests.param;

import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ParamTypeTest {
  @Test
  public void testBoolean() {
    assertEquals("boolean", ParamType.Boolean.toString());
    whenValidValue$thenParsedValueReturned(ParamType.Boolean, true, "true");
    whenValidValue$thenParsedValueReturned(ParamType.BooleanArray, new Object[] { true, false }, "true", "false");
    whenInvalidValue$thenExceptionThrown(ParamType.Boolean, IllegalArgumentException.class, "TRuE");
  }

  @Test
  public void testByte() {
    assertEquals("byte", ParamType.Byte.toString());
    whenValidValue$thenParsedValueReturned(ParamType.Byte, (byte) 3, "3");
    whenValidValue$thenParsedValueReturned(ParamType.ByteArray, new Object[] { (byte) 1, (byte) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(ParamType.Byte, IllegalArgumentException.class, "ZZZ");
  }

  @Test
  public void testChar() {
    assertEquals("char", ParamType.Char.toString());
    whenValidValue$thenParsedValueReturned(ParamType.Char, 't', "t");
    whenValidValue$thenParsedValueReturned(ParamType.CharArray, new Object[] { 'a', 'b', 'c' }, "a", "b", "c");
    whenInvalidValue$thenExceptionThrown(ParamType.Char, IllegalArgumentException.class, "ABC");
  }

  @Test
  public void testShort() {
    assertEquals("short", ParamType.Short.toString());
    whenValidValue$thenParsedValueReturned(ParamType.Short, (short) 3, "3");
    whenValidValue$thenParsedValueReturned(ParamType.ShortArray, new Object[] { (short) 1, (short) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(ParamType.Short, IllegalArgumentException.class, "ZZZ");
  }

  @Test
  public void testInt() {
    assertEquals("int", ParamType.Int.toString());
    whenValidValue$thenParsedValueReturned(ParamType.Int, 3, "3");
    whenValidValue$thenParsedValueReturned(ParamType.IntArray, new Object[] { 1, 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(ParamType.Int, IllegalArgumentException.class, "ZZZ");
  }

  @Test
  public void testLong() {
    assertEquals("long", ParamType.Long.toString());
    whenValidValue$thenParsedValueReturned(ParamType.Long, (long) 3, "3");
    whenValidValue$thenParsedValueReturned(ParamType.LongArray, new Object[] { (long) 1, (long) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(ParamType.Long, IllegalArgumentException.class, "ZZZ");
  }

  @Test
  public void testFloat() {
    assertEquals("float", ParamType.Float.toString());
    whenValidValue$thenParsedValueReturned(ParamType.Float, (float) 3.1, "3.1");
    whenValidValue$thenParsedValueReturned(ParamType.FloatArray, new Object[] { (float) 1.2, (float) 2.3 }, "1.2", "2.3");
    whenInvalidValue$thenExceptionThrown(ParamType.Float, IllegalArgumentException.class, "ZZZ");
  }

  @Test
  public void testDouble() {
    assertEquals("double", ParamType.Double.toString());
    whenValidValue$thenParsedValueReturned(ParamType.Double, 3.1, "3.1");
    whenValidValue$thenParsedValueReturned(ParamType.DoubleArray, new Object[] { 1.2, 2.3 }, "1.2", "2.3");
    whenInvalidValue$thenExceptionThrown(ParamType.Double, IllegalArgumentException.class, "ZZZ");
  }

  @Test
  public void testString() {
    assertEquals("String", ParamType.String.toString());
    whenValidValue$thenParsedValueReturned(ParamType.String, "hello", "hello");
    whenValidValue$thenParsedValueReturned(ParamType.StringArray, new Object[] { "Hello", "World" }, "Hello", "World");
  }

  public void whenValidValue$thenParsedValueReturned(ParamType type, Object expected, String... value) {
    assertEquals(expected, type.parse(value));
  }

  public void whenValidValue$thenParsedValueReturned(ParamType type, Object[] expected, String... value) {
    assertArrayEquals(expected, (Object[]) type.parse(value));
  }

  public void whenInvalidValue$thenExceptionThrown(ParamType type, Class<? extends Throwable> expectedException, String... value) {
    try {
      type.parse(value);
    } catch (Throwable t) {
      assertTrue(
          String.format("Expected exception=%s, actual exception=%s\n\t%s",
              expectedException.getCanonicalName(),
              t.getClass().getCanonicalName(),
              Utils.join("\n\t", t.getStackTrace())),
          expectedException.isAssignableFrom(t.getClass())
      );
      return;
    }
    assertTrue(
        String.format("%s should have been thrown, but not.", expectedException.getCanonicalName()),
        false);
  }

  public static Param p1 = new Param() {
    @Override
    public String[] value() {
      return new String[]{"true"};
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Param.class;
    }
  };

  @Test
  public void processParamsTest1() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean}, new Param[]{p1});
    assertArrayEquals(new Object[]{Boolean.TRUE}, processed);
  }

  @Test
  public void processParamsTest2() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean.withDefaultValue(true)}, new Param[]{});
    assertArrayEquals(new Object[]{Boolean.TRUE}, processed);
  }

  @Test
  public void processParamsTest3() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean.withDefaultValue(true), ParamType.Boolean.withDefaultValue(false)}, new Param[]{});
    assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.FALSE}, processed);
  }

  @Test
  public void processParamsTest4() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean.withVarArgsEnabled()}, new Param[]{p1});
    assertArrayEquals(new Object[]{Boolean.TRUE}, processed);
  }

  @Test
  public void processParamsTest5() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean.withVarArgsEnabled()}, new Param[]{p1, p1});
    assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.TRUE}, processed);
  }

  @Test(expected = InvalidTestException.class)
  public void processParamsTest6$tooLittleParameters() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean, ParamType.Boolean.withVarArgsEnabled()}, new Param[]{});
    assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.TRUE}, processed);
  }
  @Test(expected = InvalidTestException.class)
  public void processParamsTest6$tooManyParameters() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean, ParamType.Boolean}, new Param[]{p1, p1, p1, p1});
    assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.TRUE}, processed);
  }

  @Test
  public void processParamsTest$defaultValueAndVarArgsInCombination() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean.withDefaultValue(false), ParamType.Boolean.withVarArgsEnabled()}, new Param[]{});
    assertArrayEquals(new Object[]{Boolean.FALSE}, processed);
  }

  @Test(expected = InvalidPluginException.class)
  public void processParamsTest$invalidDefaultNotAtLast() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean.withDefaultValue(false), ParamType.Boolean}, new Param[]{});
    assertArrayEquals(new Object[]{Boolean.FALSE}, processed);
  }

  @Test(expected = InvalidPluginException.class)
  public void processParamsTest$invalidVarargsNotAtLast() {
    Object[] processed = ParamType.processParams(new ParamType[]{ParamType.Boolean.withVarArgsEnabled(), ParamType.Boolean}, new Param[]{p1, p1});
  }

}
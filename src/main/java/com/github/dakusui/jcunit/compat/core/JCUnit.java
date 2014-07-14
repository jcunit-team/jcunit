package com.github.dakusui.jcunit.compat.core;

import com.github.dakusui.jcunit.compat.report.ReportWriter;
import com.github.dakusui.jcunit.compat.core.annotations.Generator;
import com.github.dakusui.jcunit.compat.core.annotations.GeneratorParameters;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitPluginException;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;
import com.github.dakusui.jcunit.compat.generators.SimpleTestArrayGenerator;
import com.github.dakusui.jcunit.compat.generators.TestArrayGenerator;
import com.github.dakusui.lisj.Basic;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class JCUnit extends Suite {
  private final ArrayList<Runner> runners = new ArrayList<Runner>();

  /**
   * Only called reflectively. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());
    List<Object> parametersList = getParametersList(getTestClass());
    for (int i = 0; i < parametersList.size(); i++) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          parametersList, i));
    }
  }

  @SuppressWarnings("unchecked")
  static Map<Field, Object> cast(Object values) {
    return (Map<Field, Object>) values;
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  private List<Object> getParametersList(TestClass klass) throws Throwable {
    @SuppressWarnings("rawtypes")
    Class<? extends TestArrayGenerator> generatorClass = getTestArrayGeneratorClass(
        klass
            .getJavaClass());
    if (generatorClass == null) {
      generatorClass = SimpleTestArrayGenerator.class;
    }
    return this.composeTestArray(klass.getJavaClass(), generatorClass);
  }

  static Method domainMethod(Class<?> cut, Field inField) {
    Method ret;
    try {
      try {
        ret = cut.getMethod(inField.getName());
      } catch (NoSuchMethodException e) {
        ret = cut.getDeclaredMethod(inField.getName());
      }
    } catch (SecurityException e) {
      String msg = String.format(
          "JCUnit cannot be run in this environment. (%s:%s)", e.getClass()
              .getName(), e.getMessage()
      );
      throw new JCUnitEnvironmentException(msg, e);
    } catch (NoSuchMethodException e) {
      String msg = String
          .format(
              "Method to generate a domain for '%s' isn't defined in class '%s' or not visible.",
              inField, cut);
      throw new ObjectUnderFrameworkException(msg, e);
    }
    if (!validateDomainMethod(inField, ret)) {
      String msg = String.format(
          "Domain method '%s' isn't compatible with field '%s'", ret, inField);
      throw new IllegalArgumentException(msg, null);
    }
    return ret;
  }

  public static boolean checkIfStatic(Method domainMethod) {
    return Modifier.isStatic(domainMethod.getModifiers());
  }

  private static boolean checkIfTypeCompatible(Field inField,
      Method domainMethod) {
    return domainMethod.getReturnType().isArray()
        && domainMethod.getReturnType().getComponentType() == inField.getType();
  }

  private static boolean checkIfAnyParameterExists(Method domainMethod) {
    return domainMethod.getParameterTypes().length != 0;
  }

  private static boolean checkIfReturnTypeIsArray(Method domainMethod) {
    return domainMethod.getReturnType().isArray();
  }

  private static boolean validateDomainMethod(Field inField,
      Method domainMethod) {
    boolean ret;
    ret = JCUnit.checkIfStatic(domainMethod);
    ret &= !JCUnit.checkIfAnyParameterExists(domainMethod);
    ret &= JCUnit.checkIfReturnTypeIsArray(domainMethod);
    ret &= JCUnit.checkIfTypeCompatible(inField, domainMethod);
    return ret;
  }

  public static DomainGenerator domainGenerator(Class<?> cut, Field inField) {
    DomainGenerator ret = null;
    In.Domain inType = inField.getAnnotation(In.class).domain();
    assert inType != null;

    if (inType == In.Domain.Method) {
      final Method m = JCUnit.domainMethod(cut, inField);
      ret = new DomainGenerator() {
        @Override
        public Object[] domain() throws JCUnitCheckedException {
          return CompatUtils.invokeDomainMethod(m);
        }
      };
    } else if (inType == In.Domain.Default) {
      Class<?> inFieldType = inField.getType();
      Object[] tmpvalues = null;
      if (inFieldType == Integer.TYPE || inFieldType == Integer.class) {
        tmpvalues = new Object[] { 1, 0, -1, 100, -100, Integer.MAX_VALUE,
            Integer.MIN_VALUE };
      } else if (inFieldType == Long.TYPE || inFieldType == Long.class) {
        tmpvalues = new Object[] { 1L, 0L, -1L, 100L, -100L, Long.MAX_VALUE,
            Long.MIN_VALUE };
      } else if (inFieldType == Short.TYPE || inFieldType == Short.class) {
        tmpvalues = new Object[] { (short) 1, (short) 0, (short) -1,
            (short) 100, (short) -100, Short.MAX_VALUE, Short.MIN_VALUE };
      } else if (inFieldType == Byte.TYPE || inFieldType == Byte.class) {
        tmpvalues = new Object[] { (byte) 1, (byte) 0, (byte) -1, (byte) 100,
            (byte) -100, Byte.MAX_VALUE, Byte.MIN_VALUE };
      } else if (inFieldType == Float.TYPE || inFieldType == Float.class) {
        tmpvalues = new Object[] { 1.0f, 0f, -1.0f, 100.0f, -100.0f,
            Float.MAX_VALUE, Float.MIN_VALUE };
      } else if (inFieldType == Double.TYPE || inFieldType == Double.class) {
        tmpvalues = new Object[] { 1.0d, 0d, -1.0d, 100.0d, -100.0d,
            Double.MAX_VALUE, Double.MIN_VALUE };
      } else if (inFieldType == Character.TYPE
          || inFieldType == Character.class) {
        tmpvalues = new Object[] { 'a', 'あ', (char) 1, Character.MAX_VALUE,
            Character.MIN_VALUE };
      } else if (inFieldType == Boolean.TYPE || inFieldType == Boolean.class) {
        tmpvalues = new Object[] { true, false };
      } else if (inFieldType == String.class) {
        tmpvalues = new Object[] { "Hello world", "こんにちは世界", "" };
      } else if (Enum.class.isAssignableFrom(inFieldType)) {
        try {
          tmpvalues = (Object[]) inFieldType.getMethod("values").invoke(null);
        } catch (IllegalArgumentException e) {
          Utils.rethrow("Something went wrong.", e);
        } catch (SecurityException e) {
          Utils.rethrow("Something went wrong.", e);
          Utils.rethrow(e);
        } catch (IllegalAccessException e) {
          Utils.rethrow("Something went wrong.", e);
        } catch (InvocationTargetException e) {
          Utils.rethrow("Something went wrong.", e);
        } catch (NoSuchMethodException e) {
          Utils.rethrow("Something went wrong.", e);
        }
      } else {
        String msg = String
            .format(
                "Only primitive type fields and fields whose class are wrappers "
                    + "of primitive types can use 'Default' domain type. (field:%s, type:%s)",
                inField.getName(), inFieldType.getName()
            );
        throw new IllegalArgumentException(msg);
      }
      assert tmpvalues != null;
      // if the field isn't a primitive, null is added as a possible value.
      if (!inFieldType.isPrimitive()
          && inField.getAnnotation(In.class).includeNull()) {
        Object[] values2 = new Object[tmpvalues.length + 1];
        System.arraycopy(tmpvalues, 0, values2, 0, tmpvalues.length);
        values2[tmpvalues.length] = null;
        tmpvalues = values2;
      }
      final Object[] values = tmpvalues;
      ret = new DomainGenerator() {
        @Override
        public Object[] domain() {
          return values;
        }
      };
    } else if (inType == In.Domain.None) {
      ret = new DomainGenerator() {
        @Override
        public Object[] domain() throws JCUnitCheckedException {
          return new Object[] { };
        }
      };
    }
    assert ret != null;
    return ret;
  }

  /**
   * @param generatorClass A generator class to be used for <code>cut</code>
   * @param params         TODO
   * @param domains        Domain definitions for all the fields.
   */
  @SuppressWarnings("unchecked")
  public static TestArrayGenerator<Field> newTestArrayGenerator(
      @SuppressWarnings("rawtypes")
      Class<? extends TestArrayGenerator> generatorClass,
      GeneratorParameters.Value[] params,
      LinkedHashMap<Field, Object[]> domains) {
    TestArrayGenerator<Field> ret;
    try {
      ret = (TestArrayGenerator<Field>) generatorClass.newInstance();
      ret.init(params, domains);
    } catch (InstantiationException e) {
      throw new JCUnitPluginException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new JCUnitPluginException(e.getMessage(), e);
    }
    return ret;
  }

  /*
     * Composes the test array.
     */
  public List<Object> composeTestArray(
      Class<?> cut,
      @SuppressWarnings("rawtypes")
      Class<? extends TestArrayGenerator> generatorClass)
      throws JCUnitCheckedException {
    if (generatorClass == null) {
      throw new NullPointerException();
    }

    // //
    // Load parameters from class definition. If it isn't present, an empty
    // array will be given.
    GeneratorParameters paramsAnnotation = cut
        .getAnnotation(GeneratorParameters.class);
    GeneratorParameters.Value[] params = new GeneratorParameters.Value[0];
    if (paramsAnnotation != null) {
      params = paramsAnnotation.value();
    }

    // //
    // Initialize the domains for every '@In' annotated field.
    Field[] fields = CompatUtils.getInFieldsFromClassUnderTest(cut);

    // Intentionally using concrete class name 'LinkedHashMap' to express
    // this variable needs to have predictable order in listing keys.
    LinkedHashMap<Field, Object[]> domains = new LinkedHashMap<Field, Object[]>();
    for (Field f : fields) {
      DomainGenerator domainGenerator = JCUnit.domainGenerator(cut, f);
      if (domainGenerator != null) {
        Object[] domain = domainGenerator.domain();
        domains.put(f, domain);
      }
    }

    // //
    // Instantiates the test array generator.
    TestArrayGenerator<Field> testArrayGenerator = JCUnit
        .newTestArrayGenerator(generatorClass, params, domains);

    // //
    // Compose an array to be returned to the caller.
    List<Object> ret = new ArrayList<Object>();
    for (Map<Field, Object> pattern : testArrayGenerator) {
      ret.add(pattern);
    }

    // //
    // Print the test array definition.
    reportTestArray(cut, testArrayGenerator);
    return ret;
  }

  private void reportTestArray(Class<?> targetClass,
      TestArrayGenerator<Field> testArrayGenerator) {
    initClassLevelReport(targetClass);
    writeMatrixHeader(targetClass);
    writeDomains(targetClass, testArrayGenerator);
    writeMatrix(targetClass, testArrayGenerator);
  }

  /**
   * A report writer object.
   */
  private static ReportWriter writer = new ReportWriter();

  /* DONE */
  protected void initClassLevelReport(Class<?> targetClass) {
    writer.deleteReport(targetClass);
  }

  /* DONE */
  private void writeMatrixHeader(Class<?> targetClass) {
    writer.writeLine(targetClass, 0,
        "***********************************************");
    writer.writeLine(targetClass, 0,
        "***                                         ***");
    writer.writeLine(targetClass, 0,
        "***          T E S T   M A T R I X          ***");
    writer.writeLine(targetClass, 0,
        "***                                         ***");
    writer.writeLine(targetClass, 0,
        "***********************************************");
    writer.writeLine(targetClass, 0, "");
  }

  /* DONE */
  protected static void writeDomains(Class<?> targetClass,
      TestArrayGenerator<Field> testArrayGenerator) {
    writer.writeLine(targetClass, 0, "* DOMAINS *");
    char keyCode = 'A';
    for (Field key : testArrayGenerator.getKeys()) {
      // //
      // print out header
      String domainHeader = String.format("%s:%s(%s)", keyCode, key.getName(),
          key.getType());
      writer.writeLine(targetClass, 1, domainHeader);
      Object[] d = testArrayGenerator.getDomain(key);
      for (int i = 0; i < d.length; i++) {
        String l = String.format("%02d:'%s'", i, Basic.tostr(d[i]));
        writer.writeLine(targetClass, 2, l);
      }
      keyCode++;
    }
    writer.writeLine(targetClass, 0, "");
  }

  /* DONE */
  protected static void writeMatrix(Class<?> targetClass,
      TestArrayGenerator<Field> testArrayGenerator) {
    writer.writeLine(targetClass, 0, "* MATRIX *");
    String header = String.format("%22s", "");
    int numKeys = testArrayGenerator.getKeys().size();
    boolean firstTime = true;
    char keyCode = 'A';
    for (int i = 0; i < numKeys; i++) {
      if (firstTime) {
        firstTime = false;
      } else {
        header += ",";
      }
      header += String.format("%-2s", keyCode);
      keyCode++;
    }
    writer.writeLine(targetClass, 0, header);
    long size = testArrayGenerator.size();
    for (int i = 0; i < size; i++) {
      String line = String.format("%-20s", String.format("testrun[%d]:", i));
      firstTime = true;
      for (Field key : testArrayGenerator.getKeys()) {
        int valueCode = testArrayGenerator.getIndex(key, i);
        if (firstTime) {
          firstTime = false;
        } else {
          line += ",";
        }
        line += String.format("%02d", valueCode);
      }
      writer.writeLine(targetClass, 1, line);
    }
    writer.writeLine(targetClass, 0, "");
  }

  @SuppressWarnings("rawtypes")
  private static Class<? extends TestArrayGenerator> getTestArrayGeneratorClass(
      Class<?> cuf) {
    Generator an = cuf.getAnnotation(Generator.class);
    Class<? extends TestArrayGenerator> ret = an != null ? an.value() : null;
    if (ret != null) {
      return ret;
    } else {
      Class<?> superClass = cuf.getSuperclass();
      if (superClass == null) {
        return null;
      }
      return getTestArrayGeneratorClass(superClass);
    }
  }
}
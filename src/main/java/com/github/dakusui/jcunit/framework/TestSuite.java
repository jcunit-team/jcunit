package com.github.dakusui.jcunit.framework;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleImpl;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.StandardCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerBase;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;

import java.lang.reflect.Array;
import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;

/**
 * A class that represents a suite of test cases.
 */
public class TestSuite extends AbstractList<TestCase> {

  private final List<TestCase> testCases;

  /**
   * Creates an object of this class.
   * Users of {@link Builder} do not need to call this constructor by themselves.
   *
   * @param testCases A list of test cases.
   * @see TestSuite.Builder
   */
  public TestSuite(List<? extends TestCase> testCases) {
    this.testCases = Utils.newUnmodifiableList(testCases);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TestCase get(int index) {
    return this.testCases.get(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return this.testCases.size();
  }

  /**
   * Returns a list of constraints which violate a given test case.
   * The argument must be a test case created by a {@link Builder}, otherwise an exception will be
   * thrown.
   */
  public static List<Predicate> getViolatedConstraints(TestCase testCase) {
    checknotnull(testCase);
    checkcond(testCase instanceof Builder.TestCaseWithViolatedConstraints);
    //noinspection ConstantConditions (already checked)
    return ((Builder.TestCaseWithViolatedConstraints)testCase).constraints;
  }

  /**
   * A predicate interface for a {@code Tuple}. Mainly used to define a constraint used by
   * {@link Builder#addConstraint} method.
   *
   * @see Builder#addConstraint(TestSuite.Predicate)
   */
  public static abstract class Predicate implements Utils.Predicate<Tuple> {
    /**
     * A tag to identify this object.
     */
    public final String tag;

    /**
     * Creates an object of this class.
     *
     * @param tag A tag to identify this object.
     */
    public Predicate(String tag) {
      this.tag = checknotnull(tag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return this.tag;
    }
  }

  /**
   * A builder of {@code TestSuite}.
   * If you try to add factors with the same name to an object of this class using {@code addFactor}
   * or {@code addXyzFactor} method multiple times, an exception will be thrown.
   *
   * By default (with the no-parameter constructor) this builder uses {@link StandardCoveringArrayEngine}
   * with strength 2.
   *
   * In order to customize the strength, see {@link Builder#Builder(int)} . To use different type of covering
   * array generator see {@link Builder#Builder(CoveringArrayEngine)}.
   *
   */
  public static class Builder {
    private final List<Factor>        factors;
    private       CoveringArrayEngine coveringArrayEngine;
    private List<Predicate> constraints = new LinkedList<Predicate>();
    private boolean negativeTestsEnabled = false;

    /**
     * Add a constraint to this object.
     *
     * @param constraint A constraint to be added.
     */
    public Builder addConstraint(Predicate constraint) {
      this.constraints.add(checknotnull(constraint));
      return this;
    }

    /**
     * Add a new factor to this object.
     *
     * @param name A name of a new factor.
     * @param levels Levels of the factor.
     */
    public Builder addFactor(final String name, List<?> levels) {
      checknotnull(name);
      checknotnull(levels);
      checkcond(Utils.filter(this.factors, new Utils.Predicate<Factor>() {
            @Override
            public boolean apply(Factor in) {
              return in.name.equals(name);
            }
          }).isEmpty(),
          "A factor '%s' is already added to this object.",
          name
      );
      Factor.Builder b = new Factor.Builder(name);
      for (Object each : levels) {
        b.addLevel(each);
      }
      return this.addFactor(b.build());
    }

    public Builder addFactors(Iterable<Factor> factors) {
      for (Factor eachFactor : factors) {
        this.addFactor(eachFactor);
      }
      return this;
    }

    public Builder addFactor(Factor factor) {
      this.factors.add(factor);
      return this;
    }

    /**
     * Add a new factor to this object.
     *
     * @param name A name of a new factor.
     * @param levels Levels of the factor.
     */
    public Builder addFactor(String name, Object... levels) {
      this.addFactor(name, Arrays.asList(levels));
      return this;
    }

    /**
     * Add a new {@code boolean} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addBooleanFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.booleanLevels()));
      return this;
    }

    /**
     * Add a new {@code byte} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addByteFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.byteLevels()));
      return this;
    }

    /**
     * Add a new {@code char} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addCharFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.charLevels()));
      return this;
    }

    /**
     * Add a new {@code short} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addShortFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.shortLevels()));
      return this;
    }

    /**
     * Add a new {@code int} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addIntFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.intLevels()));
      return this;
    }

    /**
     * Add a new {@code long} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addLongFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.longLevels()));
      return this;
    }

    /**
     * Add a new {@code float} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addFloatFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.doubleLevels()));
      return this;
    }

    /**
     * Add a new {@code double} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addDoubleFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.doubleLevels()));
      return this;
    }

    /**
     * Add a new {@code String} factor with preset levels.
     *
     * @param name A name of a new factor
     */
    public Builder addStringFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.stringLevels()));
      return this;
    }

    /**
     * Add a new {@code enum} factor with preset levels.
     * Levels are provided by {@code enumClass.getEnumConstants()} method.
     *
     * @param name A name of a new factor
     */
    public Builder addEnumLevels(String name, Class<? extends Enum> enumClass) {
      this.addFactor(name, Arrays.asList(enumClass.getEnumConstants()));
      return this;
    }

    /**
     * Enables negative test generation.
     */
    public Builder enableNegativeTests() {
      this.negativeTestsEnabled = true;
      return this;
    }

    /**
     * Disables negative test generation.
     */
    public Builder disableNegativeTests() {
      this.negativeTestsEnabled = false;
      return this;
    }

    /**
     * Builds a new {@code TestSuite} object based on given settings.
     */
    public TestSuite build() {
      FactorSpace.Builder builder = new FactorSpace.Builder();
      builder.addFactorDefs(Utils.transform(this.factors, new Utils.Form<Factor, FactorDef>() {
        @Override
        public FactorDef apply(final Factor in) {
          return new FactorDef.Simple(in.name, new LevelsProvider() {
            @Override
            public int size() {
              return in.levels.size();
            }

            @Override
            public Object get(int n) {
              return in.levels.get(n);
            }
          });
        }
      }));
      ConstraintChecker checker = new SmartConstraintCheckerBase(new Factors(factors)) {
        @Override
        protected List<Constraint> getConstraints() {
          return Utils.transform(
              constraints,
              new Utils.Form<Predicate, Constraint>() {
                @Override
                public Constraint apply(Predicate in) {
                  return new GuardedConstraint(in.tag, in);
                }
              }
          );
        }
      };
      builder.setTopLevelConstraintChecker(checker);
      List<TestCase> testCases = new LinkedList<TestCase>();
      testCases.addAll(Utils.transform(
          this.coveringArrayEngine.generate(builder.build()),
          new Utils.Form<Tuple, TestCase>() {
            @Override
            public TestCase apply(final Tuple in) {
              checkcond(
                  Utils.filter(
                      constraints,
                      new Utils.Predicate<Predicate>() {
                        @Override
                        public boolean apply(Predicate constraint) {
                          return !constraint.apply(new GuardedTuple(in));
                        }
                      }
                  ).isEmpty(),
                  "The covering array engine (%s) in use does not respect constraints.",
                  coveringArrayEngine.getClass().getCanonicalName()
              );
              return new TestCase(TestCase.Type.REGULAR, in);
            }
          }
      ));
      if (negativeTestsEnabled) {
        testCases.addAll(Utils.transform(
            checker.getViolations(),
            new Utils.Form<Tuple, TestCase>() {
              @Override
              public TestCase apply(final Tuple in) {
                return new TestCaseWithViolatedConstraints(
                    TestCase.Type.VIOLATION,
                    in,
                    Utils.filter(
                        constraints,
                        new Utils.Predicate<Predicate>() {
                          @Override
                          public boolean apply(Predicate constraint) {
                            return !constraint.apply(in);
                          }
                        }
                    )
                );
              }
            }
        ));
      }
      return new TestSuite(testCases);
    }

    /**
     * Creates an object of this class.
     *
     * @param engine An engine with which a new test suite will be created.
     */
    public Builder(CoveringArrayEngine engine) {
      this.coveringArrayEngine = checknotnull(engine);
      this.factors = new LinkedList<Factor>();
    }

    /**
     * Creates an object of this class.
     *
     * @param strength A combinatorial strength passed to a default covering array generation engine.
     */
    public Builder(int strength) {
      this(new StandardCoveringArrayEngine(strength));
    }

    /**
     * Creates an object of this class.
     */
    public Builder() {
      this(2);
    }

    private List toList(final Object primitiveArray) {
      return new AbstractList() {
        @Override
        public Object get(int index) {
          return Array.get(primitiveArray, index);
        }

        @Override
        public int size() {
          return Array.getLength(primitiveArray);
        }
      };
    }

    static class TestCaseWithViolatedConstraints extends TestCase {
      private final List<Predicate> constraints;

      protected TestCaseWithViolatedConstraints(Type type, Tuple tuple, List<Predicate> constraints) {
        super(type, tuple);
        this.constraints = Collections.unmodifiableList(checknotnull(constraints));
      }
    }

    static class GuardedConstraint implements Constraint {
      private final Utils.Predicate<Tuple> predicate;
      private final String                 tag;

      GuardedConstraint(String tag, Utils.Predicate<Tuple> base) {
        this.predicate = checknotnull(base);
        this.tag = checknotnull(tag);
      }

      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        try {
          return this.predicate.apply(new GuardedTuple(tuple));
        } catch (Undef e) {
          throw e.toUndefinedSymbol();
        }
      }

      @Override
      public String tag() {
        return this.tag;
      }
    }

    static class GuardedTuple extends TupleImpl {
      GuardedTuple(Tuple tuple) {
        this.putAll(checknotnull(tuple));
      }

      @Override
      public Object get(Object key) {
        Checks.checkcond(key instanceof String);
        if (!containsKey(key))
          // already checked.
          //noinspection ConstantConditions
          throw new Undef((String) key);
        return super.get(key);
      }
    }

    private static class Undef extends JCUnitException {
      private final String factorName;

      /**
       * Creates an object of this class.
       *
       * @param factorName An error message for this object.
       */
      private Undef(String factorName) {
        super(null, null);
        this.factorName = checknotnull(factorName);
      }

      UndefinedSymbol toUndefinedSymbol() {
        return new UndefinedSymbol(factorName);
      }
    }
  }
}
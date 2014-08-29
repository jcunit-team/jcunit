package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.FactorField;

/**
 */
public class TestFactors {
  @SuppressWarnings("unused") // used through reflection
	@FactorField
	public int validIntFieldWithDefaultValues;
  @SuppressWarnings("unused") // used through reflection
	@FactorField(intLevels = {1, 2, 3})
	public int validIntFieldWithExplicitIntValues;
  @SuppressWarnings("unused") // used through reflection
	@FactorField(longLevels = {1, 2, 3})
	public int invalidIntFieldWithExplicitLongValues;
}
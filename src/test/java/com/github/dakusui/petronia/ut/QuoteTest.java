package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.lisj.Basic;

public class QuoteTest extends DefaultRuleSetBuilder {
  @Test
  public void quote_00() throws Exception {
    assertArrayEquals(new Object[] {}, (Object[]) (Basic.eval(this, quote())));
  }

  @Test
  public void quote_01() throws Exception {
    assertArrayEquals(new Object[] { 1 }, (Object[]) Basic.eval(this, quote(1)));
  }

  @Test
  public void quote_02() throws Exception {
    assertArrayEquals(new Object[] { 1, 2 },
        (Object[]) (Basic.eval(this, quote(1, 2))));
  }

  @Test
  public void quote_03() throws Exception {
    assertArrayEquals(new Object[] { 1, 2, 3 },
        (Object[]) (Basic.eval(this, quote(1, 2, 3))));
  }

  @Test
  public void q_03() throws Exception {
    assertArrayEquals(new Object[] { 1, 2, 3 },
        (Object[]) (Basic.eval(this, q(1, 2, 3))));
  }
}

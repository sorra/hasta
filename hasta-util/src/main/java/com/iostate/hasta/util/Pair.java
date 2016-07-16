package com.iostate.hasta.util;

import java.util.Objects;

/**
 * A tuple of two elements
 * @param <A> first element
 * @param <B> second element
 */
public class Pair<A, B> {
  private A a;
  private B b;

  public Pair(A a, B b) {
    Objects.requireNonNull(a);
    Objects.requireNonNull(b);
    this.a = a;
    this.b = b;
  }

  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<>(a, b);
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pair<?, ?> pair = (Pair<?, ?>) o;

    if (!a.equals(pair.a)) return false;
    return b.equals(pair.b);

  }

  @Override
  public int hashCode() {
    int result = a.hashCode();
    result = 31 * result + b.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Pair{" +
        "a=" + a +
        ", b=" + b +
        '}';
  }
}

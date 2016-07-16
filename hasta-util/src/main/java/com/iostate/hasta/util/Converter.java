package com.iostate.hasta.util;

/**
 * Defines how to convert an instance of AClass to an instance of BClass.
 */
public interface Converter {
  Object convert(Object from);
}

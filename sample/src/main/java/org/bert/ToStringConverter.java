package org.bert;

public interface ToStringConverter<T> {

    String toString(T instance);

    T fromString(String value);

}

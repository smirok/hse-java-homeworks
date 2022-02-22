package ru.hse.java.implementor.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodUtils {
    public static String signatureToString(Method method) {
        return method.getName() + Arrays.toString(method.getParameterTypes());
    }
}

package ru.hse.java.implementor;

import ru.hse.java.implementor.util.MethodUtils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public class ImplementorImpl implements Implementor {
    private static final String FOUR_SPACES = "    ";
    private final String outputDirectory;
    private final Set<String> importedClasses = new HashSet<>();
    private boolean isClassFromDirectory;

    public ImplementorImpl(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    private Class<?> loadClass(String directoryPath, String className) throws ImplementorException {
        Class<?> loadedClass;

        try (URLClassLoader ucl = new URLClassLoader(new URL[]{new File(directoryPath).toURI().toURL()})) {
            loadedClass = ucl.loadClass(className);
        } catch (IOException malformedURLException) {
            throw new ImplementorException("URL class loader close error", malformedURLException);
        } catch (ClassNotFoundException classNotFoundException) {
            throw new ImplementorException("Class " + className + " not found!", classNotFoundException);
        }

        return loadedClass;
    }

    private File createNewFile(Class<?> loadedClass) throws ImplementorException {
        String filePathWithoutImplJava = isClassFromDirectory
                ? String.join(File.separator, loadedClass.getCanonicalName().split("\\."))
                : loadedClass.getSimpleName();

        File file;
        file = new File(outputDirectory + File.separator + filePathWithoutImplJava + "Impl.java");

        try {
            if (isClassFromDirectory) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        } catch (IOException | SecurityException exception) {
            throw new ImplementorException("Cannot create a file", exception);
        }

        return file;
    }

    private String generateClass(Class<?> loadedClass) throws ImplementorException {
        StringBuilder classBuilder = new StringBuilder();

        int modifier = loadedClass.getModifiers();
        if (Modifier.isFinal(modifier) || Modifier.isPrivate(modifier) || loadedClass.equals(Enum.class)) {
            throw new ImplementorException("Cannot inherit from that class");
        }

        String generatedPackage = generatePackage(loadedClass);
        String generatedClassDefinition = generateClassDefinition(loadedClass);
        String generatedConstructors = generateConstructors(loadedClass);
        String generatedMethods = generateMethods(loadedClass);
        String generatedImports = generateImports();

        if (isClassFromDirectory) {
            classBuilder
                    .append(generatedPackage)
                    .append('\n');
        }

        classBuilder.append(generatedImports)
                .append('\n')
                .append(generatedClassDefinition)
                .append(" {\n\n")
                .append(generatedConstructors)
                .append(generatedMethods)
                .append('}');

        return classBuilder.toString();
    }

    private String generateImports() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String className : importedClasses) {
            stringBuilder
                    .append("import ")
                    .append(className)
                    .append(";\n");
        }

        return stringBuilder.toString();
    }

    private String generatePackage(Class<?> loadedClass) {
        String packageName = loadedClass.getPackage().getName();
        return packageName.length() > 0 ? "package " + packageName + ";" : "";
    }

    private String generateMethods(Class<?> loadedClass) {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, List<Method>> methods = new HashMap<>();
        Map<String, List<Method>> bannedMethods = new HashMap<>();

        for (Class<?> currentClass = loadedClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
            for (Method method : currentClass.getDeclaredMethods()) {
                processMethod(method, methods, bannedMethods);
            }
        }

        for (Method method : loadedClass.getMethods()) {
            processMethod(method, methods, bannedMethods);
        }

        for (List<Method> methodList : methods.values()) {
            for (Method method : methodList) {
                stringBuilder
                        .append(generateMethod(method))
                        .append("\n\n");
            }
        }

        return stringBuilder.toString();
    }

    private void processMethod(Method method,
                               Map<String, List<Method>> methods,
                               Map<String, List<Method>> bannedMethods) {
        String signature = MethodUtils.signatureToString(method);
        if (Modifier.isAbstract(method.getModifiers())) {
            if (shouldImplementMethod(method, methods, bannedMethods)) {
                methods.computeIfAbsent(signature, s -> new ArrayList<>()).add(method);
            }
        } else {
            bannedMethods.computeIfAbsent(signature, s -> new ArrayList<>()).add(method);
        }
    }

    private boolean shouldImplementMethod(Method method,
                                          Map<String, List<Method>> methods,
                                          Map<String, List<Method>> bannedMethods) {
        String methodSignature = MethodUtils.signatureToString(method);
        if (methods.containsKey(methodSignature)) {
            List<Method> possibleEqualMethods = methods.get(methodSignature);

            for (Method currentMethod : possibleEqualMethods) {
                boolean isReturnTypeIsSuperClass = method.getReturnType().isAssignableFrom(currentMethod.getReturnType());
                boolean isReturnTypeIsExtendClass = currentMethod.getReturnType().isAssignableFrom(method.getReturnType());

                if (method.getReturnType().isPrimitive() || isReturnTypeIsSuperClass) {
                    return false;
                } else if (isReturnTypeIsExtendClass) {
                    possibleEqualMethods.remove(currentMethod);
                    String signatureCurrentMethod = MethodUtils.signatureToString(currentMethod);
                    bannedMethods.computeIfAbsent(signatureCurrentMethod, s -> new ArrayList<>()).add(currentMethod);
                    return true;
                }
            }
        }

        if (bannedMethods.containsKey(methodSignature)) {
            List<Method> methodList = bannedMethods.get(methodSignature);
            for (Method curMethod : methodList) {
                boolean isReturnTypeIsSuperClass = method.getReturnType().isAssignableFrom(curMethod.getReturnType());

                if (method.getReturnType().isPrimitive() || isReturnTypeIsSuperClass) {
                    return false;
                }
            }

        }
        return true;
    }

    private String generateParameters(Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            if (!importedClasses.contains(parameter.getType().getCanonicalName())) {
                Class<?> type = parameter.getType();
                while (type.isArray()) {
                    type = type.getComponentType();
                }
                if (!type.isPrimitive()) {
                    importedClasses.add(type.getCanonicalName());
                }
            }
        }

        return Arrays.stream(parameters)
                .map(parameter -> parameter.getType().getSimpleName() + ' ' + parameter.getName())
                .collect(Collectors.joining(", ", "(", ")"));
    }

    private String generateMethod(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(FOUR_SPACES)
                .append(Modifier.toString(
                        method.getModifiers())
                        .replace("abstract", "")
                        .replace("native", "")
                        .replace("strict", "")
                        .replace("synchronized", "")
                        .replace("transient", "")
                        .replace("volatile", "")
                )
                .append(' ')
                .append(method.getReturnType().getCanonicalName())
                .append(' ')
                .append(method.getName());

        String stringParams = generateParameters(method.getParameters());

        stringBuilder
                .append(stringParams)
                .append(" {\n");

        Class<?> returnType = method.getReturnType();

        stringBuilder
                .append(FOUR_SPACES)
                .append(FOUR_SPACES);

        if (!returnType.isPrimitive()) {
            stringBuilder.append("return null;\n");
        } else {
            switch (returnType.getName()) {
                case "void":
                    stringBuilder.append("return;\n");
                    break;
                case "boolean":
                    stringBuilder.append("return false;\n");
                    break;
                case "float":
                    stringBuilder.append("return 0.0f;\n");
                    break;
                case "double":
                    stringBuilder.append("return 0.0;\n");
                    break;
                default:
                    stringBuilder.append("return 0;\n");
                    break;
            }
        }

        stringBuilder
                .append(FOUR_SPACES)
                .append("}");

        return stringBuilder.toString();
    }

    private String generateClassDefinition(Class<?> loadedClass) {
        return "public class " +
                loadedClass.getSimpleName() +
                "Impl" +
                (loadedClass.isInterface() ? " implements " : " extends ") +
                loadedClass.getCanonicalName();
    }

    private String generateConstructors(Class<?> loadedClass) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Constructor<?> constructor : loadedClass.getDeclaredConstructors()) {
            if (constructor.getParameterCount() > 0) {
                stringBuilder
                        .append(generateConstructor(constructor))
                        .append("\n\n");
            }
        }

        return stringBuilder.toString();
    }

    private String generateSuperConstructorCall(Parameter[] parameters) {
        return "super" + Arrays.stream(parameters)
                .map(Parameter::getName)
                .collect(Collectors.joining(", ", "(", ")")) + ';';
    }

    private String generateConstructor(Constructor<?> constructor) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(FOUR_SPACES)
                .append(Modifier.toString(constructor.getModifiers()))
                .append(' ')
                .append(constructor.getDeclaringClass().getSimpleName())
                .append("Impl");

        String stringParams = generateParameters(constructor.getParameters());
        stringBuilder
                .append(stringParams);

        String callingSuperConstructor = generateSuperConstructorCall(constructor.getParameters());

        if (constructor.getExceptionTypes().length > 0) {
            stringBuilder.append(" throws ").append(
                    Arrays.stream(constructor.getExceptionTypes())
                            .map(Class::getName)
                            .collect(Collectors.joining(", "))
            );
        }

        stringBuilder
                .append(" {\n")
                .append(FOUR_SPACES)
                .append(FOUR_SPACES)
                .append(callingSuperConstructor)
                .append('\n')
                .append(FOUR_SPACES)
                .append("}");

        return stringBuilder.toString();
    }

    private void writeClassToFile(String generatedClassString, Class<?> loadedClass) throws ImplementorException {
        File newFile = createNewFile(loadedClass);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(newFile)))) {
            writer.write(generatedClassString);
        } catch (FileNotFoundException ignored) {

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public String implementFromDirectory(String directoryPath, String className) throws ImplementorException {
        isClassFromDirectory = true;

        Class<?> loadedClass = loadClass(directoryPath, className);

        String generatedClass = generateClass(loadedClass);
        writeClassToFile(generatedClass, loadedClass);

        String packagePrefix = loadedClass.getPackage().getName() + '.';

        if (packagePrefix.equals(".")) {
            return loadedClass.getSimpleName() + "Impl";
        } else {
            return packagePrefix + loadedClass.getSimpleName() + "Impl";
        }
    }

    @Override
    public String implementFromStandardLibrary(String className) throws ImplementorException {
        isClassFromDirectory = false;

        Class<?> loadedClass;
        try {
            loadedClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ImplementorException("Class " + className + " not found!", e);
        }

        String generatedClass = generateClass(loadedClass);
        writeClassToFile(generatedClass, loadedClass);

        return loadedClass.getSimpleName() + "Impl";
    }

    public static void main(String[] args) throws ImplementorException {
        ImplementorImpl implementor = new ImplementorImpl("check/outputDEV");
        implementor.implementFromDirectory(
                "build/classes/java/main", "test.SameMethodsDifferentReturnTypes2"
        );
    }
}

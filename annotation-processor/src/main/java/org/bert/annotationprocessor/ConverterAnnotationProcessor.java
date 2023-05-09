package org.bert.annotationprocessor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("org.bert.annotation.PersistAsId")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class ConverterAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            annotatedElements.forEach(element -> {
                try {
                    JavaFile converter = generateConverter(element);
                    if (converter.toJavaFileObject().delete()) {
                        System.out.println("Deleted previously generated file");
                    }
                    converter.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    e.printStackTrace();
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Could not generate Converter", element);
                }
            });
        }
        return true;
    }

    private JavaFile generateConverter(Element mainType) {
        var elementTypeName = mainType.toString();
        var mainTypeName = ClassName.get(mainType.asType());
        MethodSpec fromString = MethodSpec.methodBuilder("fromString")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(mainTypeName)
                .addParameter(String.class, "s")
                .addStatement("return $T.fromString()", mainTypeName)
                .build();
        MethodSpec toString = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String.class)
                .addParameter(mainTypeName, "t")
                .addStatement("return t == null ? null : t.toString()")
                .build();

        var toStringConverterType = ParameterizedTypeName.get(ClassName.get("org.bert", "ToStringConverter"), mainTypeName);
        TypeSpec helloWorld = TypeSpec.classBuilder(mainType.getSimpleName() + "Converter")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(toStringConverterType)
                .addMethod(fromString)
                .addMethod(toString)
                .build();

        String packageName = elementTypeName.substring(0, elementTypeName.lastIndexOf('.'));
        return JavaFile.builder(packageName, helloWorld).build();
    }
}

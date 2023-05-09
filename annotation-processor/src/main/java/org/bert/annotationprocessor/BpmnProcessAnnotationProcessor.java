package org.bert.annotationprocessor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import org.w3c.dom.Document;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@SupportedAnnotationTypes("org.bert.annotation.BpmnProcess")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class BpmnProcessAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
                annotatedElement.getAnnotationMirrors().stream().map(this::getBpmnProcess).filter(Objects::nonNull).findFirst().ifPresent(p -> {
                    FileObject fileObject = processingEnv.getFiler()
                            .getResource(StandardLocation.CLASS_OUTPUT, "", p);
                    DocumentBuilder db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
                    var bpmn = db.parse(fileObject.openInputStream());
                });
            }
        }
        return true;
    }

    private JavaFile generateConverter(Element mainType, Document bpmn) {
        var xpath = XPathFactory.newDefaultInstance().newXPath();
        xpath.compile("/bpmn:process/@id");
        return null;
    }


    private String getBpmnProcess(AnnotationMirror mirror) {
        if (mirror.getAnnotationType().toString().equals("org.bert.annotation.BpmnProcess")) {
            return mirror.getElementValues().values().iterator().next().getValue().toString();
        }
        return null;
    }
}
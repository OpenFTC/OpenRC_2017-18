package org.openftc;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({"com.qualcomm.robotcore.eventloop.opmode.TeleOp", "com.qualcomm.robotcore.eventloop.opmode.Autonomous"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DuplicateOpModeProcessor extends AbstractProcessor {
    private List<String> opModeNames;

    public DuplicateOpModeProcessor() {
        opModeNames = new ArrayList<>();
    }

    private void processOpModeName(String name) {
        if (opModeNames.contains(name)) {
            throw new RuntimeException("Multiple OpModes with the same name: '" + name + "'");
        } else {
            opModeNames.add(name);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(TeleOp.class)) {
            String name = annotatedElement.getAnnotation(TeleOp.class).name();
            processOpModeName(name.length() == 0 ? annotatedElement.getSimpleName().toString() : name);
        }

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(Autonomous.class)) {
            String name = annotatedElement.getAnnotation(Autonomous.class).name();
            processOpModeName(name.length() == 0 ? annotatedElement.getSimpleName().toString() : name);
        }

        return true;
    }
}

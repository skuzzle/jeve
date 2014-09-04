package de.skuzzle.jeve.annotation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;

/**
 * Processes classes which are marked with {@link ListenerInterface} annotation and checks
 * whether they adhere to the general contract of {@link Listener} implementations.
 *
 * @author Simon Taddiken
 */
@SupportedAnnotationTypes("de.skuzzle.jeve.annotation.ListenerInterface")
public class ListenerAnnotationProcessor extends AbstractProcessor {

    private static final String EXPECTED_TYPE =
            "Listening method '%s' must return %s in order to conform to ListenerKind %s";

    private static final String ILLEGAL_PARAMETER =
            "Listening method '%s' must have a single parameter of type 'Event'";

    private static final String ILLEGAL_EXCEPTION =
            "Listening method '%s' can not throw checked exception";

    private static final String EMPTY_LISTENER =
            "Listener '%s' does not declare any listening methods";

    private static final String INTERFACE_ONLY =
            "@ListenerInterface only supported on interface types";

    private static final String MISSING_INHERITANCE =
            "@ListenerInterface '%s' must extend de.skuzzle.jeve.Listener";

    private static final String TAGGING_NOT_EMPTY = "Tagging listeners must be empty";



    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }



    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {

        final Messager msg = this.processingEnv.getMessager();

        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(
                ListenerInterface.class);

        for (final Element parent : elements) {
            if (parent.getKind() != ElementKind.INTERFACE) {
                msg.printMessage(Kind.ERROR, INTERFACE_ONLY, parent);
            }

            checkInheritance(parent);

            final ListenerInterface anno = parent.getAnnotation(ListenerInterface.class);
            final ListenerKind kind = anno.value();

            final List<ExecutableElement> members = parent.getEnclosedElements().stream()
                    .filter(m -> m instanceof ExecutableElement)
                    .map(m -> (ExecutableElement) m)
                    .collect(Collectors.toList());

            if (kind == ListenerKind.TAGGING) {
                if (!members.isEmpty()) {
                    msg.printMessage(Kind.ERROR, TAGGING_NOT_EMPTY, parent);
                }
                continue;
            } else if (members.isEmpty()) {
                msg.printMessage(Kind.WARNING, EMPTY_LISTENER, parent);
                continue;
            }

            for (final ExecutableElement member : members) {
                checkReturnValue(member, kind);
                checkParameter(member);
                checkThrown(member);
            }
        }

        return true;
    }



    private void checkReturnValue(ExecutableElement member, ListenerKind expectedKind) {
        final Types types = this.processingEnv.getTypeUtils();
        final Messager msg = this.processingEnv.getMessager();

        final TypeMirror voidPrim = types.getNoType(TypeKind.VOID);
        final TypeMirror ret = member.getReturnType();

        switch (expectedKind) {
        case NORMAL:
            if (!types.isSameType(ret, voidPrim)) {
                msg.printMessage(Kind.ERROR, String.format(EXPECTED_TYPE,
                        member.getSimpleName(), "'void'", expectedKind), member);
            }
            break;
        case TAGGING:
            assert false : "should not be reachable";
            break;
        }
    }



    private void checkInheritance(Element parent) {
        final Messager msg = this.processingEnv.getMessager();
        final Types types = this.processingEnv.getTypeUtils();
        final Elements elements = this.processingEnv.getElementUtils();

        final TypeMirror listenerType = elements.getTypeElement(
                Listener.class.getName()).asType();
        if (!types.isSubtype(parent.asType(), listenerType)) {
            msg.printMessage(Kind.ERROR,
                    String.format(MISSING_INHERITANCE, parent.getSimpleName()), parent);
        }
    }



    private void checkParameter(ExecutableElement member) {
        final Types types = this.processingEnv.getTypeUtils();
        final Messager msg = this.processingEnv.getMessager();


        final List<? extends VariableElement> params = member.getParameters();

        if (params.size() != 1) {
            msg.printMessage(Kind.ERROR, String.format(ILLEGAL_PARAMETER,
                    member.getSimpleName()), member);
            return;
        }

        final Elements elements = this.processingEnv.getElementUtils();
        final VariableElement param = params.iterator().next();
        final TypeElement eventType = elements.getTypeElement(Event.class.getName());

        final TypeElement listenerType = elements.getTypeElement(
                Listener.class.getName());
        final TypeMirror listenerMirror = types.getDeclaredType(listenerType);
        final TypeMirror eType = types.getDeclaredType(eventType,
                types.getWildcardType(null, null),
                types.getWildcardType(listenerMirror, null));
        if (!types.isSubtype(param.asType(), eType)) {
            msg.printMessage(Kind.ERROR, String.format(ILLEGAL_PARAMETER,
                    member.getSimpleName()), param);
        }
    }



    private void checkThrown(ExecutableElement member) {
        final Types types = this.processingEnv.getTypeUtils();
        final Messager msg = this.processingEnv.getMessager();

        final List<? extends TypeMirror> thrown = member.getThrownTypes();
        final TypeMirror runtimeEx = this.processingEnv.getElementUtils().getTypeElement(
                RuntimeException.class.getName()).asType();

        for (final TypeMirror tm : thrown) {
            if (!types.isSubtype(tm, runtimeEx)) {
                msg.printMessage(Kind.ERROR,
                        String.format(ILLEGAL_EXCEPTION, member.getSimpleName()), member);

                // only check one exception
                return;
            }
        }
    }
}

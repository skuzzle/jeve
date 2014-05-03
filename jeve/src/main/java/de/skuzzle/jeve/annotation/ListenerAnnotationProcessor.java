package de.skuzzle.jeve.annotation;

import java.util.List;
import java.util.Set;

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
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;

@SupportedAnnotationTypes("de.skuzzle.jeve.annotation.ListenerInterface")
public class ListenerAnnotationProcessor extends AbstractProcessor {

    private final static String EXPECTED_TYPE = 
            "Listening method '%s' must return %s in order to conform to ListenerKind %s";
    
    private final static String ILLEGAL_PARAMETER = 
            "Listening method '%s' must have a single parameter of type 'Event'";
    
    private final static String ILLEGAL_EXCEPTION = 
            "Listening method '%s' can not throw checked exception";
    
    private final static String EMPTY_LISTENER = 
            "Listener '%s' does not declare any listening methods";
    
    private final static String INTERFACE_ONLY = 
            "@ListenerInterface only supported on interface types";
    
    private final static String MISSING_INHERITANCE =
            "@ListenerInterface '%s' must extend de.skuzzle.jeve.Listener";
    
    
    
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
            
            this.checkInheritance(parent);
            
            final ListenerInterface anno = parent.getAnnotation(ListenerInterface.class);
            final ListenerKind kind = anno.value();
            
            
        
            final List<ExecutableElement> members = ElementFilter.methodsIn(
                    parent.getEnclosedElements());

            if (kind == ListenerKind.TAGGING) {
                continue;
            } else if (members.isEmpty()) {
                msg.printMessage(Kind.WARNING, EMPTY_LISTENER, parent);
                continue;
            }
            
            for (final ExecutableElement member : members) {
                this.checkReturnValue(member, kind);
                this.checkParameter(member);
                this.checkThrown(member);
            }
        }
        
        return true;
    }
    
    
    
    private void checkReturnValue(ExecutableElement member, ListenerKind expectedKind) {
        final Types types = this.processingEnv.getTypeUtils();
        final Messager msg = this.processingEnv.getMessager();
        
        final PrimitiveType boolPrim = types.getPrimitiveType(TypeKind.BOOLEAN);
        final TypeMirror boolBox = types.boxedClass(boolPrim).asType();
        final TypeMirror voidPrim = types.getNoType(TypeKind.VOID);
        final TypeMirror ret = member.getReturnType();
        
        switch (expectedKind) {
        case NORMAL:
            if (!types.isSameType(ret, voidPrim)) {
                msg.printMessage(Kind.ERROR, String.format(EXPECTED_TYPE, 
                        member.getSimpleName(), "'void'", expectedKind), member);
            }
            break;
        case ABORTABLE:
            if (!(types.isSameType(ret, boolBox) || types.isSameType(ret, boolPrim))) {
                msg.printMessage(Kind.ERROR, String.format(EXPECTED_TYPE, 
                        member.getSimpleName(), "'boolean' or 'Boolean'", expectedKind),
                        member);
            }
            break;
        case MIXED:
            if (!(types.isSameType(ret, voidPrim) || types.isSameType(ret, boolBox) || 
                    types.isSameType(ret, boolPrim))) {
                msg.printMessage(Kind.ERROR, String.format(EXPECTED_TYPE, 
                        member.getSimpleName(), "''void', boolean' or 'Boolean'", 
                        expectedKind), member);
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
        
        final VariableElement param = params.iterator().next();
        final TypeElement eventType = processingEnv.getElementUtils().getTypeElement(
                Event.class.getName());

        final TypeMirror eType = types.getDeclaredType(eventType, 
                types.getWildcardType(null, null));
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

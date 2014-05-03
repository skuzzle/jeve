package de.skuzzle.jeve.annotation;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import de.skuzzle.jeve.Event;

@SupportedAnnotationTypes("de.skuzzle.jeve.annotation.ListenerInterface")
public class MyAnnotationProcessor extends AbstractProcessor {

    private final static String EXPECTED_TYPE = 
            "Listening method '%s' must return %s in order to conform to ListenerKind %s";
    
    private final static String ILLEGAL_PARAMETER = 
            "Listening method '%s' must have a single parameter of type 'Event'";
    
    private final static String ILLEGAL_EXCEPTION = 
            "Listening method '%s' can not throw checked exception";
    
    
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
    
    
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        
        for (final TypeElement te : annotations) {
            
            final ListenerInterface annotation = (ListenerInterface) te;
            final ListenerKind kind = annotation.kind();
            
            for (final Element element : te.getEnclosedElements()) {
                
                final List<? extends Element> members = element.getEnclosedElements();
                for (final ExecutableElement member : ElementFilter.methodsIn(members)) {
                    this.checkReturnValue(member, kind);
                    this.checkParameter(member);
                    this.checkThrown(member);
                }
            }
        }
        
        return true;
    }
    
    
    
    private void checkReturnValue(ExecutableElement member, ListenerKind expectedKind) {
        final Types types = this.processingEnv.getTypeUtils();
        final Messager msg = this.processingEnv.getMessager();
        
        final PrimitiveType boolPrim = types.getPrimitiveType(TypeKind.BOOLEAN);
        final TypeMirror boolBox = types.boxedClass(boolPrim).asType();
        final TypeMirror voidPrim = types.getPrimitiveType(TypeKind.VOID);
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
        final TypeMirror eventType = processingEnv.getElementUtils().getTypeElement(
                Event.class.getName()).asType();
        
        if (types.isSubtype(param.asType(), eventType)) {
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

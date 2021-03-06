package de.skuzzle.jeve;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

import de.skuzzle.jeve.annotation.ListenerAnnotationProcessor;


public class ListenerAnnotationProcessorIT {
    
    
    
    protected void compileWithoutError(String listener) {
        final String path = getResourcePath(listener);
        ASSERT.about(javaSource())
            .that(JavaFileObjects.forResource(path))
            .processedWith(new ListenerAnnotationProcessor())
            .compilesWithoutError();
    }
    
    
    
    protected void compileWithError(String listener, String errorPart, int line) {
        final String path = getResourcePath(listener);
        final JavaFileObject fileObject = JavaFileObjects.forResource(path);
        ASSERT.about(javaSource())
            .that(fileObject)
            .processedWith(new ListenerAnnotationProcessor())
            .failsToCompile().withErrorContaining(errorPart).in(fileObject).onLine(line);
    }
    
    
    
    protected void testFailReturn(String listener) {
        this.compileWithError(listener, "must return", 10);
    }
    
    protected void testFailParameter(String listener) {
        this.compileWithError(listener, "must have a single ", 10);
    }
    
    protected void testFailException(String listener) {
        this.compileWithError(listener, "can not throw", 11);
    }

    protected String getResourcePath(String listener) {
        return "listeners/" + listener + ".java";
    }
    
    
    
    // BEGIN CLASS LEVEL TESTS
    
    
    
    @Test
    public void testAnnotationOnClassFail() {
        compileWithError("ListenerFailNoInterface", "only supported on interface", 9);
    }
    
    @Test
    public void testFailNoInheritance() {
        compileWithError("ListenerFailNoInheritance", "must extend", 9);
    }
    
    
    
    
    // ENDCLASS LEVEL TESTS    
    
    // BEGIN NORMAL TESTS
    
    
    
    @Test
    public void testNormalSuccess() {
        this.compileWithoutError("NormalListener");
    }
    
    @Test
    public void testNormalFailReturn() {
        testFailReturn("NormalListenerFailReturnType");
    }
    
    @Test
    public void testNormalFailParameter() {
        testFailParameter("NormalListenerFailParameterType");
    }
    
    @Test
    public void testNormalFailException() {
        testFailException("NormalListenerFailException");
    }
    
    
    
    // END NORMAL TESTS
    
    // BEGIN TAGGING TESTS
    
    
    
    @Test
    public void testTaggingSuccess() {
        this.compileWithoutError("TaggingListener");
    }
    
    @Test
    public void testTaggingNotEmpty() {
        final String path = getResourcePath("TaggingListenerFailNotEmpty");
        final JavaFileObject fileObject = JavaFileObjects.forResource(path);
        ASSERT.about(javaSource())
            .that(fileObject)
            .processedWith(new ListenerAnnotationProcessor())
            .failsToCompile().withErrorContaining("must be empty")
            .in(fileObject).onLine(9);
    }
    
    
    
    // END TAGGING TESTS
}

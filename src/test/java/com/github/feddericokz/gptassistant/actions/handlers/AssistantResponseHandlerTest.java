package com.github.feddericokz.gptassistant.actions.handlers;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class AssistantResponseHandlerTest {

    List<String> assistantResponse = Collections.singletonList("""
            <user-request>
                Implement a method in Java that takes a random number and prints it to the console. Additionally, create a unit test for this method in a new file.
            </user-request>
            <imports>
                java.util.Random, org.junit.jupiter.api.Test, static org.junit.jupiter.api.Assertions.assertNotNull
            </imports>
            <code-replacement>
            public class SomeClass {
                        
                public void printRandomNumber() {
                    Random random = new Random();
                    int randomNumber = random.nextInt();
                    System.out.println(randomNumber);
                }
                        
            }
            </code-replacement>
            <file-creation path="SomeClassTest.java">
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.assertNotNull;
                        
            public class SomeClassTest {
                        
                @Test
                public void testPrintRandomNumber() {
                    SomeClass someClass = new SomeClass();
                    someClass.printRandomNumber();
                    // This test verifies that the method runs, but it's not practical to test random outputs
                    assertNotNull(someClass, "SomeClass instance should not be null.");
                }
            }
            </file-creation>
            <steps>
                1. Understand the user's request for implementing a method in Java to print a random number and to create a unit test for it.
                2. Implement the method 'printRandomNumber' in the 'SomeClass' class, generating a random number and printing it to the console.
                3. Decide to use the java.util.Random class to generate a random number.
                4. In a new file, 'SomeClassTest.java', create a unit test named 'testPrintRandomNumber' using JUnit.
                5. Implement the unit test to simply check if the method can be called; actual output checking is not practical due to the random nature of the output.
                6. Import necessary classes for random number generation and unit testing.
            </steps>
            """);

    private String fileCreationTagContent = """
            
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.assertNotNull;
                        
            public class SomeClassTest {
                        
                @Test
                public void testPrintRandomNumber() {
                    SomeClass someClass = new SomeClass();
                    someClass.printRandomNumber();
                    // This test verifies that the method runs, but it's not practical to test random outputs
                    assertNotNull(someClass, "SomeClass instance should not be null.");
                }
            }
            """;

    private String codeReplacementContent = """
             
            public class SomeClass {
                       
                public void printRandomNumber() {
                    Random random = new Random();
                    int randomNumber = random.nextInt();
                    System.out.println(randomNumber);
                }
                        
            }
            """;

    @Test
    public void testAttributeExtractionFromXmlTag() {
        String attributeValue = AssistantResponseHandler.getXmlAttributeFromResponses(assistantResponse,
                "file-creation", "path");
        Assert.assertEquals("SomeClassTest.java", attributeValue);
    }

    @Test
    public void testContentExtractionFromXmlTagWithAttributes() {
        String extractedContent = AssistantResponseHandler.getXmlTagContentFromResponse(assistantResponse, "file-creation");
        Assert.assertEquals(fileCreationTagContent, extractedContent);
    }

    @Test
    public void testContentExtractionFromXmlTagWithoutAttributes() {
        String extractedContent = AssistantResponseHandler.getXmlTagContentFromResponse(assistantResponse, "code-replacement");
        Assert.assertEquals(codeReplacementContent, extractedContent);
    }

}

package com.github.feddericokz.gptassistant.actions.handlers;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface AssistantResponseHandler {

    Logger logger = ToolWindowLogger.getInstance(AssistantResponseHandler.class);

    static String getXmlAttribute(String tagWithContent, String tagName, String attributeName) {
        // Pattern to match the tagName with any attributes.
        Pattern fullTagPattern = Pattern.compile(
                "<" + Pattern.quote(tagName) + "([\\s]+[^>]*?)?>[\\s\\S]*?</" + Pattern.quote(tagName) + ">",
                Pattern.DOTALL);

        // Pattern to extract the specified attributeName's value within the tag.
        Pattern attributePattern = Pattern.compile(
                Pattern.quote(attributeName) + "\\s*=\\s*\"([^\"]*)\"|"
                        + Pattern.quote(attributeName) + "\\s*=\\s*'([^']*)'",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = fullTagPattern.matcher(tagWithContent);
        if (matcher.find()) {
            String matchedFullTag = matcher.group(0);
            // Now, using the attributePattern to find the attribute's value within the tagged content.
            Matcher attrMatcher = attributePattern.matcher(matchedFullTag);
            if (attrMatcher.find()) {
                // Return the value that corresponds to the attribute, considering both single and double quotes.
                return attrMatcher.group(1) != null ? attrMatcher.group(1) : attrMatcher.group(2);
            }
        }
        return ""; // Return empty string if the attribute is not found.
    }

    void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse);


    static String getXmlTagContentFromResponse(List<String> assistantResponses, String tagName) {
        String matchedFullTag = getXmlTagFromResponse(assistantResponses, tagName);
        try {
            // Parsing the matchedTag to a Document to easily extract attributes.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(matchedFullTag.getBytes("UTF-8"));
            Document doc = builder.parse(input);

            Node desiredNode = doc.getElementsByTagName(tagName).item(0);
            if (desiredNode != null && desiredNode.getTextContent() != null) {
                // Return the content of the found tag
                return desiredNode.getTextContent();
            }
        } catch (Exception e) {
            logger.error("Error while parsing assistant response.", e);
        }

        return ""; // Return empty string if not found or error occurred.
    }

    static List<String> getXmlTagContentListFromResponse(List<String> assistantResponses, String tagName) {
        List<String> contents = new ArrayList<>(); // List to store contents of all tags found with their surrounding tags
        for (String assistantResponse : assistantResponses) {
            int startIndex = 0;
            while (startIndex != -1) {
                // Find the start tag
                String startTag = "<" + tagName; // Ensure correct start tag format
                startIndex = assistantResponse.indexOf(startTag, startIndex);
                if (startIndex != -1) {
                    // Adjust the startIndex back to include the start tag in the result
                    int contentStartIndex = startIndex; // To include the start tag in the content

                    // Find the end tag
                    String endTag = "</" + tagName + ">";
                    int endIndex = assistantResponse.indexOf(endTag, startIndex);
                    if (endIndex != -1) {
                        // Adjust the endIndex to include the end tag
                        int contentEndIndex = endIndex + endTag.length();

                        // Extract content including startTag and endTag
                        String content = assistantResponse.substring(contentStartIndex, contentEndIndex);
                        contents.add(content);
                        // Move startIndex for next search
                        startIndex = contentEndIndex;
                    }
                }
            }
        }
        return contents; // Return the collected contents including the tags
    }

    static String getXmlTagFromResponse(List<String> assistantResponses, String tagName) {
        // Pattern to match the tagName with any attributes.
        Pattern fullTagPattern = Pattern.compile(
                "<" + Pattern.quote(tagName) + "(\\s+[\\w\\W]*?)?>[\\s\\S]*?</" + Pattern.quote(tagName) + ">",
                Pattern.DOTALL);

        for (String response : assistantResponses) {
            Matcher matcher = fullTagPattern.matcher(response);
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        return ""; // Return empty string if not found or error occurred.
    }


}

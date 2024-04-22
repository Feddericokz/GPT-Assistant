package com.github.feddericokz.gptassistant.actions.handlers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface AssistantResponseHandler {

    static String getXmlAttributeFromResponses(List<String> assistantResponses, String tagName, String attributeName) {
        // Pattern to match the tagName with any attributes.
        Pattern fullTagPattern = Pattern.compile(
                "<" + Pattern.quote(tagName) + "(\\s+[\\w\\W]*?)?>[\\s\\S]*?</" + Pattern.quote(tagName) + ">",
                Pattern.DOTALL);

        for (String response : assistantResponses) {
            Matcher matcher = fullTagPattern.matcher(response);
            if (matcher.find()) {
                String matchedFullTag = matcher.group(0);
                try {
                    // Parsing the matchedTag to a Document to easily extract attributes.
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    ByteArrayInputStream input = new ByteArrayInputStream(matchedFullTag.getBytes("UTF-8"));
                    Document doc = builder.parse(input);

                    NamedNodeMap attrs = doc.getDocumentElement().getAttributes();
                    Node nodeAttr = attrs.getNamedItem(attributeName);
                    return nodeAttr != null ? nodeAttr.getNodeValue() : "";
                } catch (Exception e) {
                    // TODO Agreed that should be replaced with more robust logging.
                    e.printStackTrace();
                }
            }
        }
        return ""; // Return empty string if not found or error occurred.
    }

    void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse);


    static String getXmlTagContentFromResponse(List<String> assistantResponses, String tagName) {
        // Pattern to match the tagName with any attributes.
        Pattern fullTagPattern = Pattern.compile(
                "<" + Pattern.quote(tagName) + "(\\s+[\\w\\W]*?)?>[\\s\\S]*?</" + Pattern.quote(tagName) + ">",
                Pattern.DOTALL);

        for (String response : assistantResponses) {
            Matcher matcher = fullTagPattern.matcher(response);
            if (matcher.find()) {
                String matchedFullTag = matcher.group(0);
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
                    // TODO Agreed that should be replaced with more robust logging.
                    e.printStackTrace();
                }
            }
        }
        return ""; // Return empty string if not found or error occurred.
    }


}

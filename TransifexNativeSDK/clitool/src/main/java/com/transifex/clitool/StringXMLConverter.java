package com.transifex.clitool;

import com.transifex.common.LocaleData;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * A converter that converts Android XML resources containing strings into data structures
 * accepted by CDS.
 */
class StringXMLConverter {

    XMLOutputter mXmlOutputter;

    public static class XMLConverterException extends Exception {

        public XMLConverterException() {
        }

        public XMLConverterException(String message) {
            super(message);
        }
    }

    public StringXMLConverter() {
        mXmlOutputter = new XMLOutputter();
        Format format = Format.getRawFormat();

        format.setLineSeparator(LineSeparator.UNIX);

        mXmlOutputter.setFormat(format);
    }

    /**
     * Parses the provided XML string file and populates the provided string map with key-value
     * pairs extracted from it.
     *
     * @param stringFile A file pointing to an Android XML resource file containing strings.
     * @param stringMap The map to update with the strings extracted from the string file.
     *
     * @throws JDOMException when the file is not in a valid XML format.
     * @throws IOException when the file can't be read.
     * @throws XMLConverterException when the XML file does not have the expected format.
     *
     * @see #process(Document, LinkedHashMap)
     */
    public void process(@NonNull File stringFile, @NonNull LinkedHashMap<String, LocaleData.StringInfo> stringMap)
            throws JDOMException, IOException, XMLConverterException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(stringFile);

        process(document, stringMap);
    }

    /**
     * Parses the provided XML document and populates the provided string map with key-value
     * pairs extracted from it.
     * <p>
     * The strings are inserted in the order found in the XML.
     *
     * @param document The XML representation of an Android XML resource file containing strings.
     * @param stringMap The map to update with the strings extracted from the string file.
     *
     * @throws JDOMException when the file is not in a valid XML format.
     * @throws XMLConverterException when the XML file does not have the expected format.
     */
    public void process(@NonNull Document document, @NonNull LinkedHashMap<String, LocaleData.StringInfo> stringMap)
            throws JDOMException, XMLConverterException {
       
        Element root = document.getRootElement();

        if (!root.getName().equals("resources")) {
            throw new XMLConverterException("The \"resources\" element was not found.");
        }

        List<Element> children = root.getChildren();
        for (Element child : children) {
            Attribute translatableAttr = child.getAttribute("translatable");
            if (translatableAttr != null && !translatableAttr.getBooleanValue()) {
                continue;
            }

            // TODO: support string-array and plurals

            if (child.getName().equals("string")) {
                String key = child.getAttribute("name").getValue();
                String string = getXMLText(child);
                stringMap.put(key, new LocaleData.StringInfo(string));
            }
        }
    }

    /**
     * Returns the content of the provided element as text, including the XML content.
     */
    @NonNull
    private String getXMLText(@NonNull Element element) {
        // https://stackoverflow.com/a/15219567/941314
        StringWriter stringWriter = new StringWriter();
        try {
            mXmlOutputter.output(element.getContent(), stringWriter);
        } catch (IOException e) {
            return "";
        }

        // TODO: decide on new line escape policy, spaces, special characters and update unit tests
        // https://developer.android.com/guide/topics/resources/string-resource.html#FormattingAndStyling

        return stringWriter.toString();
    }
}

package com.transifex.clitool;

import com.transifex.common.LocaleData;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;

import static com.google.common.truth.Truth.assertThat;

public class StringXMLConverterTest {

    private static final String stringsXML =
            "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<resources>\n" +
                    "    <string name=\"key1\">Some text</string>\n" +
                    "\n" +
                    "    <string name=\"key2\">Some text 2</string>\n" +
                    "</resources>";
    private static final String stringsXMLTranslatable =
            "<resources>\n" +
                    "    <string name=\"key1\" translatable=\"false\">Some text</string>\n" +
                    "\n" +
                    "    <string name=\"key2\" translatable=\"true\">Some text 2</string>\n" +
                    "</resources>";
    private static final String stringsXMLTagCharacters =
            "<resources>\n" +
                    "    <string name=\"key1\">This ia a <b>bold</b> statement</string>\n" +
                    "    <string name=\"key2\">This ia an &lt;escaped/&gt; tag</string>\n" +
                    "</resources>";
    private static final String stringsXMLNewline =
            "<resources>\n" +
                    "    <string name=\"key1\">This\\n is\n" +
                    "    a test</string>\n" +
                    "</resources>";

    static Document getXML(String string) {
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(new StringReader(string));
        } catch (JDOMException | IOException ignored) {}
        return null;
    }

    static Document getXML() {
        return getXML(stringsXML);
    }

    static Document getXMLTranslatable() {
        return getXML(stringsXMLTranslatable);
    }

    static Document getXMLTagCharacters() {
        return getXML(stringsXMLTagCharacters);
    }

    static Document getXMLNewLine() {
        return getXML(stringsXMLNewline);
    }

    @Before
    public void setUp() throws Exception {
        converter = new StringXMLConverter();
        stringMap = new LinkedHashMap<>(10);
    }

    StringXMLConverter converter;
    LinkedHashMap<String, LocaleData.StringInfo> stringMap;

    @Test
    public void testProcess_normalXML() {
        Document document = getXML();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("Some text");
        assertThat(stringMap.get("key2").string).isEqualTo("Some text 2");
    }

    @Test
    public void testProcess_translatableTag() {
        Document document = getXMLTranslatable();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key2").inOrder();
        assertThat(stringMap.get("key2").string).isEqualTo("Some text 2");
    }

    @Test
    public void testProcess_tagCharacter() {
        Document document = getXMLTagCharacters();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("This ia a <b>bold</b> statement");
        assertThat(stringMap.get("key2").string).isEqualTo("This ia an &lt;escaped/&gt; tag");
    }

//    @Test
//    public void testProcess_newLine() {
//        Document document = getXMLNewLine();
//        try {
//            converter.process(document, stringMap);
//        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
//            e.printStackTrace();
//        }
//
//        String test = "This\n is a test";
//        assertThat(stringMap.keySet()).containsExactly("key1").inOrder();
//        assertThat(stringMap.get("key1").string).isEqualTo("This\n is a test");
//    }
}
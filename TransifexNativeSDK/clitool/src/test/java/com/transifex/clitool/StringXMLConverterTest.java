package com.transifex.clitool;

import com.transifex.common.LocaleData;
import com.transifex.common.Plurals;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

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
    private static final String stringsXMLPlurals =
            "<resources>\n" +
                    "    <plurals name=\"plural_test\">\n" +
                    "        <item quantity=\"zero\">zero</item>\n" +
                    "        <item quantity=\"one\">one</item>\n" +
                    "        <item quantity=\"two\">two</item>\n" +
                    "        <item quantity=\"few\">few</item>\n" +
                    "        <item quantity=\"many\">many</item>\n" +
                    "        <item quantity=\"other\">other</item>\n" +
                    "    </plurals>\n" +
                    "\n" +
                    "    <plurals name=\"plural_test2\">\n" +
                    "        <item quantity=\"other\">other2</item>\n" +
                    "        <item quantity=\"one\">one2</item>\n" +
                    "    </plurals>\n" +
                    "</resources>";
    private static final String stringsXMLPluralsOtherNotSpecified =
            "<resources>\n" +
                    "    <plurals name=\"plural_test\">\n" +
                    "        <item quantity=\"one\">one</item>\n" +
                    "    </plurals>\n" +
                    "\n" +
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

    static Document getPluralsXML() {
        return getXML(stringsXMLPlurals);
    }

    static Document getPluralsXMLOtherNotSpecified() {
        return getXML(stringsXMLPluralsOtherNotSpecified);
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

    @Test
    public void testProcess_pluralsXMLNormal() {
        Document document = getPluralsXML();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("plural_test", "plural_test2").inOrder();
        assertThat(stringMap.get("plural_test").string).isEqualTo("{cnt, plural, zero {zero} one {one} two {two} few {few} many {many} other {other}}");
        assertThat(stringMap.get("plural_test2").string).isEqualTo("{cnt, plural, one {one2} other {other2}}");
    }

    @Test
    public void testProcess_pluralsXMLOtherNotSpecified_throwException() {
        Document document = getPluralsXMLOtherNotSpecified();

        assertThrows(StringXMLConverter.XMLConverterException.class, new ThrowingRunnable() {
                    @Override
                    public void run() throws Throwable {
                        converter.process(document, stringMap);
                    }
                }
        );
    }
}
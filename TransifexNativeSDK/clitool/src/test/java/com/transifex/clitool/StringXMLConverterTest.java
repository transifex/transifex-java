package com.transifex.clitool;

import com.transifex.common.LocaleData;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class StringXMLConverterTest {

    // Some of tests rely on the files of the following directory:
    // testFiles

    private static final String stringsXML =
            "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<resources>\n" +
                    "    <string name=\"key1\">Some text</string>\n" +
                    "\n" +
                    "    <string name=\"key2\">Some text 2</string>\n" +
                    "\n" +
                    "    <string name=\"key3\">@string/key2</string>\n" +
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
                    "    <string name=\"key3\">You have &lt;b>%2$d new messages&lt;/b></string>\n" +
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

    static Document getXMlFromFile(String filename) {
        File file = new File("testFiles" + File.separator + filename);
        if (!file.exists() && !file.exists()) {
            return null;
        }
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(file);
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

        return getXMlFromFile("strings-test-new-line.xml");
    }

    static Document getXMLTab() {

        return getXMlFromFile("strings-test-tab.xml");
    }

    static Document getXMLQuote() {

        return getXMlFromFile("strings-test-quote.xml");
    }

    static Document getXMLSpace() {

        return getXMlFromFile("strings-test-space.xml");
    }

    static Document getXMLNewLineSpaceTab() {

        return getXMlFromFile("strings-test-new-line-space-tab.xml");
    }

    static Document getXMLInsideDoubleQuotes() {

        return getXMlFromFile("strings-test-insidedoublequotes.xml");
    }

    static Document getXMLUnicode() {

        return getXMlFromFile("strings-test-unicode.xml");
    }

    static Document getXMLBackslash() {

        return getXMlFromFile("strings-test-backslash.xml");
    }

    static Document getXMLHTMLEntities() {

        return getXMlFromFile("strings-test-htmlentities.xml");
    }

    static Document getXMLAt() {

        return getXMlFromFile("strings-test-at.xml");
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

    // region parse strings

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

        assertThat(stringMap.keySet()).containsExactly("key1", "key2", "key3").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("This ia a <b>bold</b> statement");
        assertThat(stringMap.get("key2").string).isEqualTo("This ia an &lt;escaped/&gt; tag");
        // When the input uses escaped "<" as "&lt;", JDOM also escapes ">" to "&gt;"
        assertThat(stringMap.get("key3").string).isEqualTo("You have &lt;b&gt;%2$d new messages&lt;/b&gt;");
    }

    @Test
    public void testProcess_newLine() {
        Document document = getXMLNewLine();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2", "key3").inOrder();
        // This should be rendered as: "anew line \n \newline \\nb"
        assertThat(stringMap.get("key1").string).isEqualTo("a\n \\n \\\n \\\\nb");
        assertThat(stringMap.get("key2").string).isEqualTo("actual new line should be replaced by space");
        assertThat(stringMap.get("key3").string).isEqualTo("multiple actual new lines should be replaced by a single space");
    }

    @Test
    public void testProcess_tab() {
        Document document = getXMLTab();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2", "key3", "key4")
                .inOrder();
        // This should be rendered as: "aTABb\tc"
        assertThat(stringMap.get("key1").string).isEqualTo("a\tb\\tc");
        assertThat(stringMap.get("key2").string).isEqualTo("actual tab should be replaced by space");
        assertThat(stringMap.get("key3").string).isEqualTo("multiple escaped tabs\t\t\tare preserved as tabs");
        assertThat(stringMap.get("key4").string).isEqualTo("multiple real tabs are collapsed to a single space");
    }

    @Test
    public void testProcess_quote() {
        Document document = getXMLQuote();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2", "key3", "key4").inOrder();
        // This should be rendered as: "abQUOTEc\d"
        assertThat(stringMap.get("key1").string).isEqualTo("ab\"c\\d");
        assertThat(stringMap.get("key2").string).isEqualTo("ab'c\\d");
        // Ideally we would like to preserve the quotes inside the HTML: "#FF7700", but currently
        // this is the expected behavior.
        assertThat(stringMap.get("key3").string).isEqualTo("This <font color=#FF7700>is</font> a <b>bold</b> statement");
        assertThat(stringMap.get("key4").string).isEqualTo("\"");
    }

    @Test
    public void testProcess_space() {
        Document document = getXMLSpace();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("multiple spaces should be collapsed to a single space");
        assertThat(stringMap.get("key2").string).isEqualTo(" spaces at the beginning and some trailing ones ");
    }

    @Test
    public void testProcess_newLineSpaceTab() {
        Document document = getXMLNewLineSpaceTab();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("multiple spaces, a tab and new lines should be collapsed into a single space");
    }

    @Test
    public void testProcess_insideDoubleQuotes() {
        Document document = getXMLInsideDoubleQuotes();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2", "key3", "key4").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("multiple spaces are allowed     here but not here");
        assertThat(stringMap.get("key2").string).isEqualTo("single quotes ' '' are allowed here but not here");
        assertThat(stringMap.get("key3").string).isEqualTo("HTML entity quotes behave like normal        quotes");
        assertThat(stringMap.get("key4").string).isEqualTo("multiple new lines\n" +
                "\n" +
                " and new lines \n" +
                " \n" +
                " with   spaces are allowed when in double quotes");
    }

    @Test
    public void testProcess_unicode() {
        Document document = getXMLUnicode();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2", "key3", "key4", "key5", "key6").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("Ελληνικά!");
        assertThat(stringMap.get("key2").string).isEqualTo("unicode heart: ❤");
        assertThat(stringMap.get("key3").string).isEqualTo("escaped heart: ❤");
        assertThat(stringMap.get("key4").string).isEqualTo("html decimal escaped heart: ❤");
        assertThat(stringMap.get("key5").string).isEqualTo("html hexadecimal escaped heart: ❤");
        assertThat(stringMap.get("key6").string).isEqualTo("long unicode emoji: \uD83E\uDDD1\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1");
    }

    @Test
    public void testProcess_backslash() {
        Document document = getXMLBackslash();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1", "key2", "key3").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("this s should be ignored");
        assertThat(stringMap.get("key2").string).isEqualTo("an escaped backslash is here \\ is and here \\");
        assertThat(stringMap.get("key3").string).isEqualTo("the single backslash at the end is preserved \\");
    }

    @Test
    public void testProcess_htmlEntities() {
        Document document = getXMLHTMLEntities();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        assertThat(stringMap.keySet()).containsExactly("key1").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("these html entities &amp; &lt; &gt; should be left as is");
    }

    @Test
    public void testProcess_at() {
        Document document = getXMLAt();
        try {
            converter.process(document, stringMap);
        } catch (JDOMException | StringXMLConverter.XMLConverterException e) {
            e.printStackTrace();
        }

        // key 2 should be ignored by our parser
        assertThat(stringMap.keySet()).containsExactly("key1", "key3", "key4").inOrder();
        assertThat(stringMap.get("key1").string).isEqualTo("using it here @ or escaped @ is ok");
        assertThat(stringMap.get("key3").string).isEqualTo("@ this is ok");
        assertThat(stringMap.get("key4").string).isEqualTo("<b>@</b> this is ok");
    }

    // endregion parse strings

    // region parse plurals

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

    // endregion parse plurals
}
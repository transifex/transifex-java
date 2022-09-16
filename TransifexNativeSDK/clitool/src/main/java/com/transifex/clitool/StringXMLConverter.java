package com.transifex.clitool;

import com.transifex.common.LocaleData;
import com.transifex.common.Plurals;

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
     * @param stringMap The map to update with the strings extracted from the document.
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

            // TODO: support string-array

            if (child.getName().equals("string")) {
                String key = child.getAttribute("name").getValue();
                String string = getXMLText(child);
                // Ignore resource references
                if (string.startsWith("@")) {
                    continue;
                }
                stringMap.put(key, new LocaleData.StringInfo(string));
            }
            else if (child.getName().equals("plurals")) {
                String key = child.getAttribute("name").getValue();
                List<Element> pluralItems = child.getChildren("item");
                if (pluralItems.isEmpty()) {
                    continue;
                }
                boolean hasResourceReference = false;
                Plurals.Builder sb = new Plurals.Builder();
                for (Element item : pluralItems) {
                    String quantity = item.getAttribute("quantity").getValue();
                    String itemString = getXMLText(item);
                    if (itemString.startsWith("@")) {
                        hasResourceReference = true;
                        break;
                    }
                    sb.setPlural(quantity, itemString);
                }
                if (hasResourceReference) {
                    continue;
                }
                Plurals plurals;
                try {
                    plurals = sb.buildString();
                }
                catch (Plurals.InvalidPluralsConfiguration e) {
                    throw new XMLConverterException("\"other\" is not specified for Plurals resource \"" + key + "\"");
                }
                stringMap.put(key, new LocaleData.StringInfo(plurals.toICUString()));
            }
        }
    }

    /**
     * Returns the content of the provided element as text, including any XML content. It also
     * applies the special character handling of Android's XML parser as defined
     * <a href="https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes">here</a>.
     * <p>
     * The final processing of HTML entities and tags is left for the SDK at runtime. So, any tags
     * are left as is. HTML entities such as {@code "&amp;", "&lt;" and "&gt;"}
     * (with the exception of {@code "&quot;" which is converted to """} are left untouched.
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

        String originalString = stringWriter.toString();

        // We follow Android XML Parser's special character handling.

        // Replace new lines with spaces.
        String unescapedString = originalString.replace('\n', ' ');
        // Replace tabs with spaces.
        unescapedString = unescapedString.replace('\t', ' ');
        // Unescape special chars. For example convert a typed "\n" ("\\n" in Java) to a new line
        // ("\n" in Java) and do some extra processing similar to the Android XML parser.
        unescapedString= unescapeJavaString(unescapedString);

        return unescapedString;
    }

    /**
     * Unescapes a string that contains standard Java escape sequences.
     * <ul>
     * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
     * BS, FF, NL, CR, TAB, double and single quote.</li>
     * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
     * specification (0 - 377, 0x00 - 0xFF).</li>
     * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
     * </ul>
     *
     * Changes have been made to the <a href="https://gist.github.com/uklimaschewski/6741769">
     *     original code</a> to follow the Android XML Parser behavior. Octal support has been
     *     removed. The following are now supported:
     *     <ul>
     *         <li>Whitespace character sequences are collapsed into a single space, unless they
     *         are enclosed in double quotes.</li>
     *         <li>Double quotes are removed, unless escaped.</li>
     *         <li>Single quotes are removed (you can't actually write them using Android Studio's
     *         string editor), unless escaped or quoted.</li>
     *     </ul>
     *
     * In order to follow the Android XML parser's behavior, non-escaped double quotes are removed.
     * So, the quotes inside a string containing HTML code (e.g.  {@code <font color="#FF7700">})
     * will be removed as well. However, this does not seem to affect the parsing of
     * <code>HtmlCompat.fromHtml()</code> that the SDK or the user may perform.
     *
     * @param st
     *            A string optionally containing standard java escape sequences.
     * @return The unescaped string.
     */
    public String unescapeJavaString(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        boolean isInsideDoubleQuotes = false;

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st.charAt(i + 1);
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                        + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                    default:
                        // if the second character is unexpected, remove the '\' character
                        continue;
                }
                i++;
            } else if (ch == '\"') {
                isInsideDoubleQuotes = !isInsideDoubleQuotes;
                // Remove a non-escaped double quote
                continue;
            }
            else if (ch == '\'') {
                if (!isInsideDoubleQuotes) {
                    // Remove a non-escaped single quote unless we're inside double quotes
                    continue;
                }
            }
            else if (ch == ' ') {
                if (!isInsideDoubleQuotes) {
                    // Collapse sequences of whitespace characters into a single space, unless we're
                    // inside double quotes
                    for (int nextIndex = i+1; nextIndex < st.length(); nextIndex++) {
                        if (st.charAt(nextIndex) == ' ') {
                            i = nextIndex;
                        }
                        else {
                            break;
                        }
                    }
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}

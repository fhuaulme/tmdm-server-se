/*
 * Copyright (C) 2006-2011 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amalto.core.load.exception.ParserCallbackException;
import com.amalto.core.load.io.XMLRootInputStream;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
@SuppressWarnings("nls")
public class LoadParserTest extends TestCase {

    public static final boolean DEBUG = false;

    public void testArgs() {
        ParserTestCallback callback = new ParserTestCallback();

        try {
            LoadParser.parse(null, null, null);
        } catch (IllegalArgumentException e) {
            assertEquals("Input stream cannot be null", e.getMessage());
        }

        try {
            LoadParser.parse(new ByteArrayInputStream(StringUtils.EMPTY.getBytes()), null, callback);
        } catch (IllegalArgumentException e) {
            assertEquals("Configuration cannot be null", e.getMessage());
        }

        try {
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{null}, false, "clusterName");
            LoadParser.parse(new ByteArrayInputStream(StringUtils.EMPTY.getBytes()), config, null);
        } catch (IllegalArgumentException e) {
            assertEquals("LoadParser callback cannot be null", e.getMessage());
        }
    }

    public void testCallbackFailure() {
        LoadParserCallback callback = new LoadParserCallback() {
            public void flushDocument(XMLReader docReader, InputSource input) {
                throw new RuntimeException();
            }
        };

        try {
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id"}, false, "clusterName");
            LoadParser.parse(new ByteArrayInputStream("<root><Id>0</Id></root>".getBytes()), config, callback);
            Assert.fail("Should have failed due to callback exception.");
        } catch (Exception e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof ParserCallbackException);
        }
    }

    public void test1() {
        InputStream testResource = this.getClass().getResourceAsStream("test1.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"id"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "element1"));
        assertEquals("999", callback.getId());
    }

    public void test2() {
        InputStream testResource = this.getClass().getResourceAsStream("test2.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("Geoname", new String[]{"geonameid"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(31, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Geoname"));
        assertTrue(hasParsedElement(callback, "latitude"));
        assertTrue(hasParsedCharacters(callback, "Font de la Xona"));
        assertEquals("3038815", callback.getId());

        if (DEBUG) {
            testResource = this.getClass().getResourceAsStream("test2.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource, config, callback2);
        }
    }

    public void test3() {
        InputStream testResource = this.getClass().getResourceAsStream("test3.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[]{"Id"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(29, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "Product"));
        assertTrue(hasParsedElement(callback, "Features"));
        assertTrue(hasParsedCharacters(callback, "porttitor pharetra quis sed risus."));
        assertEquals("1", callback.getId());
    }

    public void test4() {
        InputStream testResource = this.getClass().getResourceAsStream("test4.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element1"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(16, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "This is sample text"));
        assertEquals("", callback.getId());

        if (DEBUG) {
            testResource = this.getClass().getResourceAsStream("test4.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource, config, callback2);
        }
    }

    public void test5() {
        InputStream testResource = this.getClass().getResourceAsStream("test5.xml");
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"uniqueId"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(14, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedAttribute(callback, "attribute1"));
        assertTrue(hasParsedAttribute(callback, "attribute2"));
        assertEquals("0", callback.getId());

        if (DEBUG) {
            testResource = this.getClass().getResourceAsStream("test5.xml");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource, config, callback2);
        }
    }

    public void test3MultiThread() {
        int threadNumber = 20;
        Set<Thread> threads = new HashSet<Thread>(threadNumber + 1);

        Runnable runnable = new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    InputStream testResource = this.getClass().getResourceAsStream("test3.xml");
                    assertNotNull(testResource);

                    ParserTestCallback callback = new ParserTestCallback();

                    LoadParser.Configuration config = new LoadParser.Configuration("Product", new String[]{"Id"}, false, "clusterName");
                    LoadParser.parse(testResource, config, callback);
                    assertTrue(callback.hasBeenFlushed());
                    assertEquals(29, callback.getStartedElements().size());
                    assertTrue(hasParsedElement(callback, "Product"));
                    assertTrue(hasParsedElement(callback, "Features"));
                    assertTrue(hasParsedCharacters(callback, "porttitor pharetra quis sed risus."));
                    assertEquals("1", callback.getId());
                }
            }
        };

        for (int i = 0; i < threadNumber; i++) {
            threads.add(new Thread(runnable));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void testMultipleXmlRoot() {
        InputStream testResource = new ByteArrayInputStream("<root><element1/><element2>text</element2></root><root><element1/><element2>text</element2></root>".getBytes());
        testResource = new XMLRootInputStream(testResource, "doc");
        assertNotNull(testResource);
        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element2"}, false, "clusterName");

        if (DEBUG) {
            InputStream testResource2 = new ByteArrayInputStream("<root><element1/><element2>text</element2></root><root><element1/><element2>text</element2></root>".getBytes());
            testResource2 = new XMLRootInputStream(testResource2, "doc");
            LoadParserCallback callback2 = new ConsolePrintParserCallback();
            LoadParser.parse(testResource2, config, callback2);
        }

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.parse(testResource, config, callback);
        assertTrue(callback.hasBeenFlushed());
        assertEquals(2, callback.getFlushCount());
        assertEquals(26, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("text", callback.getId());
    }

    public void testMultipleXmlRootFailure() {
        InputStream testResource = new ByteArrayInputStream("<root><element1/><element2>text</element2></root><root><element1/><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        try {
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element2"}, false, "clusterName");
            LoadParser.parse(testResource, config, callback);
            fail("Should have failed. There are 2 roots in XML document.");
        } catch (Exception e) {
            // Expected
        }

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element1"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("text", callback.getId());
    }

    public void testSimpleId() {
        InputStream testResource = new ByteArrayInputStream("<root><Id>1</Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("1", callback.getId());
    }

    public void testIdFailure() {
        InputStream testResource = new ByteArrayInputStream("<root><Id>1</Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        try {
            LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"element_that_does_not_exist"}, false, "clusterName");
            LoadParser.parse(testResource, config, callback);
            fail("Expected an error since id field does not exist");
        } catch (Exception e) {
            // Expected
        }

        assertFalse(callback.hasBeenFlushed());
        assertEquals(0, callback.getFlushCount());
        assertEquals(0, callback.getStartedElements().size());
    }

    public void testEmptyId() {
        InputStream testResource = new ByteArrayInputStream("<root><Id></Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("", callback.getId());
    }

    public void testCompoundId() {
        InputStream testResource = new ByteArrayInputStream("<root><Id1>1</Id1><Id2>2</Id2><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id1", "Id2"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(14, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("1:2", callback.getId());
    }

    public void testNewId() {
        InputStream testResource = new ByteArrayInputStream("<root><NewId>1</NewId><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"NewId"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(13, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("1", callback.getId());
    }

    public void testNestedId() {
        InputStream testResource = new ByteArrayInputStream("<root><Id><RealId>1</RealId></Id><element2>text</element2></root>".getBytes());
        assertNotNull(testResource);

        ParserTestCallback callback = new ParserTestCallback();

        LoadParser.Configuration config = new LoadParser.Configuration("root", new String[]{"Id/RealId"}, false, "clusterName");
        LoadParser.parse(testResource, config, callback);

        assertTrue(callback.hasBeenFlushed());
        assertEquals(1, callback.getFlushCount());
        assertEquals(14, callback.getStartedElements().size());
        assertTrue(hasParsedElement(callback, "root"));
        assertTrue(hasParsedElement(callback, "element2"));
        assertTrue(hasParsedCharacters(callback, "text"));
        assertEquals("1", callback.getId());
    }

    private boolean hasParsedCharacters(ParserTestCallback callback, String string) {
        return callback.getParsedCharacters().contains(string);
    }

    private static class ParserTestCallback implements LoadParserCallback {
        private final List<String> startedElements = new ArrayList<String>();
        private final List<String> parsedAttributes = new ArrayList<String>();
        private final Set<String> characters = new HashSet<String>();
        private int flushCount;
        private String id;

        public boolean hasBeenFlushed() {
            return flushCount > 0;
        }

        public int getFlushCount() {
            return flushCount;
        }

        public List<String> getStartedElements() {
            return startedElements;
        }

        public List<String> getParsedAttributes() {
            return parsedAttributes;
        }

        public String getId() {
            return id;
        }

        public Set<String> getParsedCharacters() {
            return Collections.unmodifiableSet(characters);
        }

        public void flushDocument(XMLReader docReader, InputSource input) {
            flushCount++;

            try {
                docReader.setContentHandler(new DefaultHandler() {
                    public boolean isId;

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        startedElements.add(localName);

                        if ("t".equals(localName)) {
                            isId = true;
                        }

                        for (int i = 0; i < attributes.getLength(); i++) {
                            parsedAttributes.add(attributes.getLocalName(i));
                        }
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        if (isId) {
                            isId = false;
                        }
                        super.endElement(uri, localName, qName);
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        String string = new String(ch);
                        if (isId) {
                            id = string;
                        } else {
                            characters.add(string.trim());
                        }
                        super.characters(ch, start, length);
                    }
                });
                docReader.parse(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean hasParsedElement(ParserTestCallback callback, String elementName) {
        boolean hasParsedElement = false;
        for (String currentElementName : callback.getStartedElements()) {
            hasParsedElement = elementName.equals(currentElementName);
            if (hasParsedElement) {
                break;
            }
        }
        return hasParsedElement;
    }

    private boolean hasParsedAttribute(ParserTestCallback callback, String attributeName) {
        boolean hasParsedAttribute = false;
        for (String currentAttributeName : callback.getParsedAttributes()) {
            hasParsedAttribute = attributeName.equals(currentAttributeName);
            if (hasParsedAttribute) {
                break;
            }
        }
        return hasParsedAttribute;
    }

    private static class ConsolePrintParserCallback implements LoadParserCallback {
        public void flushDocument(XMLReader docReader, InputSource input) {
            try {
                docReader.setContentHandler(new ContentHandler() {
                    int indent = 0;

                    private void indent() {
                        for (int i = 0; i < indent; i++) {
                            System.out.print('\t');
                        }
                    }

                    public void setDocumentLocator(Locator locator) {
                        System.out.println("ParserScalabilityTest$ConsolePrintParserCallback.setDocumentLocator");
                    }

                    public void startDocument() throws SAXException {
                        System.out.println("[Document start]");
                    }

                    public void endDocument() throws SAXException {
                        if (indent > 0) {
                            throw new IllegalStateException("XML document isn't well-formed.");
                        }
                        System.out.println("[Document end]");
                    }

                    public void startPrefixMapping(String prefix, String uri) throws SAXException {
                        System.out.println("ParserScalabilityTest$ConsolePrintParserCallback.startPrefixMapping");
                    }

                    public void endPrefixMapping(String prefix) throws SAXException {
                        System.out.println("ParserScalabilityTest$ConsolePrintParserCallback.endPrefixMapping");
                    }

                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        indent();
                        System.out.print('<' + localName);
                        for (int i = 0; i < attributes.getLength(); i++) {
                            System.out.print(attributes.getLocalName(i) + "= \"" + attributes.getValue(i) + "\" ");
                        }
                        System.out.println('>');
                        indent++;
                    }

                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        indent--;
                        indent();
                        System.out.println("</" + localName + ">");
                    }

                    public void characters(char[] ch, int start, int length) throws SAXException {
                        indent();
                        for (int i = start; i < length; i++) {
                            System.out.print(ch[i]);
                        }
                        System.out.println("");
                    }

                    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                        System.out.println("ParserScalabilityTest$ConsolePrintParserCallback.ignorableWhitespace");
                    }

                    public void processingInstruction(String target, String data) throws SAXException {
                        System.out.println("ParserScalabilityTest$ConsolePrintParserCallback.processingInstruction");
                    }

                    public void skippedEntity(String name) throws SAXException {
                        System.out.println("ParserScalabilityTest$ConsolePrintParserCallback.skippedEntity");
                    }
                });
                docReader.parse(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

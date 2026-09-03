/*
 * Copyright (c) 2002-2015, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.maven.xdoc2md;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * XDoc2MarkdownHandler unit tests
 */
public class XDoc2MarkdownHandlerTest
    extends TestCase
{
    private static final String ARTIFACT_ID = "artifactId";
    private static final String REPOSITORY = "lutece-cms-plugin-document.git";

    /**
     * Test that the repository name is not mangled when it contains "git".
     * ".git" used to be removed with replaceAll, where the dot is a regular
     * expression matching any character.
     */
    public void testGetJobNameWithGitInsideTheRepositoryName(  )
    {
        XDoc2MarkdownHandler handler = new XDoc2MarkdownHandler( ARTIFACT_ID, REPOSITORY );

        assertEquals( "tools-git-scripts-deploy", handler.getJobName( "tools-git-scripts.git" ) );
        assertEquals( "gru-plugin-digitalisation-deploy", handler.getJobName( "gru-plugin-digitalisation.git" ) );
    }

    /**
     * Test the job name of the usual repository names
     */
    public void testGetJobName(  )
    {
        XDoc2MarkdownHandler handler = new XDoc2MarkdownHandler( ARTIFACT_ID, REPOSITORY );

        assertEquals( "tools-maven-xdoc2md-plugin-deploy", handler.getJobName( "tools-maven-xdoc2md-plugin.git" ) );
        assertEquals( "core-deploy", handler.getJobName( "lutece-core.git" ) );
        assertEquals( "core-deploy", handler.getJobName( "lutece-core" ) );
    }

    /**
     * Test that only the "lutece-" prefix is removed, not every occurrence
     */
    public void testGetJobNameRemovesThePrefixOnly(  )
    {
        XDoc2MarkdownHandler handler = new XDoc2MarkdownHandler( ARTIFACT_ID, REPOSITORY );

        assertEquals( "gru-plugin-lutece-foo-deploy", handler.getJobName( "gru-plugin-lutece-foo.git" ) );
    }

    /**
     * Test the language attribute of the pre element
     * @throws java.lang.Exception
     */
    public void testCodeBlockLanguage(  ) throws Exception
    {
        assertTrue( convert( "<pre language=\"java\">code</pre>" ).contains( "```java\ncode" ) );
        assertTrue( convert( "<pre language=\"properties\">code</pre>" ).contains( "```properties\ncode" ) );
    }

    /**
     * Test that the language attribute is trimmed, otherwise the fence is invalid
     * @throws java.lang.Exception
     */
    public void testCodeBlockLanguageIsTrimmed(  ) throws Exception
    {
        String strDocument = convert( "<pre language=\" java \">code</pre>" );

        assertTrue( strDocument.contains( "```java\ncode" ) );
        assertFalse( strDocument.contains( "``` java" ) );
    }

    /**
     * Test that a pre element without usable language attribute is unchanged
     * @throws java.lang.Exception
     */
    public void testCodeBlockWithoutLanguage(  ) throws Exception
    {
        assertTrue( convert( "<pre>code</pre>" ).contains( "```\ncode" ) );
        assertTrue( convert( "<pre language=\"\">code</pre>" ).contains( "```\ncode" ) );
        assertTrue( convert( "<pre language=\"  \">code</pre>" ).contains( "```\ncode" ) );
    }

    /**
     * Test that the indentation of a code block is preserved
     * @throws java.lang.Exception
     */
    public void testCodeBlockKeepsIndentation(  ) throws Exception
    {
        assertTrue( convert( "<pre language=\"java\">\n    indented\n</pre>" ).contains( "\n    indented\n" ) );
    }

    /**
     * Test an anchor whose text contains an entity. SAX splits the text node on
     * the entity, and one link used to be generated for each fragment.
     * @throws java.lang.Exception
     */
    public void testAnchorWithEntity(  ) throws Exception
    {
        String strDocument = convert( "<a href=\"http://a.b\">Doc &amp; Guide</a>" );

        assertTrue( strDocument.contains( "[Doc & Guide](http://a.b)" ) );
        assertEquals( 1, countOccurrences( strDocument, "](http://a.b)" ) );
    }

    /**
     * Test that a link is separated from the previous text by a single space
     * @throws java.lang.Exception
     */
    public void testAnchorSpacing(  ) throws Exception
    {
        String strDocument = convert( "converted to \n            <a href=\"http://a.b\">Markdown</a> files" );

        assertTrue( strDocument.contains( "converted to [Markdown](http://a.b) files" ) );
    }

    /**
     * Test an anchor holding a nested tag
     * @throws java.lang.Exception
     */
    public void testAnchorWithNestedTag(  ) throws Exception
    {
        String strDocument = convert( "<a href=\"http://c.d\"><strong>Bold link</strong></a>" );

        assertTrue( strDocument.contains( "[**Bold link**](http://c.d)" ) );
        assertEquals( 1, countOccurrences( strDocument, "](http://c.d)" ) );
    }

    /**
     * Test an anchor holding an image, the usual way to build a clickable badge
     * @throws java.lang.Exception
     */
    public void testAnchorWithImage(  ) throws Exception
    {
        String strDocument = convert( "<a href=\"http://c.d\"><img src=\"http://e.f/logo.png\" alt=\"Logo\" /></a>" );

        assertTrue( strDocument.contains( "[![Logo](http://e.f/logo.png)](http://c.d)" ) );
    }

    /**
     * Test an anchor without href, which is a valid xDoc anchor and used to
     * throw a NullPointerException
     * @throws java.lang.Exception
     */
    public void testAnchorWithoutHref(  ) throws Exception
    {
        String strDocument = convert( "<a name=\"anchor\">Anchor text</a>" );

        assertTrue( strDocument.contains( "Anchor text" ) );
        assertFalse( strDocument.contains( "[Anchor text](" ) );
    }

    /**
     * Test a long text, which SAX splits into several events on its parser buffer
     * boundary. Trimming each event glued the last word of an event and the first
     * word of the next one.
     * @throws java.lang.Exception
     */
    public void testLongTextIsNotGlued(  ) throws Exception
    {
        StringBuilder sbContent = new StringBuilder(  );
        StringBuilder sbExpected = new StringBuilder(  );

        for ( int i = 0; i < 400; i++ )
        {
            sbContent.append( "word" ).append( i ).append( "\n                " );
            sbExpected.append( "word" ).append( i ).append( " " );
        }

        String strDocument = convert( sbContent.toString(  ) );

        assertTrue( strDocument.contains( sbExpected.toString(  ).trim(  ) ) );
    }

    /**
     * Test that a markup followed by a punctuation is not separated from it
     * @throws java.lang.Exception
     */
    public void testMarkupBeforePunctuation(  ) throws Exception
    {
        String strDocument = convert( "volutpat. <strong>bold text</strong>, quis nostrud" );

        assertTrue( strDocument.contains( "**bold text**, quis" ) );
    }

    /**
     * Test that a markup inside parentheses keeps them tight
     * @throws java.lang.Exception
     */
    public void testMarkupInsideParentheses(  ) throws Exception
    {
        String strDocument = convert( "the goal (<code>readme</code>) creates the file" );

        assertTrue( strDocument.contains( "(`readme`) creates" ) );
    }

    /**
     * Test that a link is not separated from the punctuation around it
     * @throws java.lang.Exception
     */
    public void testAnchorInsideParentheses(  ) throws Exception
    {
        String strDocument = convert( "see (<a href=\"http://a.b\">the guide</a>) for details" );

        assertTrue( strDocument.contains( "([the guide](http://a.b)) for details" ) );
    }

    /**
     * Test that a code block holds no blank line, the line break of the opening tag
     * and the indentation of the closing tag are not part of the code
     * @throws java.lang.Exception
     */
    public void testCodeBlockHasNoSurroundingBlankLine(  ) throws Exception
    {
        String strDocument = convert( "<pre language=\"java\">\ncode\n            </pre>" );

        assertTrue( strDocument.contains( "```java\ncode\n```" ) );
    }

    /**
     * Test that a code block written with a CDATA section holds no blank line
     * @throws java.lang.Exception
     */
    public void testCodeBlockWithCData(  ) throws Exception
    {
        String strDocument = convert( "<pre language=\"html\">\n<![CDATA[\n<table/>\n]]>\n            </pre>" );

        assertTrue( strDocument.contains( "```html\n<table/>\n```" ) );
    }

    /**
     * Test that no line of the document ends with a space
     * @throws java.lang.Exception
     */
    public void testNoLineEndsWithASpace(  ) throws Exception
    {
        String strDocument = convert( "Lorem ipsum \n            <ul>\n                <li>List item 1</li>\n" +
            "            </ul>\n            <table><tr><th>Column Title 1</th></tr></table>\n            " );

        assertFalse( strDocument.contains( " \n" ) );
    }

    /**
     * Test that a paragraph does not end with a space, two trailing spaces are a
     * hard line break in Markdown
     * @throws java.lang.Exception
     */
    public void testParagraphHasNoTrailingSpace(  ) throws Exception
    {
        assertFalse( convert( "Lorem ipsum \n            " ).contains( " \n" ) );
    }

    /**
     * Test that the line breaks of a paragraph are collapsed into single spaces
     * @throws java.lang.Exception
     */
    public void testMultipleLinesAreCollapsed(  ) throws Exception
    {
        String strDocument = convert( "Ut wisi enim ad minim veniam, \n            quis nostrud exerci tation" );

        assertTrue( strDocument.contains( "veniam, quis nostrud" ) );
    }

    /**
     * Test a text split by an entity
     * @throws java.lang.Exception
     */
    public void testTextSplitByAnEntity(  ) throws Exception
    {
        String strDocument = convert( "Docs &amp; Guides" );

        assertTrue( strDocument.contains( "Docs & Guides" ) );
        assertFalse( strDocument.contains( "Docs&" ) );
    }

    /**
     * Test a text split by an inline tag
     * @throws java.lang.Exception
     */
    public void testTextSplitByAnInlineTag(  ) throws Exception
    {
        String strDocument = convert( "the <code>readme</code> goal" );

        assertTrue( strDocument.contains( "the `readme` goal" ) );
    }

    /**
     * Test that the markup sticks to the text it wraps, Markdown does not
     * emphasize a text surrounded by spaces
     * @throws java.lang.Exception
     */
    public void testMarkupHasNoInnerSpace(  ) throws Exception
    {
        String strDocument = convert( "before <strong>  bold text \n            </strong> after" );

        assertTrue( strDocument.contains( "before **bold text** after" ) );
    }

    /**
     * Test that a paragraph is not indented, Markdown renders an indented line
     * as a code block
     * @throws java.lang.Exception
     */
    public void testParagraphIsNotIndented(  ) throws Exception
    {
        String strXDoc = "<document><body><section name=\"Section\">\n    <p>\n        Lorem ipsum\n    </p>\n" +
            "</section></body></document>";
        String strDocument = XDoc2MarkdownService.convert( ARTIFACT_ID, REPOSITORY,
            new ByteArrayInputStream( strXDoc.getBytes( StandardCharsets.UTF_8 ) ) );

        assertTrue( strDocument.contains( "\nLorem ipsum" ) );
        assertFalse( strDocument.contains( "\n    Lorem ipsum" ) );
    }

    /**
     * Test that the table cells are not altered
     * @throws java.lang.Exception
     */
    public void testTableCells(  ) throws Exception
    {
        String strDocument = convert( "<table><tr><th>Column Title 1</th><th>Column Title 2</th></tr>" +
            "<tr><td>Row 1 - Column 1</td><td>Row 1 - Column 2</td></tr></table>" );

        assertTrue( strDocument.contains( "| Column Title 1| Column Title 2|" ) );
        assertTrue( strDocument.contains( "| Row 1 - Column 1| Row 1 - Column 2|" ) );
    }

    /**
     * Test that a list item is not altered
     * @throws java.lang.Exception
     */
    public void testListItems(  ) throws Exception
    {
        String strDocument = convert( "<ul>\n    <li>List item 1</li>\n    <li>List item 2</li>\n</ul>" );

        assertTrue( strDocument.contains( "\n* List item 1" ) );
        assertTrue( strDocument.contains( "\n* List item 2" ) );
    }

    /**
     * Convert an xDoc body content into a Markdown document
     * @param strContent The content of the xDoc paragraph
     * @return The Markdown document
     * @throws java.lang.Exception
     */
    private static String convert( String strContent ) throws Exception
    {
        String strXDoc = "<document><body><section name=\"Section\"><p>" + strContent +
            "</p></section></body></document>";

        return XDoc2MarkdownService.convert( ARTIFACT_ID, REPOSITORY,
            new ByteArrayInputStream( strXDoc.getBytes( StandardCharsets.UTF_8 ) ) );
    }

    /**
     * Count the occurrences of a string
     * @param strDocument The document
     * @param strSearched The searched string
     * @return The occurrence count
     */
    private static int countOccurrences( String strDocument, String strSearched )
    {
        int nCount = 0;
        int nIndex = strDocument.indexOf( strSearched );

        while ( nIndex != -1 )
        {
            nCount++;
            nIndex = strDocument.indexOf( strSearched, nIndex + strSearched.length(  ) );
        }

        return nCount;
    }
}

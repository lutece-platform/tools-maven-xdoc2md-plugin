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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XDoc2MarkdownHandler
 */
public class XDoc2MarkdownHandler extends DefaultHandler
{
    private static final String TAG_BODY = "body";
    private static final String TAG_SECTION = "section";
    private static final String TAG_SUBSECTION = "subsection";
    private static final String TAG_STRONG = "strong";
    private static final String TAG_BOLD = "b";
    private static final String TAG_EM = "em";
    private static final String TAG_CODE = "code";
    private static final String TAG_PARAGRAPH = "p";
    private static final String TAG_PRE = "pre";
    private static final String TAG_LI = "li";
    private static final String TAG_UL = "ul";
    private static final String TAG_TR = "tr";
    private static final String TAG_TH = "th";
    private static final String TAG_TD = "td";
    private static final String TAG_TABLE = "table";
    private static final String TAG_ANCHOR = "a";
    private static final String TAG_IMAGE = "img";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_SRC = "src";
    private static final String ATTRIBUTE_ALT = "alt";
    private static final String URL_XDOC = "http://dev.lutece.paris.fr/plugins/";
    private static final String FOOTER =
        "\n\n *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*";
    private StringBuilder _sbDocument = new StringBuilder(  );
    private boolean _bBody;
    private boolean _bPRE;
    private int _nTableRowCount;
    private int _nTableColumnCount;
    private String _strLink;
    private boolean _bAnchor;
    private String _strArtifactId;

    /**
     * Constructor
     * @param strArtifactId The artifact id
     */
    public XDoc2MarkdownHandler( String strArtifactId )
    {
        _strArtifactId = strArtifactId;
    }

    /**
     * Returns The MD Document
     *
     * @return The MD Document
     */
    public String getDocument(  )
    {
        return _sbDocument.toString(  );
    }

    /**
     * {@inheritDoc }
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes )
                      throws SAXException
    {
        if ( qName.equalsIgnoreCase( TAG_BODY ) )
        {
            _bBody = true;
        } 
        else if ( qName.equalsIgnoreCase( TAG_SECTION ) )
        {
            _sbDocument.append( "\n#" ).append( attributes.getValue( ATTRIBUTE_NAME ) ).append( "\n" );
        } 
        else if ( qName.equalsIgnoreCase( TAG_SUBSECTION ) )
        {
            _sbDocument.append( "\n##" ).append( attributes.getValue( ATTRIBUTE_NAME ) ).append( "\n" );
        } 
        else if ( qName.equalsIgnoreCase( TAG_STRONG ) || qName.equalsIgnoreCase( TAG_BOLD ) )
        {
            _sbDocument.append( " **" );
        } 
        else if ( qName.equalsIgnoreCase( TAG_EM ) )
        {
            _sbDocument.append( " *" );
        } 
        else if ( qName.equalsIgnoreCase( TAG_CODE ) )
        {
            _sbDocument.append( " `" );
        } 
        else if ( qName.equalsIgnoreCase( TAG_PARAGRAPH ) )
        {
            _sbDocument.append( "\n" );
        } 
        else if ( qName.equalsIgnoreCase( TAG_PRE ) )
        {
            _sbDocument.append( "\n```\n" );
            _bPRE = true;
        } 
        else if ( qName.equalsIgnoreCase( TAG_LI ) )
        {
            _sbDocument.append( "\n* " );
        } 
        else if ( qName.equalsIgnoreCase( TAG_UL ) )
        {
            _sbDocument.append( "\n " );
        } 
        else if ( qName.equalsIgnoreCase( TAG_TABLE ) )
        {
            _sbDocument.append( "\n" );
            _nTableRowCount = 0;
            _nTableColumnCount = 0;
        } 
        else if ( qName.equalsIgnoreCase( TAG_TR ) )
        {
            _nTableRowCount++;
        } 
        else if ( qName.equalsIgnoreCase( TAG_TH ) )
        {
            _sbDocument.append( "| " );
            _nTableColumnCount++;
        } 
        else if ( qName.equalsIgnoreCase( TAG_TD ) )
        {
            _sbDocument.append( "| " );
            _nTableColumnCount++;
        } 
        else if ( qName.equalsIgnoreCase( TAG_ANCHOR ) )
        {
            _bAnchor = true;
            _strLink = attributes.getValue( ATTRIBUTE_HREF );
        } 
        else if ( qName.equalsIgnoreCase( TAG_IMAGE ) )
        {
            _sbDocument.append( getImage( attributes.getValue( ATTRIBUTE_SRC ),
                                          attributes.getValue( ATTRIBUTE_ALT ) ) );
        }
    }

    /**
     * {@inheritDoc }
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void endElement( String uri, String localName, String qName )
                    throws SAXException
    {
        if ( qName.equalsIgnoreCase( TAG_STRONG ) || qName.equalsIgnoreCase( TAG_BOLD ) )
        {
            _sbDocument.append( "** " );
        } 
        else if ( qName.equalsIgnoreCase( TAG_EM ) )
        {
            _sbDocument.append( "* " );
        } 
        else if ( qName.equalsIgnoreCase( TAG_CODE ) )
        {
            _sbDocument.append( "` " );
        } 
        else if ( qName.equalsIgnoreCase( TAG_PARAGRAPH ) )
        {
            _sbDocument.append( "\n" );
        } 
        else if ( qName.equalsIgnoreCase( TAG_PRE ) )
        {
            _sbDocument.append( "\n```\n" );
            _bPRE = false;
        } 
        else if ( qName.equalsIgnoreCase( TAG_UL ) )
        {
            _sbDocument.append( "\n" );
        } 
        else if ( qName.equalsIgnoreCase( TAG_TR ) )
        {
            _sbDocument.append( "|\n" );

            if ( _nTableRowCount == 1 )
            {
                for ( int i = 0; i < _nTableColumnCount; i++ )
                {
                    _sbDocument.append( "|-----------------" );
                }

                _sbDocument.append( "|\n" );
            }
        } 
        else if ( qName.equalsIgnoreCase( TAG_ANCHOR ) )
        {
            _bAnchor = false;
        } 
        else if ( qName.equalsIgnoreCase( TAG_BODY ) )
        {
            _sbDocument.append("\n\n[Maven documentation and reports](" + URL_XDOC).append(_strArtifactId).append("/)\n\n");
            _sbDocument.append( FOOTER );
        }
    }

    /**
     * {@inheritDoc }
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void characters( char[] ch, int start, int length )
                    throws SAXException
    {
        if ( _bAnchor )
        {
            _sbDocument.append( " [" ).append( new String( ch, start, length ).trim(  ) ).append( "](" )
                       .append( _strLink.trim(  ) ).append( ") " );
        } else if ( _bBody )
        {
            String strText = new String( ch, start, length );

            if ( ! _bPRE )
            {
                strText = strText.trim(  ).replaceAll( "\\s+", " " );
            }

            _sbDocument.append( strText );
        }
    }

    /**
     * Build the image code
     * @param strSource The image source url
     * @param strAlt The alternate text
     * @return The image code
     */
    private String getImage( String strSource, String strAlt )
    {
        String strUrl = strSource;
        String strText = ( strAlt != null ) ? strAlt : "";

        if ( ! strSource.startsWith( "http" ) )
        {
            strUrl = URL_XDOC + _strArtifactId + "/" + strSource;
        }

        return "![" + strText + "](" + strUrl + ")";
    }
}

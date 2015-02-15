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
 *
 * @author pierre
 */
public class XDoc2MarkdownHandler extends DefaultHandler
{
    private static final String TAG_TITLE = "title";
    private static final String TAG_SECTION = "section";
    private static final String TAG_SUBSECTION = "subsection";
    private static final String TAG_STRONG = "strong";
    private static final String TAG_BOLD = "b";
    private static final String TAG_EM = "em";
    private static final String TAG_PRE = "pre";
    private static final String TAG_LI = "li";
    private static final String TAG_UL = "ul";
    private static final String TAG_TR = "tr";
    private static final String TAG_TH = "th";
    private static final String TAG_TD = "td";
    private static final String TAG_TABLE = "table";
    private static final String ATTRIBUTE_NAME = "name";
    private StringBuilder _sbDocument = new StringBuilder();
    private boolean _bTitle;
    private boolean _bPRE;
    private int _nTableRowCount;
    private int _nTableColumnCount;

    /**
     * Returns The MD Document
     * @return The MD Document
     */
    public String getDocument(  )
    {
        return _sbDocument.toString();
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes )
        throws SAXException
    {
        if ( qName.equalsIgnoreCase( TAG_TITLE ) )
        {
            _bTitle = true;
        }
        else if ( qName.equalsIgnoreCase( TAG_SECTION ) )
        {
           _sbDocument.append( "\n##").append( attributes.getValue( ATTRIBUTE_NAME )).append("\n");
        }
        else if ( qName.equalsIgnoreCase( TAG_SUBSECTION ))
        {
           _sbDocument.append( "\n###").append( attributes.getValue( ATTRIBUTE_NAME )).append("\n");
        }
        else if ( qName.equalsIgnoreCase( TAG_STRONG ) || qName.equalsIgnoreCase( TAG_BOLD ))
        {
           _sbDocument.append( " **" );
        }
        else if ( qName.equalsIgnoreCase( TAG_EM ) )
        {
           _sbDocument.append( " *" );
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
            _nTableRowCount ++;
       }
        else if ( qName.equalsIgnoreCase( TAG_TH ))
        {
           _sbDocument.append( "| " );
           _nTableColumnCount ++;
        }
        else if ( qName.equalsIgnoreCase( TAG_TD ))
        {
           _sbDocument.append( "| " );
           _nTableColumnCount ++;
       }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        if ( qName.equalsIgnoreCase( TAG_TITLE ) )
        {
            _bTitle = false;
        }
        else if ( qName.equalsIgnoreCase( TAG_STRONG ) || qName.equalsIgnoreCase( TAG_BOLD ))
        {
           _sbDocument.append( "** " );
        }
        else if ( qName.equalsIgnoreCase( TAG_EM ) )
        {
           _sbDocument.append( "* " );
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
           if( _nTableRowCount == 1 )
           {
               for( int i = 0 ; i < _nTableColumnCount ; i++ )
               {
                   _sbDocument.append( "|-----------------" );
               }
               _sbDocument.append( "|\n" );
           }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void characters( char[] ch, int start, int length )
        throws SAXException
    {
        if ( _bTitle )
        {
            _sbDocument.append( "\n#" ).append( new String( ch, start, length ).trim() ).append( "\n" );
        }
        else
        {
            String strText = new String( ch, start, length );
            if( ! _bPRE )
            {
                strText = strText.trim().replaceAll("\\s+", " ");
            }
            _sbDocument.append( strText );
        }
    }
}


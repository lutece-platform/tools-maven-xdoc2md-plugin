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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * XDoc2MarkdownMojo
 *
 */
@Mojo( name = "readme" )
public class XDoc2MarkdownMojo extends AbstractMojo
{
    @Override
    public void execute(  ) throws MojoExecutionException, MojoFailureException
    {
        getLog(  ).info( "Create or update file README.md from src/site/xdoc/index.xml" );

        MavenProject project = (MavenProject) getPluginContext(  ).get( "project" );
        String strBaseDir = project.getBasedir(  ).getAbsolutePath(  );
        getLog(  ).info( "Basedir :" + strBaseDir );

        String strInput = strBaseDir + File.separator + "src/site/xdoc/index.xml";
        String strOutput = strBaseDir + File.separator + "README.md";
        transform( project.getArtifactId(  ), project.getScm().getUrl() , strInput, strOutput );
    }

    /**
     * Transform an xDoc to MD file
     *
     * @param strArtifactId The artifact ID
     * @param strScmUrl The SCM Url
     * @param strInput The input file path
     * @param strOutput The output file path
     */
    private void transform( String strArtifactId, String strScmUrl, String strInput, String strOutput)
    {
        try
        {
            String strRepository = getRepositoryName( strScmUrl );
            String strDocument = XDoc2MarkdownService.convert( strArtifactId, strRepository , new FileInputStream( strInput ) );
            BufferedWriter writer = new BufferedWriter( new FileWriter( strOutput ) );
            writer.write( strDocument );
            writer.close(  );
            getLog(  ).info( strDocument );
        } 
        catch ( ParserConfigurationException ex )
        {
            getLog(  ).error( ex.getMessage(  ), ex );
        } 
        catch ( SAXException ex )
        {
            getLog(  ).error( ex.getMessage(  ), ex );
        } 
        catch ( FileNotFoundException ex )
        {
            getLog(  ).error( ex.getMessage(  ), ex );
        } 
        catch ( IOException ex )
        {
            getLog(  ).error( ex.getMessage(  ), ex );
        }
    }
    
    /**
     * Extracts the repository name from the repository URL
     * @param strScmUrl the repository URL
     * @return the repository name
     */
    private String getRepositoryName( String strScmUrl )
    {
        int nPos = strScmUrl.lastIndexOf( '/' );
        return strScmUrl.substring( nPos + 1 );
        
    }
}

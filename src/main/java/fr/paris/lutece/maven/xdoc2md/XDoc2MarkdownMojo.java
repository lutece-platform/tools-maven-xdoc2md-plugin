/*
 * Copyright (c) 2002-2019, Mairie de Paris
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

/**
 * XDoc2MarkdownMojo
 * Maven Goal to convert xDoc documentation into README file in markdown format
 *
 */
@Mojo(name = "readme")
public class XDoc2MarkdownMojo extends AbstractMojo
{

    private static final String XDOC_PATH = "/src/site";
    private static final String XDOC_DIR = "/xdoc/";
    private static final String XDOC_DIR_NAME = "xdoc";
    private static final String XDOC_FILE = "index.xml";
    private static final String README_FILE = "README.md";

    /**
     * Execute the goal 'readme' on the current project
     * @throws MojoExecutionException if an error occurs
     * @throws MojoFailureException if an error occurs
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "========================================================================" );
        getLog().info( "         ____             ____  __  __ ____");  
        getLog().info( "   __  _|  _ \\  ___   ___|___ \\|  \\/  |  _ \\ ");
        getLog().info( "   \\ \\/ / | | |/ _ \\ / __| __) | |\\/| | | | |" );
        getLog().info( "    >  <| |_| | (_) | (__ / __/| |  | | |_| |" );
        getLog().info( "   /_/\\_\\____/ \\___/ \\___|_____|_|  |_|____/" );
        getLog().info( "   Create Markdown README files from xDoc documentation" );
        getLog().info( "========================================================================" );
        getLog().info( "------------------------------------------------------------------------" );
        getLog().info( "Create or update the README.md file" );
        getLog().info( "------------------------------------------------------------------------" );

        MavenProject project = (MavenProject) getPluginContext().get( "project" );
        String strBaseDir = project.getBasedir().getAbsolutePath();
        getLog().info( "Basedir :" + strBaseDir );

        String strScmUrl = getScmUrl( project );

        String strInput = strBaseDir + File.separator + XDOC_PATH + File.separator + XDOC_DIR + XDOC_FILE;
        String strOutput = strBaseDir + File.separator + README_FILE;
        transform( project.getArtifactId(), strScmUrl, strInput, strOutput );

        // Localized documentation
        for( String strLocale : getLocales( strBaseDir + XDOC_PATH ) )
        {
            getLog().info( "------------------------------------------------------------------------" );
            getLog().info( "Create or update documentation for locale: " + strLocale );
            getLog().info( "------------------------------------------------------------------------" );
            getLog().info( "Localized documentation directory: " + strBaseDir + XDOC_PATH + File.separator + strLocale );
            strInput = strBaseDir + XDOC_PATH + File.separator + strLocale + XDOC_DIR + XDOC_FILE;
            strOutput = strBaseDir + File.separator + "README." + strLocale + ".md";
            transform( project.getArtifactId(), strScmUrl, strInput, strOutput );
        }
    }

    /**
     * Gets the SCM url of the project. The url is required to build the documentation
     * links and the build status badge, so a missing declaration is an error.
     * Package visibility for unit testing.
     * @param project The Maven project
     * @return The SCM url
     * @throws MojoExecutionException if the POM declares no SCM url
     */
    String getScmUrl( MavenProject project ) throws MojoExecutionException
    {
        String strScmUrl = ( project.getScm() != null ) ? project.getScm().getUrl() : null;

        if ( strScmUrl == null || strScmUrl.trim().isEmpty() )
        {
            throw new MojoExecutionException( "No <scm><url> declared in the POM of " + project.getArtifactId()
                    + ". This url is required to build the documentation links and the build status badge." );
        }

        return strScmUrl;
    }

    /**
     * Search for localized documentation directories from a root directory
     * @param strDocumentationRootDir The documentation root directory
     * @return The locale list
     */
    List<String> getLocales( String strDocumentationRootDir )
    {
        try ( Stream<Path> paths = Files.list( Paths.get( strDocumentationRootDir ) ) )
        {
            return paths.filter( Files::isDirectory )
                    .map( path -> path.getFileName().toString() )
                    .filter( XDoc2MarkdownMojo::isLanguage )
                    .filter( strLocale -> Files.isReadable(
                            Paths.get( strDocumentationRootDir, strLocale, XDOC_DIR_NAME, XDOC_FILE ) ) )
                    .sorted()
                    .collect( Collectors.toList() );
        }
        catch( IOException ex )
        {
            getLog().error( ex.getMessage(), ex );
            return new ArrayList<>();
        }
    }

    /**
     * Checks that a directory name is an ISO 639 language code
     * @param strName The directory name
     * @return true if the name is a language code, false otherwise
     */
    private static boolean isLanguage( String strName )
    {
        return Arrays.asList( Locale.getISOLanguages() ).contains( strName );
    }

    /**
     * Transform an xDoc to MD file
     *
     * @param strArtifactId The artifact ID
     * @param strScmUrl The SCM Url
     * @param strInput The input file path
     * @param strOutput The output file path
     * @throws MojoExecutionException if the file can not be read, converted or written
     */
    void transform( String strArtifactId, String strScmUrl, String strInput, String strOutput )
            throws MojoExecutionException
    {
        try ( InputStream input = new FileInputStream( strInput ) )
        {
            String strRepository = getRepositoryName( strScmUrl );
            String strDocument = XDoc2MarkdownService.convert( strArtifactId, strRepository, input );

            try ( Writer writer = Files.newBufferedWriter( Paths.get( strOutput ), StandardCharsets.UTF_8 ) )
            {
                writer.write( strDocument );
            }

            getLog().info( strDocument );
        }
        catch( ParserConfigurationException | SAXException | IOException ex )
        {
            throw new MojoExecutionException( "Unable to generate " + strOutput + " from " + strInput
                    + " : " + ex.getMessage(), ex );
        }
    }

    /**
     * Extracts the repository name from the repository URL
     *
     * @param strScmUrl the repository URL
     * @return the repository name
     */
    private String getRepositoryName( String strScmUrl )
    {
        int nPos = strScmUrl.lastIndexOf( '/' );
        return strScmUrl.substring( nPos + 1 );

    }
}

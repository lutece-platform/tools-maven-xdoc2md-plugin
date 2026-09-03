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

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * XDoc2MarkdownMojo unit tests
 */
public class XDoc2MarkdownMojoTest
    extends TestCase
{
    private static final String ARTIFACT_ID = "artifactId";
    private static final String SCM_URL = "https://github.com/lutece-platform/lutece-core.git";

    /**
     * Test the SCM url reading
     * @throws java.lang.Exception
     */
    public void testGetScmUrl(  ) throws Exception
    {
        Scm scm = new Scm(  );
        scm.setUrl( SCM_URL );

        assertEquals( SCM_URL, new XDoc2MarkdownMojo(  ).getScmUrl( getProject( scm ) ) );
    }

    /**
     * Test that a missing SCM declaration gives an explicit error instead of a
     * NullPointerException
     */
    public void testGetScmUrlWithoutScmDeclaration(  )
    {
        assertScmUrlFailure( null );

        Scm scmWithoutUrl = new Scm(  );
        assertScmUrlFailure( scmWithoutUrl );

        Scm scmWithEmptyUrl = new Scm(  );
        scmWithEmptyUrl.setUrl( "  " );
        assertScmUrlFailure( scmWithEmptyUrl );
    }

    /**
     * Test that the documentation is generated
     * @throws java.lang.Exception
     */
    public void testTransform(  ) throws Exception
    {
        Path directory = Files.createTempDirectory( "xdoc2md" );
        File output = new File( directory.toFile(  ), "README.md" );
        String strInput = new File( XDoc2MarkdownMojoTest.class.getResource( "/xdoc/index.xml" ).toURI(  ) )
            .getAbsolutePath(  );

        new XDoc2MarkdownMojo(  ).transform( ARTIFACT_ID, SCM_URL, strInput, output.getAbsolutePath(  ) );

        assertTrue( output.exists(  ) );

        String strDocument = new String( Files.readAllBytes( output.toPath(  ) ), StandardCharsets.UTF_8 );
        assertTrue( strDocument.contains( "```java" ) );
    }

    /**
     * Test that the documentation is written in UTF-8, whatever the default charset
     * of the platform is. The output used to be written with the platform charset,
     * which mangled the accented characters of the French documentation.
     * @throws java.lang.Exception
     */
    public void testTransformWritesUtf8(  ) throws Exception
    {
        Path directory = Files.createTempDirectory( "xdoc2md" );
        Path input = directory.resolve( "index.xml" );
        Files.write( input, ( "<?xml version=\"1.0\" encoding=\"UTF-8\"?><document><body>" +
            "<section name=\"Créé à Paris\"><p>Fonctionnalité déjà présente</p></section>" +
            "</body></document>" ).getBytes( StandardCharsets.UTF_8 ) );

        File output = new File( directory.toFile(  ), "README.md" );
        new XDoc2MarkdownMojo(  ).transform( ARTIFACT_ID, SCM_URL, input.toString(  ), output.getAbsolutePath(  ) );

        String strDocument = new String( Files.readAllBytes( output.toPath(  ) ), StandardCharsets.UTF_8 );
        assertTrue( strDocument.contains( "Créé à Paris" ) );
        assertTrue( strDocument.contains( "Fonctionnalité déjà présente" ) );
    }

    /**
     * Test that a conversion error fails the build instead of leaving it green
     */
    public void testTransformFailsOnMissingInputFile(  )
    {
        try
        {
            new XDoc2MarkdownMojo(  ).transform( ARTIFACT_ID, SCM_URL, "/does/not/exist/index.xml",
                new File( System.getProperty( "java.io.tmpdir" ), "README.md" ).getAbsolutePath(  ) );
            fail( "A missing xDoc file must fail the build" );
        }
        catch( MojoExecutionException ex )
        {
            assertTrue( ex.getMessage(  ).contains( "index.xml" ) );
        }
    }

    /**
     * Test that only the ISO language directories holding a documentation file
     * are detected as localized documentation
     * @throws java.lang.Exception
     */
    public void testGetLocales(  ) throws Exception
    {
        Path root = Files.createTempDirectory( "site" );
        addDocumentation( root, "fr" );
        addDocumentation( root, "en" );
        addDocumentation( root, "zz" );
        Files.createDirectories( root.resolve( "de" ) );
        Files.createDirectories( root.resolve( "xdoc" ) );

        List<String> listLocales = new XDoc2MarkdownMojo(  ).getLocales( root.toString(  ) );

        assertEquals( 2, listLocales.size(  ) );
        assertEquals( "en", listLocales.get( 0 ) );
        assertEquals( "fr", listLocales.get( 1 ) );
    }

    /**
     * Test that an unreadable documentation directory does not break the goal
     * @throws java.lang.Exception
     */
    public void testGetLocalesWithoutSiteDirectory(  ) throws Exception
    {
        assertTrue( new XDoc2MarkdownMojo(  ).getLocales( "/does/not/exist" ).isEmpty(  ) );
    }

    /**
     * Asserts that the SCM declaration is rejected
     * @param scm The SCM declaration
     */
    private static void assertScmUrlFailure( Scm scm )
    {
        try
        {
            new XDoc2MarkdownMojo(  ).getScmUrl( getProject( scm ) );
            fail( "A missing SCM url must fail the build" );
        }
        catch( MojoExecutionException ex )
        {
            assertTrue( ex.getMessage(  ).contains( ARTIFACT_ID ) );
        }
    }

    /**
     * Build a project with the given SCM declaration
     * @param scm The SCM declaration
     * @return The project
     */
    private static MavenProject getProject( Scm scm )
    {
        Model model = new Model(  );
        model.setArtifactId( ARTIFACT_ID );
        model.setScm( scm );

        return new MavenProject( model );
    }

    /**
     * Add a localized documentation file into a documentation root directory
     * @param root The documentation root directory
     * @param strLocale The locale
     * @throws java.io.IOException
     */
    private static void addDocumentation( Path root, String strLocale ) throws IOException
    {
        Path directory = root.resolve( strLocale ).resolve( "xdoc" );
        Files.createDirectories( directory );
        Files.createFile( directory.resolve( "index.xml" ) );
    }
}

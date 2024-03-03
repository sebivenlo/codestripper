/*
 * Copyright 2006-2020 Fontys Hogeschool voor Techniek en Logistiek.
 * Version $Id: CodeStripper.java 27 2018-05-23 06:23:32Z 879417 $
 */
package org.fontysvenlo.codestripper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * CodeStripper acts as a filter and removes code between start and end tag.
 *
 * <p>
 * CodeStripper is an ant task and extends the ant...MatchingTask, which allows
 * selection of files to work on by means of the file matching pattern similar
 * as those available in the copy and zip ant-tasks such as "**&#47;*.java". The
 * purpose is to stripCode files from text (code) between start and end tags.
 * Its initial use is to remove solutions from exam files.
 *
 * The following properties are available: <table border='1'
 * style='border-collapse:collapse'> <tr><th>property</th><th
 * width='60%;valign:top'>Description</th><th>default
 * value</th><th>required</th></tr> <tr><td>deletelines</td><td>deletes lines if
 * set. Otherwise replaces original lines with <code>newline</code>-characters
 * in output file.
 * <td>false</td><td>no</td></tr> <tr><td>dir</td><td>input
 * directory</td><td>"."</td><td>no</td></tr> <tr><td>todir</td><td>output
 * dir.</td><td>"out"</td><td>no</td></tr> <tr><td>starttags</td><td> start
 * stripping tokens. Codestripper supports pairs of start and end
 * tags.</td><td><code>^\\s&lowast;//Start Solution</code></td><td>no</td></tr>
 * <tr><td>endtag</td><td>end stripping
 * tokens.</td><td><code>^\\s&lowast;//End Solution</code>.</td><td>no</td></tr>
 * <tr><td>dryRun</td><td>dryRun to test to test ant
 * task.</td><td>false</td><td>no</td></tr> <tr><td>stripCode</td><td>stripCode;
 * strips when true</td><td>true</td><td>no</td></tr>
 * <tr><td>replaceTag</td><td>This allows means to replace the start and end
 * lines with some other text, typically with <code>//TODO</code> for start and
 * <code>return 0;</code> for end. <br>Any leading white space (indentation) is
 * preserved.</td> <td><code>::replacewith::</code></td><td>no</td></tr>
 * <tr><td>verbose</td><td>Generate some output to stderr on files and start and
 * end tags found</td> <td>true</td><td>no</td></tr>
 * <tr><td>invert</td><td>Inverts operation, i.e. strips all that is OUTSIDE the
 * tags.</td><td>false</td><td>no</td></tr>
 * <tr><td>transformtags</td><td>Apply tag replacement on the start and end tags
 * as indicated above.<br>If transfromtags is false, the tags are copied
 * verbatim to the output.</td><td>true</td><td>no</td></tr>
 *
 * </table>
 *
 *
 * <p>
 * Start and end tags are used as regular expression to move between the states
 * Forwarding and Skipping. The tags should come in pairs, in order of their
 * definition in the starttag and endtag definition, i.e. starttag[0] pairs with
 * endtag[0] and so on.
 *
 * <p>
 * @author Pieter van den Hombergh {@code p.vandenhombergh@fontys.nl}
 * @author Ferd van Odenhoven      {@code f.vanodenhoven@fontys.nl}
 * @version $Revision: 27 $
 */
public class CodeStripper extends MatchingTask {

    /**
     * start token
     */
    private String[] startTag = { "^\\s*//Start Solution.*?" };
    /**
     * end token
     */
    private String[] endTag = { "^\\s*//End Solution.*?" };
    /**
     * replace token
     */
    private String replaceTag = "::replacewith::";
    /**
     * Set if lines should be removed instead of stripped (emptied).
     */
    private boolean deleteLines = false;
    /**
     * print output per processed file
     */
    private boolean verbose = true;
    /**
     *
     */
    private boolean inverse = false;
    private boolean transformtags = true;

    /**
     * Initialise state.
     */
    public CodeStripper() {
        super();
        skipper = new Skipper();
        forwarder = new Forwarder();
        passthrough = new PassThrough();
    }
    /**
     * The forwarder state instance
     */
    private StripperState forwarder = new Forwarder();
    /**
     * The skipper state instance
     */
    private final StripperState skipper;
    /**
     * passthrough state instance
     */
    private final PassThrough passthrough;
    /**
     * The current state
     */
    private StripperState state = forwarder;
    /**
     * The input directory
     */
    private File dir = new File( "." );
    /**
     * The output directory
     */
    private File toDir = new File( "out" );
    /**
     * The current output file. used by state, for which this is the context
     */
    private PrintStream outStream;
    /**
     * For debugging, If set, files are not processed but only filenames are
     * shown
     */
    private boolean dryRun = false;
    /**
     * weather to apply stripping or not
     */
    private boolean stripCode = true;

    /**
     * @return the dryRun
     */
    public final boolean isDryRun() {
        return dryRun;
    }

    /**
     * @param dryRun the dryRun to set
     */
    public final void setDryRun( boolean dryRun ) {
        this.dryRun = dryRun;
    }

    /*
     * (non-Javadoc) @see org.apache.tools.ant.Task#execute()
     */
    @Override
    @SuppressWarnings( "CallToThreadDumpStack" )
    public void execute() {
        // <editor-fold defaultstate="expanded" desc="EXAM DESCRIPTION X; POINTS Y; EARNED Z ">

        //Start Solution::replacewith:://TODO
        if ( inverse ) { // swap start and end.
            for ( int i = 0; i < startTag.length; i++ ) {
                String t = this.startTag[ i ];
                this.startTag[ i ] = this.endTag[ i ];
                this.endTag[ i ] = t;
            }
        }
        startPatterns = new Pattern[ startTag.length ];
        for ( int i = 0; i < startTag.length; i++ ) {
            startPatterns[ i ] = Pattern.compile( startTag[ i ] );
        }
        endPatterns = new Pattern[ endTag.length ];
        for ( int i = 0; i < endTag.length; i++ ) {
            endPatterns[ i ] = Pattern.compile( endTag[ i ] );
        }
        pst = startPatterns[ 0 ];
        pet = endPatterns[ 0 ];
        rep = Pattern.compile( ".*?" + replaceTag + ".*?" );
        setStartState();
        DirectoryScanner ds = this.getDirectoryScanner( dir );
        String[] files = ds.getIncludedFiles();
        File outFile;
        System.out.println( "source dir=" + dir );
        System.out.println( "Skipping between \"" + Arrays.deepToString(
                startTag ) + "\" and \""
                + Arrays.deepToString( endTag ) + "\"" );
        String ifDir = dir.toString();
        for ( String filename : files ) {
            String line;
            BufferedReader inputStream = null;
            String ofFile = this.buildOutFilename( filename );
            String inFile = ifDir + "/" + filename;
            if ( this.verbose ) {
                System.err.print( "stripping file " + inFile + "\n to "
                        + ofFile );
            }
            if ( dryRun ) {
                if ( this.verbose ) {
                    System.err.println( " dryRun" );
                }
            } else {
                if ( this.verbose ) {
                    System.err.println(); // to end line
                }
                outFile = makeOutputFile( ofFile );
                try {
                    outStream = new PrintStream( outFile );
                    setStartState();
                    inputStream = new BufferedReader( new FileReader( inFile ) );
                    while ( ( line = inputStream.readLine() ) != null ) {
                        state.handleString( line );
                    }
                } catch ( IOException ioe ) {
                    // TODO Auto-generated catch block
                    ioe.printStackTrace();
                } finally {
                    try {
                        if ( inputStream != null ) {
                            inputStream.close();
                        }
                        if ( outStream != null ) {
                            outStream.close();
                        }
                    } catch ( IOException e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        //End Solution::replacewith::return;
        // </editor-fold>
    }

    /**
     * Build a String from toDir+file separator+arg.
     * <p/>
     * @param arg tail of filename path
     */
    private String buildOutFilename( String arg ) {
        return this.toDir.toString() + "/" + arg;
    }
    //Start Solution

    /**
     * Create a File in the output dir with pathname as specified.
     * <p/>
     * @param arg pathname of file to be created
     */
    private File makeOutputFile( String arg ) {
        File result = new File( arg );
        String parent = result.getParent();
        if ( parent != null ) {
            File outDir = new File( parent );
            outDir.mkdirs();
        }
        try {
            String fileString = result.
                    getCanonicalPath().replaceFirst( toDir.getName(),
                            "\033[41;37m" + toDir.getName() + "\033[0m" );
            if ( result.createNewFile() ) {
                System.out.println( "created new file " + fileString );
            } else {
                System.out.println( "overwriting file " + fileString );
            }
        } catch ( IOException ex ) {
            Logger.getLogger( CodeStripper.class.getName() ).
                    log( Level.SEVERE, null, ex );
        }
        return result;
    }
    //End Solution

    /**
     * The println method for string
     * <p/>
     * @param s string to be printed to file
     */
    private void println( String s ) {
        outStream.println( s );
    }

    /**
     * Put a newline to file
     * <p/>
     */
    private void println() {
        outStream.println();
    }

    /**
     * Set the stripper state to a new state. Does calls exit of leaving and
     * entry of new state.
     * <p/>
     * @param s
     */
    private void setState( StripperState s ) {
        this.state.atExit();
        this.state = s;
        this.state.atEntry();
    }
    /**
     * Keep line numbers for logging.
     */
    private int lineNumber;
    /**
     * last line from input, processed on entry or exit
     */
    private String lastLineRead;
    private Pattern pst;
    private Pattern pet;
    private Pattern rep;
    private Pattern[] startPatterns;
    private Pattern[] endPatterns;

    private boolean findMatcher( String line ) {
//        System.out.
//                println( "patterns =" + Arrays.deepToString( startPatterns ) );
        for ( int i = 0; i < startPatterns.length; i++ ) {
            if ( startPatterns[ i ].matcher( line ).matches() ) {
                pst = startPatterns[ i ];
                pet = endPatterns[ i ];
                System.out.println( "Match for " + pst + " ends at " + pet );
                return true;
            }
        }
        return false;
    }

    /**
     * StatePattern abstract parent class.
     * <p/>
     * Stripperstate is the super of <code>Forwarder</code> and
     * <code>Skipper</code>. It specified the state dependent methods
     * <code>handleString()</code>, <code>atEntry</code> and <code>atExit</code>
     * and the utility method <code>replace()</code> to do the tag replacing.
     * <p/>
     * @author hom
     * <p/>
     */
    private abstract class StripperState {

        /**
         * The event handler.
         */
        void handleString( String s ) {
            lastLineRead = s;
            lineNumber++;
        }

        /**
         * on state entry
         */
        abstract void atEntry();

        /**
         * on state exit. Possibly replace string.
         */
        void atExit() {
            if ( CodeStripper.this.transformtags ) {
                replace( lastLineRead );
            } else {
                println( lastLineRead );
            }
        }

        void replace( String s ) {
            if ( rep.matcher( s ).matches() ) {
                String tail = s.substring( s.indexOf( replaceTag ) + replaceTag.
                        length() );
                StringBuilder sb = new StringBuilder();
                int i = 0;
                char c = s.charAt( i );
                while ( Character.isWhitespace( c ) ) {
                    sb.append( c );
                    i++;
                    c = s.charAt( i );
                }
                sb.append( tail );
                println( sb.toString() );
            }
        }
    }

    /**
     * On start, reset line number and put in start state.
     * <p/>
     */
    private void setStartState() {
        if ( this.stripCode ) {
            if ( this.inverse ) {
                state = skipper;
            } else {
                state = forwarder;
            }
        } else {
            state = passthrough;
        }
        lineNumber = 1;
    }

    /**
     * Pass through class prints to outputs and scan for start token.
     * <p/>
     * @author hom
     * <p/>
     */
    private class Forwarder extends StripperState {

        @Override
        public void handleString( String in ) {
            super.handleString( in ); // sets lastLineRead
            if ( findMatcher( lastLineRead ) ) {
                if ( !pet.matcher( lastLineRead ).matches() ) {
                    setState( skipper );
                }
            } else {
                println( lastLineRead );
            }
        }

        @Override
        void atEntry() {
            if ( CodeStripper.this.transformtags ) {
                replace( lastLineRead );
            } else {
                println( lastLineRead );
            }
        }

        @Override
        void atExit() {
        }
    }

    /**
     * Skips input, possibly replacing with a bare newline and at the same time
     * scan for end token.
     * <p/>
     * @author hom
     * <p/>
     */
    private class Skipper extends StripperState {

        /**
         * The event handler.
         */
        @Override
        public void handleString( String in ) {
            super.handleString( in );
            if ( pet.matcher( lastLineRead ).matches() ) {
                setState( forwarder );
            } else if ( !deleteLines ) {
                println();
            }
        }

        @Override
        void atEntry() {
            if ( CodeStripper.this.transformtags ) {
                replace( lastLineRead );
            } else {
                println( lastLineRead );
            }
            if ( verbose ) {
                System.err.println( "start skip at line " + lineNumber );
            }
        }

        @Override
        void atExit() {
            if ( verbose ) {
                System.err.println( "stop skip at line " + lineNumber );
            }
        }
    }

    private class PassThrough extends StripperState {

        @Override
        void atEntry() {
            // nothing
        }

        @Override
        public void handleString( String in ) {
            super.handleString( in );
            println( lastLineRead );
        }
    }

    /**
     * @return the deleteLines
     */
    public final boolean isDeleteLines() {
        return deleteLines;
    }

    /**
     * @param deleteLines the deleteLines to set
     */
    public final void setDeleteLines( boolean deleteLines ) {
        this.deleteLines = deleteLines;
    }

    /**
     * @return the endTag
     */
    public final String getEndTag() {
        return String.join(",",endTag );
    }

    /**
     * @param endTag the endTag to set
     */
    public final void setEndTag( String endTag ) {
        this.endTag = endTag.split( "\\s*,\\s*" );
    }

    /**
     * @return the startTag
     */
    public final String getStartTag() {
        return String.join( ",", startTag);
    }

    /**
     * @param startTag the startTag to set
     */
    public final void setStartTag( String startTag ) {
        this.startTag = startTag.split( "\\s*,\\s*" );
    }
    //!S

    /**
     * Get the source directory of the stripCode operation
     * <p/>
     * @return the input dir
     */
    public File getDir() {
        return dir;
    }

    /**
     * Set the source directory of stripCode operation.
     * <p/>
     * @param dir
     */
    public void setDir( File dir ) {
        this.dir = dir;
    }

    /**
     * @return the toDir
     */
    public final File getToDir() {
        return toDir;
    }

    /**
     * @param toDir the toDir to set
     */
    public final void setToDir( File toDir ) {
        this.toDir = toDir;
    }

    /**
     * @return the replaceTag
     */
    public String getReplaceTag() {
        return replaceTag;
    }

    /**
     * @param replaceTag the replaceTag to set
     */
    public void setReplaceTag( String replaceTag ) {
        this.replaceTag = replaceTag;
    }

    /**
     * @return the stripCode
     */
    public boolean isStripCode() {
        return stripCode;
    }

    /**
     * @param stripCode the stripCode to set
     */
    public void setStripCode( boolean stripCode ) {
        this.stripCode = stripCode;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose( boolean verbose ) {
        this.verbose = verbose;
    }

    public void setTransformtags( boolean transform ) {
        this.transformtags = transform;
    }

    public boolean isTransformtags() {
        return this.transformtags;
    }

    public void setInverse( boolean inv ) {
        this.inverse = inv;
    }

    public boolean isInverse() {
        return this.inverse;
    }
    //!s
}

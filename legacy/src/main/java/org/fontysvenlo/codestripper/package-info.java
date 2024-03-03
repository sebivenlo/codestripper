/*
 *  Copyright Pieter van den Hombergh 2010/.
 *  Fontys Hogeschool voor Techniek en logistiek Venlo Netherlands.
 *  Software Engineering. Website: http://www.fontysvenlo.org
 *  This file may be used distributed under GPL License V2.
 */
/**
 *
 * The package codestripper provides one ant task, <code>codestripper</code>.
 *
 *
 * The purpose of codestripper is to remove code (solutions) from exam
 * questions, such that tried and tested solutions code can be stripped and used
 * as initial code for exam questions.<br/>
 *
 * As CodeStripper extends the ant MatchingTask, it supports <b>includes</b> and
 * <b>excludes</b> with filename patterns such as "**&#47;*.java" and the
 * like.<br/>
 *
 * Codestripper makes no assumptions on the source language syntax. It is the
 * users responsibility to place the tags in such a way that the source code is
 * valid before and after stripping. In most c-like languages a simple double
 * slash as line comment start is sufficient to fulfil this requirement.
 *
 * The codestripper supports a number of properties to define start and end
 * tags, whether to remove the lines or replaces them with empty lines and to
 * replace start an end tags with in code replacements, mainly to keep the
 * compiler happy with the remaining code.<br/> Properties: <table border='1'
 * style='border-collapse:collapse'>
 * <tr><th>property</th><th
 * wdith='60%'>Description</th><th>default value</th></tr>
 * <tr><td>deletelines</td><td>deletes lines if set. Otherwise replaces original
 * lines with <code>newline</code>-characters in output file.
 * <td>false</td></tr>
 * <tr><td>dir</td><td>input directory</td><td>"."</td></tr>
 * <tr><td>todir</td><td>output dir.</td><td>"out"</td></tr>
 * <tr><td>starttag</td><td> start stripping tokens. Codestripper supports
 * multiple pairs of
 * tokens.</td><td><code>^\\s&lowast;//Start&nbsp;Solution</code></td>
 * </tr> <tr><td>endtag</td><td>end stripping
 * tokens.</td><td><code>^\\s&lowast;//End&nbsp;Solution</code>.</td></tr>
 * <tr><td>dryRun</td><td>dryRun to test to test ant
 * task.</td><td>false</td></tr> <tr><td>replaceTag</td><td>This allows means to
 * replace the start and end lines with some other text, typically with
 * <code>//TODO</code> for start and <code>return 0;</code> for end. <br/>Any
 * leading white space (indentation) is preserved.</td>
 * <td><code>::replacewith::</code></td></tr>
 * <tr><td>invert</td><td>Inverts operation, i.e. strips all that is OUTSIDE the
 * tags.</td><td>false</td></tr>  <tr><td>verbose</td><td>Generate some output to
 * stderr on files and start and end tags found</td>
 * <td>true</td></tr>
 * <tr><td>transformtags</td><td>Apply tag replacement on the start and end tags
 * as indicated above.<br/>If transfromtags is false, the tags are copied
 * verbatim to the output.</td><td>true</td></tr> </table>
 *
 * <p>
 * <b>Start and end tokens should for pairs</b> such that starttag[0] goes with
 * endtag[0].</p>
 *
 *
 * <h2>A typical usage example:</h2>
 * The ant task:<br/> <pre
 * style='background:#AFF'> &lt;<FONT COLOR="#A020F0">target</FONT>
 * name="strip"&gt; &lt;<FONT COLOR="#A020F0">codestripper</FONT>
 * todir="../examproject" dir="src" deletelines="true" includes="**&#47;*.java"
 * excludes="**&#47;Parents.java" /&gt;
 * &lt;<FONT COLOR="#A020F0">/target</FONT>&gt;
 * </pre> The tags in the code (using the defaults) could look like this (the
 * <span style="text-decoration:line-through">strikethrough</span> is not in the
 * code or in the result, but rather shows what would be removed):
 * <pre style='background:#FFFFBB'>
 * <code>
 * <B><FONT COLOR="#A020F0">private</FONT></B> File makeOutputFile(String arg) {
 * <I><FONT COLOR="#B22222"><span style="text-decoration:line-through">//Start
 * Solution::replacewith::</span>//TODO
 * </FONT></I><div style="text-decoration:line-through"> File f =
 * <B><FONT COLOR="#A020F0">new</FONT></B> File(arg); String parent =
 * f.getParent(); System.out.println(f.getName()); String fullOutPath =
 * <B><FONT COLOR="#BC8F8F">&quot;out/&quot;</FONT></B> + arg;
 * <B><FONT COLOR="#A020F0">if</FONT></B> (parent !=
 * <B><FONT COLOR="#A020F0">null</FONT></B>) { System.out.println(parent); File
 * outDir = <B><FONT COLOR="#A020F0">new</FONT></B>
 * File(<B><FONT COLOR="#BC8F8F">&quot;out/&quot;</FONT></B> + parent);
 * outDir.mkdirs(); } File result = <B><FONT COLOR="#A020F0">new</FONT></B>
 * File(fullOutPath);
 * <B><FONT COLOR="#A020F0">return</FONT></B> result;</div>
 * <I><FONT COLOR="#B22222"><span style="text-decoration:line-through">//End
 * Solution::replacewith::</span>return null;
 * </FONT></I> }
 * </code>
 * </pre>
 *
 */
package org.fontysvenlo.codestripper;

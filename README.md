# CodeStripper maven plugin

**Modern code stripper as maven plugin.**

## LTDR;

To build
. clone this repo
. `cd codestripper-maven-plugin`
. `mvn install`
And if you are in real hurry
. `mvn -DskipTests install`

In the future we may deploy this as a plugin to some repository.

### Legacy ant based codestripper

If you need the legacy version, move to the legacy folder and build there.
There should be no reason to use the legacy version, because this plugin provides
all the features of the legacy version, apart from the tag syntax.

All you need to do is replace `//Start Solution::...` with `//cs:remove:start`.

For other details, see below.


## Introduction

New version of the CodeStripper that was previously used, which can be found at [https://github.com/sebivenlo/codestripper](https://github.com/sebivenlo/codestripper).
This variant of codestripper borrows the API definition from the Python implementation with one feature
 deprecated and a few features added.

It can also be used as inspiration for an assignment.

[https://github.com/FontysVenlo/codestripper](https://github.com/FontysVenlo/codestripper)
The reason for the switch for the python version is to not be dependent on Ant as a build system and the possibility to easily add more tags.

The same reasons for switching from the orginal apply, but now not being dependent on python knowledge. Even a Java teacher is able to maintain this version.


## Available tags

All transformations to lines and ranges are indentation preserving, including the include file transformation.
This implies that all lines of the included file is prefix with the indentation of the tag line.

There are two applications of the tags, line wise and range wise.
In fact all tags are available in both forms, but not useful (but not harmful either) in all cases.
This to keep the implementation simple.

Single line application

| Command | Tag(s) | Description |
|-----|-------------|------------|
| Add | `cs:add:<text>` |  Add the *text* (without the tag in front) |
| Ignore | `cs:ignore` | Ignore the entire file, only valid on the first line |
| Remove line | `cs:remove` | Remove the line |
| Replace | `cs:replace:<replacement>` | Replace the text in front by the *replacement*, keeps whitespace |
| Uncomment | `cs:uncomment` | Uncomment this line, but drop tag |
| Comment | `cs:comment` | Comment this line |
| Include | `cs:include:<filename>` | include the named file in the output replacing this line |

Range application

| Command | Tag(s) | Description |
|---------|--------|-------------|
| Remove range | `cs:remove:start`/`cs:remove:end` | Remove all text in between tags |
| Uncomment | `cs:uncomment:start`/`cs:uncomment:end` | Uncomment all lines in between tags |
| Comment | `cs:comment:start`/`cs:uncomment:end` | Uncomment all lines in between tags |

NOT or less useful in range application.

| Command | Effect |
|---------|----------------------------------------------------------|
| Include | both start and end will include the file name           |
|  , ,    | In the range each line will be interpreted as as file names | 
| replaceFirst | would apply the same substitution in all lines  in range |
| replaceAll   | would apply the all substitutions in all lines in range |


### Legacy

The compatibility with the legacy codestripper has been dropped in the version.
It would clutter a clean implementation.

~~To support the old CodeStripper, the legacy tag `Start Solution::replacewith::`/`End Solution::replacewith::` is still supported for now. This tag does both the `Remove` and `Replace` in one go.~~

The replacewith is more or less free, because the payload is preserved on start and end line.


## Command Line Properties

These will be implemented as maven plugin feautures

CodeStripper can be used as a Python Module and as a command line tool. The command line tool has the following interface.

| Flag | Long form | Description | Default value | Required |
|----------|------|-------------|---------------|----------|
| `<positional>` | None | files to include for code stripping (glob) | None | True |
| -e | --exclude | files to exclude for code stripping (glob) | None | False |
| -c | --comment | comment symbol(s) for the given language | // | False |
| -o | --output | the output directory to store the stripped files | out | False |
| -r | --recursive | do NOT use recursive globs for include/exclude | True | False |
| -v | --verbosity | increase output verbosity | None | False |
| -d | --dry | execute a dry run | False | False |
| -w | --working-directory | set the working directory for include/exclude | pwd | False |



## Examples

This section contains examples for all supported tags.

### Add //TODO

Input:
```java
public class Test {
    //cs:add:private final String test = "test";
}
```

Output:
```java
public class Test {
    private final String test = "test";
}
```

### Ignore //TODO in main?

Input:
```java
//cs:ignore
public class Test {
    private final String test = "test";
}
```

Output: No output, file is ignored

### Remove line

Input:
```java
public class Test {
    private final String test = "test";//cs:remove
}
```

Output:
```java
public class Test {
}
```

### Remove range

Input:
```java
public class Test {
    //cs:remove:start
    private final String test = "test";
    private final int count = 0;
    //cs:remove:end
    private final boolean keep = true;
}
```

Output:
```java
public class Test {
    private final boolean keep = true;
}
```

### Remove with replacement

Input:
```Java
import java.util.List;
//cs:remove:start//TODO write your solution here
import com.example.package;
import java.util.Map;
//cs:remove:end://You should solve it with two lines only.
import java.util.Set;
```

Output:
```Java
import java.util.List;
//TODO write your solution here
//You should solve it with two lines only.
import java.util.Set;
```

For a single line remove:

Input:
```Java
import java.util.List;
import com.example.package;//cs:remove://Add proper import statement
import java.util.Map;
```

Output:
```Java
import java.util.List;
//Add proper import statement
import java.util.Map;
```


### Replace

Input:
```java
public class Test {
    private final boolean keep = false;//cs:replace://TODO: add fields
}
```

Output:
```java
public class Test {
    //TODO: add fields
}
```

### Replace First

The exact documentation can be found in String#replaceFirst.

Input:
```java
public class Test {
    private final boolean keep = false;//cs:replaceFirst:/final //
}
```

Output:
```java
public class Test {
    private boolean keep = false;
}
```

### Replace All

Maybe less useful, because it will break a lot of code.
Most likely the replaceFirst transformation works just as well.

The exact documentation can be found in String#replaceAll.

Input:
```java
public class Test {
    //cs:replaceAll:start:/false/true/
    private final boolean keep = false;
    private isactive= false;
    //cs:replaceAll:end
}
```

Output:
```java
public class Test {
    private final boolean keep = true;
    private isactive= true;
}
```

### Uncomment

Input:
```java
public class Test {
//    fail( "method NopEscapes reached end. You know what to do." );//cs:uncomment
}
```

Output:
```java
public class Test {
    fail( "method NopEscapes reached end. You know what to do." );
}
```


### Uncomment Range

Input:
```java
public class Test {
    //cs:uncomment:start
    //private final String example = "example";
    //private final boolean isTestCode = true;
    //cs:uncomment:end
}
```

Output:
```java
public class Test {
    private final String example = "example";
    private final boolean isTestCode = true;
}
```


### Comment range

Input:
```java
public class Test {
    //cs:comment:start:// when finished uncomment
    private final String example = "example";
    private final boolean isTestCode = true;
    //cs:comment:end:// you did it
}
```

Note the use of a payload to replace the start line. 

Output:
```java
public class Test {
    //when finished uncomment
    //private final String example = "example";
    //private final boolean isTestCode = true;
    // you did it
}
```

### Include file

With the file 
```
"""
Humpty Dumpty sat on a wall.
Humpty Dumpty had a great fall.
All the king's horses and all the king's men
Couldn't put Humpty together again.
""";
```

Input:

```java
public class Test {
    String verse; //cs:replaceFirst:/verse;/verse=/
        //cs:include: humpty.txt
}
```

Output:
```java
public class Test {
    String verse=
        """
        Humpty Dumpty sat on a wall.
        Humpty Dumpty had a great fall.
        All the king's horses and all the king's men
        Couldn't put Humpty together again.
        """;
}


### Feature or Bug

Lines in a 'remove' range that have tags will execupte that tag.
This implies that ranges can't be nested. 
However, it can be used to advantage, for instance:
To excluding a line from removal, tag it with e.g. a nop instruction.

Input:

```Java
import java.util.List;
//cs:remove:start//TODO add your solution here
import com.example.package;
import java.util.HashMap;//cs:nop
import java.util.Map;
//cs:remove:end://You should solve it with two lines only.
import java.util.Set;
```

Output:
```Java
import java.util.List;
//TODO add your solution here
import java.util.HashMap;
//You should solve it with two lines only.
import java.util.Set;
```



## Adding a new tag

The core funtionality is built arround this regular expression, that 
decomposes a matching String into  a number of parts.

```java
public static final String myPreciousRegex
            = "(?<indent>\\s*)" //optional indentation
            + "(?<text>\\S.*)?" // anything other starting with non space
            + "(?<commentToken>//)" // mandatory comment token 
            + "cs:" // mandatory tag to match
            + "(?<instruction>\\w+)" // required instruction group
            + "(:(?<startEnd>(start|end)))?" // optional start end group
            + ":?(?<payLoad>(.*$))?" // optional  payLoad
            ;
```

The Processor in the diagram is a glorified box (a record, that is initialized
with the findings of the regular expression) and the actual functionality is
passed in as a function called transformation that converts the content of the box into a Stream of String.

The record is defined as:
```Java
public record Processor(String line, String payLoad,
        Function<Processor, Stream<String>> transformation,
        String instruction, int lineNumber, String text, String indent) implements
        Function<Processor, Stream<String>> {

    @Override
    public Stream<String> apply(Processor proc) {
        return this.transformation.apply( this );
    }
```


The parts are named as follows, applied to this sample line

```Java
    int a = 12; //cs:replaceFirst:/a/b/
```


| Name        | Content                                 | 
|-------------|-----------------------------------------| 
| indent      | '    '(4 spaces)                        |
| text        | int a = 12;                             |
| instruction | passed as the function 'transformation' |
| startEnd    | not passed, used by factory             |
| payLoad     | /a/b/                                   |

In this example the payLoad is '/a/b/'. In the Function that implements the `replaceFirst` 
transformation this is further processed. See the example below.
The instruction is looked up by the factory and is passed to the processor box
as a Function<Processor,Stream<String>>.



Additionally the following is passed

| Name            | Content                             | 
|-----------------|-------------------------------------|
| transformation  | function to apply to this processor |
| lineNumber      | for debugging purposes              | 

### example tranformation replaceFirst

```Java
static final Function<Processor, Stream<String>> replaceFirst
    = ( Processor p ) -> {
        String separator = p.payLoad().substring( 0, 1 );
        String[] split = p.payLoad().substring(1).split( separator );
        String result = p.text().replaceFirst( split[ 0 ], split[ 1 ] );
        return of( result );
};
```


We chose to make the return type to be a `Stream<String>` to be able to be to return 
0, 1 or more strings as a result of the transformation. 

0. strings is used in the case of drop (mandatory feature of a code stripper).
1. with a tranformation of of the string \
n. makes it possible to include a file of strings to replace the tagged line.


## Migrating from legacy codestripper

For all the files that contain tags, replace the tags with
this codestripper variant.

/
I would use sed to operate in the files.

*For tags that have a payload:*

The tag `//Start Solution::replaceWith::sometext` should be replaced with `//cs:remove:start:sometext`
The tag `//End Solution::replaceWith::sometext` should be replaced with `//cs:remove:end:sometext`

the sed formula for this is 
```sh
sed -i 's#//Start Solution::replaceWith::#//cs:remove:start:#' file.java
```

*For simple tags*

The tag `//Start Solution` should be replaces with `//cs:remove:start`
The tag `//End Solution` should be replaced with `//cs:remove:end`


```sh
for i in $(grep -rl 'Solution' .) ;
  do sed -i -f  migrate.sed  $i; 
done
```

where the script file is 

migrate.sed
```sed
s#//Start Solution::replacewith::#//cs:remove:start:#
s#//End Solution::replacewith::#//cs:remove:end:#
s#//Start Solution#//cs:remove:start#
s#//End Solution#//cs:remove:end#
```

Note that the payload-full versions are adapted first.

**No guarantee for this migration. Make sure you have a up to date backup of the file (version control)**

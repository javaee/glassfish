/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.amx.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ObjectName;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.base.Pathnames;
import static org.glassfish.admin.amx.base.Pathnames.*;


/**
 *
 * @author llc
 */
public final class PathnameParser {
    private static void debug(final Object o)
    {
        //System.out.println( "" + o);
    }
    
    private final char mDelim;
    private final char mNameLeft;
    private final char mNameRight;

    private final String mPath;
    private final List<PathPart> mParts;

    public static final class PathPart
    {
        private final String mType, mName;
        public PathPart(final String type, final String name)
        {
            mType = type;
            mName = name;
        }
        public String type() { return mType; }
        public String name() { return mName; }
        public String toString() { return pathPart(mType,mName);}
    }
    
    public PathnameParser(final String path)
    {
        this( path, SEPARATOR, SUBSCRIPT_LEFT, SUBSCRIPT_RIGHT);
    }
    
    public PathnameParser(
        final String path,
        final char  delim,
        final char nameLeft,
        final char nameRight)
    {
        mPath = path;
        
        mDelim     = delim;
        mNameLeft  = nameLeft;
        mNameRight = nameRight;

        mParts  = parse();
    }

    public List<PathPart>  parts()
    {
        return mParts;
    }


    /**
        FIXME: how to support arbitrary delimiter or subscript?
        
        Maybe better to do a simple parse which can do escapes, etc.
        Characters for the type are restricted to alphanumeric matchs the type portion of the path part
     */
        private static Pattern
    getTypePattern()
    {
        final String typeAloneStr = "([" + LEGAL_TYPE_CHARS + "]*)";
        final String typePatternStr = typeAloneStr + ".*";
        final Pattern typePattern = Pattern.compile(typePatternStr);
        
        return typePattern;
    }
    
    public static boolean isValidType(final String type)
    {
        final Matcher matcher = getTypePattern().matcher(type);
        return matcher.matches();
    }
    
    /**
     */
    private void parse( final String path, final List<PathPart> parts)
    {
        debug( "PathnameParser: parsing: " + path );
        if ( path == null || path.length() == 0)
        {
            throw new IllegalArgumentException(path);
        }
        final Pattern typePattern = getTypePattern();

        final String namePatternStr = "([^]]*)](.*)";
        final Pattern namePattern = Pattern.compile(namePatternStr);
        
        // how to know whether to escape the name-left char if it can be any char?
        String remaining = path;
        while ( remaining.length() != 0 )
        {
            Matcher matcher = typePattern.matcher(remaining);
            if ( ! matcher.matches() )
            {
                throw new IllegalArgumentException("No match: " + remaining);
            }

            final String type = matcher.group(1);
            debug( "PathnameParser, matched type: \"" + type + "\"" );

            char matchChar = '\0';
            if ( type.length() < remaining.length()  )
            {
                matchChar = remaining.charAt( type.length() );
                remaining = remaining.substring( type.length() + 1 );
            }
            else
            {
                break;
            }
            debug( "PathnameParser, match char: \"" + matchChar + "\"" );
            debug( "PathnameParser, remaining: \"" + remaining + "\"" );

            String name = null;
            if ( matchChar == mNameLeft )
            {
                // anything goes in a name, and we do NOT allow escaped SUBSCRIPT_RIGHT,
                // so just scarf up everything untilthe next SUBSCRIPT_RIGHT.
                final int idx = remaining.indexOf(mNameRight);
                if ( idx < 0 ) {
                    throw new IllegalArgumentException(path);
                }
                name = remaining.substring(0, idx);
                remaining = remaining.substring(idx +1);
                if ( remaining.length() != 0 && remaining.charAt(0) == mDelim )
                {
                    remaining = remaining.substring(1);
                }
            }

            final PathPart part = new PathPart(type,name);
            parts.add(part);

            debug( "PathnameParser, matched part: \"" + part + "\"" );
        }

        String s = "";
        for( final PathPart part : parts ){
            s = s + "{" + part + "}";
        }
        debug( "FINAL PARSE for : " + path + " = " + s);
    }
     
    private List<PathPart> parse()
    {
        final List<PathPart> parts = new ArrayList<PathPart>();

        parse(mPath, parts);
        
        return parts;
    }

    
    private static void checkName( final String name ) {
        if (name != null && (name.indexOf(SUBSCRIPT_LEFT) >= 0 || name.indexOf(SUBSCRIPT_RIGHT) >= 0)) {
            throw new IllegalArgumentException("Illegal name: " + name);
        }
    }

    private static void checkType( final String type ) {
        if (type == null ) {
            throw new IllegalArgumentException("Illegal type: " + type);
        }

        if ( type.indexOf(SUBSCRIPT_LEFT) >= 0 || type.indexOf(SUBSCRIPT_RIGHT) >= 0 ) {
            throw new IllegalArgumentException("Illegal type: " + type);
        }
        
        if ( ! isValidType(type) )
        {
            throw new IllegalArgumentException("Illegal type: " + type);
        }
    }

    public static String pathPart(final String type, final String name) {
        checkName(name);

        final String namePart = (name == null) ? "" : SUBSCRIPT_LEFT + name + SUBSCRIPT_RIGHT;

        final String part = pathPart(type) + namePart;
        return part;
    }

    public static String pathPart(final String type) {
        checkType(type);
        return type;
    }

    public static String path(final String parentPath, final String type, final String name) {
        if ( parentPath != null && parentPath.length() == 0 && ! type.equals(Util.deduceType(DomainRoot.class)) )
        {
            throw new IllegalArgumentException( "parent path cannot be the empty string" );
        }

        String path = (parentPath == null || parentPath.equals(DomainRoot.PATH)) ?
                DomainRoot.PATH : parentPath + Pathnames.SEPARATOR;

        path = path + pathPart(type,name);

        // make sure it can be parsed
        new PathnameParser(path);

        return path;
    }

    public static String parentPath(final ObjectName objectName) {
        return objectName.getKeyProperty(AMXConstants.PARENT_PATH_KEY);
    }
}














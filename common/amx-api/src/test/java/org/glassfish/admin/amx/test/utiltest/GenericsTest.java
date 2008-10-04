/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admin.amx.test.utiltest;

import com.sun.appserv.management.util.misc.TypeCast;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 Demo of generic do/don't issues.
 */
public final class GenericsTest
        extends junit.framework.TestCase {
    public GenericsTest() {
    }

    private static class A
            implements Serializable {
        public static final long serialVersionUID = 999;

        public A() {}
    }

    private static final class AA
            extends A {
        public static final long serialVersionUID = 9999;

        public AA() {}
    }

    /**
     commented out to avoid compiler warnings; code compiles and tests good,
     and is useful in understanding generics.

     // An example of an unchecked cast where the caller had better
     // use the correct type of Set<T>; see testCallersProblem().

     private <T extends Serializable> Set<T>
     getSetOfSerializableT()
     {
     final Set<Serializable> result    = new HashSet<Serializable>();
     result.add( new String("hello") );
     result.add( new Integer(0) );
     result.add( new Boolean(false) );

     // "warning: [unchecked] unchecked cast: found Set<String>, required Set<T>"
     // it's up to the *caller* to ensure that T is a valid type as per javadoc
     // or other semantics
     return (Set<T>)result;
     }


     // Please see how getSetOfT_ImplCast() is implemented with a cast.
     public void
     test_getSetOfSerializableT()
     {
     final Set<String>       s1  = getSetOfSerializableT();  // OK, no warning
     final Set<Integer>      s2  = getSetOfSerializableT();  // OK, no warning
     final Set<Long>         s3  = getSetOfSerializableT();  // OK, no warning
     final Set<Serializable> s4  = getSetOfSerializableT();  // OK, no warning

     // COMPILE FAILURE:
     // final Set<Object>   x  = getSetOfT();
     // final Set<?>        x  = getSetOfT();

     TypeCast.checkSet( s4, Serializable.class);

     try
     {
     TypeCast.checkSet( s1, String.class);
     assert false;
     }
     catch( final ClassCastException e )
     {
     // should arrive here
     }

     try
     {
     TypeCast.checkSet( s2, Integer.class );
     assert false;
     }
     catch( final ClassCastException e )
     {
     // should arrive here
     }

     try
     {
     TypeCast.checkSet( s3, Long.class );
     assert false;
     }
     catch( final ClassCastException e )
     {
     // should arrive here
     }
     }
     */

    /**
     Method is "good"; no warnings.
     */
    private Set<? extends Serializable>
    getSetOfSerializableUnknown() {
        final Set<Serializable> result = new HashSet<Serializable>();
        result.add(new String("hello"));
        result.add(new Integer(0));
        result.add(new Boolean(false));
        return result;
    }

    /**
     commented out to avoid compiler warnings; code compiles and tests good,
     and is useful in understanding generics.

     // Please see how getSetOfT_UnknownSubclass() is implemented.
     public void
     test_getSetOfSerializableSubclassUnknown()
     {
     // s1 is effectively read-only, since nothing can be put into it
     final Set<? extends Serializable> s1  = getSetOfSerializableUnknown();

     // warning: "unchecked cast"
     final Set<Serializable>  s2  = (Set<Serializable>)getSetOfSerializableUnknown();
     }
     */


    /**
     Method is "good"; no warnings.
     */
    private Set<Serializable>
    getSetOfSerializable() {
        final Set<Serializable> result = new HashSet<Serializable>();
        result.add(new String("hello"));
        result.add(new Integer(0));
        result.add(new Boolean(false));
        return result;
    }

    /**
     */
    public void
    test_getSetOfSerializable() {
        // all OK.  's1' can contain any Serializable
        final Set<Serializable> s1 = getSetOfSerializable();
        TypeCast.checkSet(s1, Serializable.class);

        // all OK, no warning.
        final Set<? extends Serializable> s2 = getSetOfSerializable();
        TypeCast.checkSet(s2, Serializable.class);
    }

    /*
        private <T> void
    checkValidItems( final Collection<T> c, final Class<T> theClass)
    {
        for( final T item : c )
        {
            if ( ! theClass.isAssignableFrom( item.getClass() ) )
            {
                throw new ClassCastException(
                    item.getClass() + " not assignable to " + theClass );
            }
        }
    }
    */


    public void
    testAssign() {
        final Set<Serializable> serializableSet = new HashSet<Serializable>();
        final Set<String> stringSet = new HashSet<String>();

        // these of course must work; the types match
        serializableSet.add(new Integer(0));
        serializableSet.add(new String());
        serializableSet.add(new Boolean(false));

        stringSet.add("hello");

        serializableSet.addAll(stringSet);

        // This is counterintuitive"; we just added all members of
        // 'stringSet' to 'serializableSet', but we cannot assign the Set itself:
        //      Set<Serializable> illegal = stringSet;

        // However, we can make this assignment if we use a wildcard Set,
        // no further add() calls to the set can be made.
        Set<? extends Serializable> unknownSub1 = null;
        unknownSub1 = stringSet;
        unknownSub1 = serializableSet;
    }


    public void
    testCheckedSet() {
        final Set<Object> s = new HashSet<Object>();
        TypeCast.checkSet(s, String.class);
        TypeCast.checkSet(s, Integer.class);

        s.add("hello");
        TypeCast.checkSet(s, String.class);
        TypeCast.checkSet(s, Object.class);
        TypeCast.checkSet(s, Serializable.class);

        try {
            TypeCast.checkSet(s, Integer.class);
            assert false;
        }
        catch (Exception e) {/*good, expected*/}

        final Set<String> ss = TypeCast.checkedStringSet(s);
        try {
            // it's NOT a Set<Integer>, but let's verify that
            // the exception is thrown.
            final Set<Integer> x = TypeCast.asSet(ss);
            x.add(new Integer(0));
            assert false;
        }
        catch (Exception e) {/*good, expected*/}

        final Set<String> sss = TypeCast.checkedStringSet(ss);
        // assert( sss == ss );  bummer, it's not smart enough to not wrap it twice


        final Set<Object> mixed = new HashSet<Object>();
        mixed.add("hello");
        mixed.add(new Integer(0));
        final Set<String> bogus = TypeCast.asSet(mixed);
        final Set<String> checkedBogus = Collections.checkedSet(bogus, String.class);
        // that worked.  Our variant should reject it:
        try {
            TypeCast.checkedStringSet(bogus);
        }
        catch (Exception e) {/*good, expected*/}
    }
}















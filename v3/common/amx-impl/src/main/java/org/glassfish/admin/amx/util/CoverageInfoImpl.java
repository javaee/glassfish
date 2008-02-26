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
package org.glassfish.admin.amx.util;

import java.io.Serializable;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.jmx.ReadWriteAttributeFilter;


/**
	Used to record access to AMX.
	@see AMXDebugStuff
 */
public final class CoverageInfoImpl implements Serializable, CoverageInfo
{
    public static final long serialVersionUID   = 0xABCDEF;
    
    private final Set<String>   mAttributesRead;
    private final Set<String>   mAttributesWritten;
    private final Set<String>   mOperationsInvoked;
    
    private final Map<String,Integer>   mAttributeGetFailures;
    private final Map<String,Integer>   mAttributeSetFailures;
    private final Map<String,Integer>   mUnknownAttributes;
    private final Map<String,Integer>   mUnknownOperations;
    private final Map<String,Integer>   mInvocationFailures;
    
    private Set<String>     mLegalReadableAttributes;
    private Set<String>     mLegalWriteableAttributes;
    private Set<String>     mLegalOperations;
    
    private MBeanInfo   mMBeanInfo;
    
        public
    CoverageInfoImpl( final MBeanInfo mbeanInfo )
    {
        mLegalReadableAttributes   = null;
        mLegalWriteableAttributes  = null;
        mLegalOperations           = null;
        mMBeanInfo  = mbeanInfo;
        setMBeanInfo( mMBeanInfo );
        
        mAttributesRead    = new HashSet<String>();
        mAttributesWritten = new HashSet<String>();
        mOperationsInvoked = new HashSet<String>();
        
        mAttributeGetFailures = new HashMap<String,Integer>();
        mAttributeSetFailures = new HashMap<String,Integer>();
        mUnknownAttributes    = new HashMap<String,Integer>();
        mUnknownOperations    = new HashMap<String,Integer>();
        mInvocationFailures   = new HashMap<String,Integer>();
        
    }
    
        public void
    clear()
    {
        mAttributesRead.clear();
        mAttributesWritten.clear();
        mOperationsInvoked.clear();
        
        mAttributeGetFailures.clear();
        mAttributeSetFailures.clear();
        mUnknownAttributes.clear();
        mUnknownOperations.clear();
        mInvocationFailures.clear();
    }
    
        public MBeanInfo
    getMBeanInfo()
    {
        return mMBeanInfo;
    }

        public void
    setMBeanInfo( final MBeanInfo mbeanInfo )
    {
        mLegalOperations            = new HashSet<String>();
        mLegalReadableAttributes    = new HashSet<String>();
        mLegalWriteableAttributes   = new HashSet<String>();
        
        if ( mbeanInfo != null ) try
        {
            final MBeanOperationInfo[]  ops = mbeanInfo.getOperations();
            for( final MBeanOperationInfo opInfo : ops  )
            {
                final String[]  sig = JMXUtil.getSignature( opInfo.getSignature() );
                final String    fullName    =
                    getFullOperationName( opInfo.getName(), sig );
                mLegalOperations.add( fullName );
            }
            mLegalOperations    = Collections.unmodifiableSet( mLegalOperations );
            
            final MBeanAttributeInfo[] allAttrInfos    = getMBeanInfo().getAttributes();
                
            final MBeanAttributeInfo[]  readables  =
                JMXUtil.filterAttributeInfos( allAttrInfos,
                    ReadWriteAttributeFilter.READABLE_FILTER );
                    
            final MBeanAttributeInfo[]  writeables  =
                JMXUtil.filterAttributeInfos( allAttrInfos,
                    ReadWriteAttributeFilter.WRITEABLE_FILTER );
            
            mLegalReadableAttributes    =
                GSetUtil.newUnmodifiableStringSet( JMXUtil.getAttributeNames( readables ) );
                
            mLegalWriteableAttributes    =
                GSetUtil.newUnmodifiableStringSet( JMXUtil.getAttributeNames( writeables ) );
        }
        catch( Exception e )
        {
            System.out.println( ExceptionUtil.toString( e ) );
            throw new RuntimeException( e );
        }
    }
    
    
        private void
    mergeCounts(
        final Map<String,Integer> src,
        final Map<String,Integer> dest )
    {
        for( final String key : src.keySet() )
        {
            final Integer srcValue = src.get( key );
            final Integer destValue = dest.get( key );
            
            final int sum   = srcValue.intValue() +
                    (destValue == null ? 0 : destValue.intValue());
            
            dest.put(key, Integer.valueOf(sum));
        }
    }
    
        public void
    merge( final CoverageInfo info )
    {
        mAttributesRead.addAll( info.getAttributesRead() );
        mAttributesWritten.addAll( info.getAttributesWritten() );
        mOperationsInvoked.addAll( info.getOperationsInvoked() );
        
        mergeCounts( info.getAttributeGetFailures(), mAttributeGetFailures );
        mergeCounts( info.getAttributeSetFailures(), mAttributeSetFailures );
        mergeCounts( info.getUnknownAttributes(), mUnknownAttributes );
        mergeCounts( info.getUnknownOperations(), mUnknownOperations );
        mergeCounts( info.getInvocationFailures(), mInvocationFailures );
    }
    
        public Set<String>
    getAttributesRead()
    {
        return new HashSet<String>( mAttributesRead );
    }
    
        public Set<String>
    getAttributesNotRead()
    {
        checkHaveMBeanInfo();
        
        // remove all Attributes which were written from the legal set
        final Set<String>   notRead = new HashSet<String>( mLegalReadableAttributes );
        notRead.removeAll( mAttributesRead );

        return notRead;
    }
    
        public Set<String>
    getAttributesWritten()
    {
        return  new HashSet<String>( mAttributesWritten );
    }
    
        public Set<String>
    getAttributesNotWritten()
    {
        checkHaveMBeanInfo();
        
        // remove all Attributes which were read from the legal set
        final Set<String>   notWritten = new HashSet<String>( mLegalWriteableAttributes );
        notWritten.removeAll( mAttributesWritten );

        return notWritten;
    }
    
        public Set<String>
    getOperationsInvoked()
    {
        return new HashSet<String>( mOperationsInvoked );
    }
    
        public Set<String>
    getOperationsNotInvoked()
    {
        checkHaveMBeanInfo();
        
        // remove all Attributes which were read from the legal set
        final Set<String>   notInvoked = new HashSet<String>( mLegalOperations );
        notInvoked.removeAll( getOperationsInvoked() );

        return notInvoked;
    }
    
        private void
    checkHaveMBeanInfo()
    {
        if ( getMBeanInfo() == null )
        {
            throw new IllegalArgumentException(
                "MBeanInfo must be set using setMBeanInfo() prior to call" );
        }
    }
    
        public Map<String,Integer>
    getAttributeGetFailures()
    {
        return new HashMap<String,Integer>( mAttributeGetFailures );
    }
    
        public Map<String,Integer>
    getAttributeSetFailures()
    {
        return new HashMap<String,Integer>( mAttributeSetFailures );
    }
    
        public Map<String,Integer>
    getUnknownAttributes()
    {
        return new HashMap<String,Integer>( mUnknownAttributes );
    }    

    
        public void
    ignoreUnknownAttribute( final String name )
    {
        mUnknownAttributes.remove( name );
        mAttributeGetFailures.remove( name );
        mAttributeSetFailures.remove( name );
    }
    
    
        public Map<String,Integer>
    getUnknownOperations()
    {
        return new HashMap<String,Integer>( mUnknownOperations );
    }    
    
        public Map<String,Integer>
    getInvocationFailures()
    {
        return new HashMap<String,Integer>( mInvocationFailures );
    }
    
        private void
    unknownAttribute( final String name )
    {
        Integer count   = mUnknownAttributes.get( name );
        count   = Integer.valueOf(count == null ? 1
                                      : 1 + count.intValue());
        
        mUnknownAttributes.put( name, count );
    }
    
        public void
    attributeWasRead( final String name )
    {
        if ( mLegalReadableAttributes.contains( name ) )
        {
            mAttributesRead.add( name );
        }
        else
        {
            unknownAttribute( name );
        }
    }
    
        public void
    attributesWereRead( final String[] names )
    {
        for( final String name : names )
        {
            attributeWasRead( name );
        }
    }
    
        public void
    attributeSetFailure( final String name )
    {
        Integer count   = mAttributeSetFailures.get( name );
        count   = Integer.valueOf(count == null ? 1
                                      : 1 + count.intValue());
        
        mAttributeSetFailures.put( name, count );
    }
    
    
        public void
    attributeGetFailure( final String name )
    {
        Integer count   = mAttributeGetFailures.get( name );
        count   = Integer.valueOf(count == null ? 1
                                      : 1 + count.intValue());
        
        mAttributeGetFailures.put( name, count );
    }
    
        public void
    attributeWasWritten( final String name )
    {
        if ( mLegalWriteableAttributes.contains( name ) )
        {
            mAttributesWritten.add( name );
        }
        else
        {
            unknownAttribute( name );
        }
    }
    
        private String
    getFullOperationName(
        final String    name,
        final String[]  sig)
    {
        final String    sigString   = StringUtil.toString( ",", sig == null ? EMPTY_SIG : sig  );
        
        final String    s   = name + "(" + sigString + ")";
        
        return s;
    }
    
    private static final String[]   EMPTY_SIG   = new String[0];
    
    private void sdebug( final Object o )
    {
        System.out.println( "" + o );
    }
        public void
    operationWasInvoked(
        final String    name,
        final String[]  sig)
    {
        final String fullName   = getFullOperationName( name, sig );

        if ( mLegalOperations.contains( fullName ) )
        {
            mOperationsInvoked.add( fullName );
        }
        else
        {
            unknownOperation( fullName, sig );
            assert( ! mOperationsInvoked.contains( fullName ) );
        }
    }
    
        private void
    unknownOperation(
        final String    name,
        final String[]  sig)
    {
        final String fullName   =
            getFullOperationName( name, sig );
        
        Integer count   = mUnknownOperations.get( fullName );
        count   = Integer.valueOf(count == null ? 1
                                      : 1 + count.intValue());
        
        mUnknownOperations.put( fullName, count );
    }
    
        public void
    markAsInvoked(final String fullName )
    {
        if ( (! fullName.endsWith( ")" )) || fullName.indexOf( "(" ) < 0 )
        {
            throw new IllegalArgumentException( fullName );
        }
        
        mOperationsInvoked.add( fullName );
    }
    
        public void
    operationFailed(
        final String    name,
        final String[]  sig)
    {
        final String    fullName    = getFullOperationName( name, sig );
        if ( ! getOperations().contains( fullName ) )
        {
            throw new IllegalArgumentException( fullName );
        }
        
        Integer count   = mInvocationFailures.get( name );
        count   = Integer.valueOf(count == null ? 1
                                      : 1 + count.intValue());
        
        mInvocationFailures.put( fullName, count );
    }
    
        
        public String
    toString()
    {
        return toString( true );
    }
    
        private String
    toString( final Collection c)
    {
        return CollectionUtil.toString( c );
    }
    
        private String
    toString( final Collection c, final String sep )
    {
        return CollectionUtil.toString( c, sep );
    }

        public int
    getNumReadableAttributes()
    {
        checkHaveMBeanInfo();
        
        return mLegalReadableAttributes.size();
    }
    
        public Set<String>
    getReadableAttributes()
    {
        return mLegalReadableAttributes;
    }
    
        public Set<String>
    getWriteableAttributes()
    {
        return mLegalWriteableAttributes;
    }
    
    
        public Set<String>
    getOperations()
    {
        return mLegalOperations;
    }
    
        public int
    getNumWriteableAttributes()
    {
        checkHaveMBeanInfo();
        
        return mLegalWriteableAttributes.size();
    }
    
        public int
    getNumOperations()
    {
        return mLegalOperations.size();
    }
    
        public int
    getAttributeReadCoverage()
    {
        return percent( mAttributesRead.size(), mLegalReadableAttributes.size() );
    }
    
        public int
    getAttributeWriteCoverage()
    {
        return percent( mAttributesWritten.size(), getNumWriteableAttributes() );
    }
    
    /**
        @return percent of legal operations which were invoked
     */
        public int
    getOperationCoverage()
    {
        checkHaveMBeanInfo();
        
        final int   numOperations   = mLegalOperations.size();
        
        // do it this way; some illegal operations get invoked, too
        final Set<String> remaining   = new HashSet<String>( mLegalOperations );
        for( final String invoked : mOperationsInvoked )
        {
            remaining.remove( invoked );
        }

        final int   numInvoked      = numOperations - remaining.size();
        
        return percent( numInvoked, numOperations );
    }
    
        public boolean
    getFullCoverage()
    {
        return getAttributeReadCoverage() == 100 &&
                getAttributeWriteCoverage() == 100 &&
                getOperationCoverage() == 100;
    }
    
        private int
    percent( final int numerator, final int denominator )
    {
        return (int)((((float)numerator / (float)denominator))*100.0);
    }
    
        public String
    toString( final boolean verbose )
    {
        final String    NEWLINE    = System.getProperty( "line.separator" );
        final String    INDENT  = "  ";
        final String    ITEM_SEP  = NEWLINE + INDENT;
        
        final StringBuilder b   = new StringBuilder();
        
        b.append( "Attribute read coverage " +
            getAttributesRead().size() + "/" + getNumReadableAttributes() +
            " = " + getAttributeReadCoverage() + "%" + NEWLINE );
            
        b.append( "Attribute write coverage " +
            getAttributesWritten().size() + "/" + getNumWriteableAttributes() +
            " = " + getAttributeWriteCoverage() + "%" + NEWLINE );
            
        b.append( "Operation invocation coverage " +
                    getOperationsInvoked().size() + "/" + getNumOperations() +
                    " = " + getOperationCoverage()  + "%" + NEWLINE );
        
        if ( verbose )
        {
            b.append( mAttributesRead.size() +
                " Attributes read: " + ITEM_SEP +
                    toString( getAttributesRead(), ITEM_SEP) + NEWLINE );
                
            b.append( mAttributesWritten.size() +
                " Attributes written: " + ITEM_SEP +
                    toString( getAttributesWritten(), ITEM_SEP) + NEWLINE );
                
            b.append( mOperationsInvoked.size() +
                " operations invoked: " + ITEM_SEP +
                    toString( getOperationsInvoked(), ITEM_SEP) + NEWLINE );
        }
        
        if ( getMBeanInfo() != null )
        {
            Set<String> not = null;
            
            not = getAttributesNotRead();
            if ( not.size() != 0 || verbose )
            {
                b.append( not.size() +
                    " Attributes NOT read:" + ITEM_SEP + toString( not, ITEM_SEP ) + NEWLINE );
            }
                
            not = getAttributesNotWritten();
            if ( not.size() != 0 || verbose )
            {
                b.append( not.size() +
                    " Attributes NOT written:" + ITEM_SEP + toString( not, ITEM_SEP) + NEWLINE );
            }
            
            not = getOperationsNotInvoked();
            if ( not.size() != 0 || verbose )
            {
                b.append( not.size() +
                    " operations NOT invoked:" + ITEM_SEP + 
                        CollectionUtil.toString( not, ITEM_SEP ) + NEWLINE );
            }
        }
        else
        {
            b.append( "WARNING: MBeanInfo not supplied, " +
                "can't emit Attributes/operations not read/written/invoked" + NEWLINE );
        }
        
        Map<String,Integer> failures    = null;
        
        failures    = getAttributeGetFailures();
        if ( failures.size() != 0 || verbose )
        {
            b.append( failures.keySet().size() +
                " getAttribute failures: " + ITEM_SEP +
                    toString( failures.keySet(), ITEM_SEP) + NEWLINE );
        }
            
        failures    = getAttributeSetFailures();
        if ( failures.size() != 0 || verbose )
        {
            b.append( failures.keySet().size() +
                " setAttribute failures: " + ITEM_SEP +
                    toString( failures.keySet(), ITEM_SEP) + NEWLINE );
        }
            
        failures    = getUnknownAttributes();
        if ( failures.size() != 0 || verbose )
        {
            b.append( failures.keySet().size() +
                " unknown Attributes: " + ITEM_SEP +
                    toString( failures.keySet(), ITEM_SEP) + NEWLINE );
        }
        
        failures    = getUnknownOperations();
        if ( failures.size() != 0 || verbose )
        {
            b.append( failures.keySet().size() +
                " unknown operations: " + ITEM_SEP +
                    toString( failures.keySet(), ITEM_SEP) + NEWLINE );
        }
            
        failures    = getInvocationFailures();
        if ( failures.size() != 0 || verbose )
        {
            b.append( failures.keySet().size() +
                " invoke() failures: " + ITEM_SEP +
                CollectionUtil.toString( failures.keySet(), ITEM_SEP) + NEWLINE );
        }
        
        return b.toString();
    }
}








































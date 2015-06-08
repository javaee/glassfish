/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.hk2.tests.locator.provider;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.Assert;

import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * @author jwells
 *
 */
public class Menagerie {
    @Inject
    private @Eagles IterableProvider<Character> allEagles;
    
    @Inject
    private IterableProvider<FootballCharacter> allNFLPlayers;
    
    @Inject
    private @Named(ProviderTest.QUEEQUEG) IterableProvider<BookCharacter> queequegProvider;
    
    @Inject
    private IterableProvider<BookCharacter> allBookCharacters;
    
    @Inject
    private IterableProvider<Character> allCharacters;
    
    public void validateAllEagles() {
        Assert.assertTrue(allEagles.getSize() == 1);
        Assert.assertEquals(ProviderTest.SHADY, allEagles.get().getName());
        Assert.assertEquals(ProviderTest.EAGLES, ((FootballCharacter) allEagles.getHandle().getService()).getTeam());
    }
    
    public void validateAllGiants() {
        Assert.assertTrue(allNFLPlayers.getSize() == 2);
        IterableProvider<FootballCharacter> giants = allNFLPlayers.qualifiedWith(new GiantsImpl());
        
        Assert.assertTrue(giants.getSize() == 1);
        Assert.assertEquals(ProviderTest.ELI, giants.get().getName());
        Assert.assertEquals(ProviderTest.GIANTS, giants.getHandle().getService().getTeam());
    }
    
    public void validateQueequeg() {
        boolean found = false;
        for (BookCharacter character : queequegProvider) {
            Assert.assertFalse(found);
            found = true;
            
            Assert.assertEquals(ProviderTest.QUEEQUEG, character.getName());
            Assert.assertEquals(ProviderTest.MOBY_DICK, character.getBook());
        }
        Assert.assertTrue(found);
        
        found = false;
        for (ServiceHandle<BookCharacter> character : queequegProvider.handleIterator()) {
            Assert.assertFalse(found);
            found = true;
            
            Assert.assertEquals(ProviderTest.QUEEQUEG, character.getService().getName());
            Assert.assertEquals(ProviderTest.MOBY_DICK, character.getService().getBook());
        }
    }
    
    public void validateBookCharacters() {
        Assert.assertEquals(2, allBookCharacters.getSize());
        
        Assert.assertEquals(2, allCharacters.ofType(BookCharacter.class).getSize());
    }
    
    public void validateAllCharacters() {
        Assert.assertEquals(4, allCharacters.getSize());
        
        boolean foundQueequeg = false;
        boolean foundIshmael = false;
        boolean foundShady = false;
        boolean foundEli = false;
        for (Character character : allCharacters) {
            if (ProviderTest.QUEEQUEG.equals(character.getName())) foundQueequeg = true;
            if (ProviderTest.ISHMAEL.equals(character.getName())) foundIshmael = true;
            if (ProviderTest.SHADY.equals(character.getName())) foundShady = true;
            if (ProviderTest.ELI.equals(character.getName())) foundEli = true;
        }
        
        Assert.assertTrue(foundQueequeg);
        Assert.assertTrue(foundIshmael);
        Assert.assertTrue(foundShady);
        Assert.assertTrue(foundEli);
        
        foundQueequeg = false;
        foundIshmael = false;
        foundShady = false;
        foundEli = false;
        for (ServiceHandle<Character> character : allCharacters.handleIterator()) {
            if (ProviderTest.QUEEQUEG.equals(character.getService().getName())) foundQueequeg = true;
            if (ProviderTest.ISHMAEL.equals(character.getService().getName())) foundIshmael = true;
            if (ProviderTest.SHADY.equals(character.getService().getName())) foundShady = true;
            if (ProviderTest.ELI.equals(character.getService().getName())) foundEli = true;
        }
        
        Assert.assertTrue(foundQueequeg);
        Assert.assertTrue(foundIshmael);
        Assert.assertTrue(foundShady);
        Assert.assertTrue(foundEli);
        
        Assert.assertEquals(ProviderTest.SHADY,
                allCharacters.named(ProviderTest.SHADY).get().getName());
        Assert.assertEquals(ProviderTest.ELI,
                allCharacters.named(ProviderTest.ELI).get().getName());
        Assert.assertEquals(ProviderTest.QUEEQUEG,
                allCharacters.named(ProviderTest.QUEEQUEG).get().getName());
        Assert.assertEquals(ProviderTest.ISHMAEL,
                allCharacters.named(ProviderTest.ISHMAEL).get().getName());
        
        Assert.assertEquals(ProviderTest.SHADY,
                allCharacters.named(ProviderTest.SHADY).getHandle().getService().getName());
        Assert.assertEquals(ProviderTest.ELI,
                allCharacters.named(ProviderTest.ELI).getHandle().getService().getName());
        Assert.assertEquals(ProviderTest.QUEEQUEG,
                allCharacters.named(ProviderTest.QUEEQUEG).getHandle().getService().getName());
        Assert.assertEquals(ProviderTest.ISHMAEL,
                allCharacters.named(ProviderTest.ISHMAEL).getHandle().getService().getName());
    }
}

package org.jboss.weld.examples.translator;

import javax.ejb.Local;

@Local 
public interface Translator 
{ 

   public String translate(String sentence);
   
}

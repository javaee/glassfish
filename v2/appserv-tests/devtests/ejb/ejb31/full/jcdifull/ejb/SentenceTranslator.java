package org.jboss.weld.examples.translator;

import javax.ejb.Stateless;

@Stateless
public class SentenceTranslator implements Translator 
{ 
   
   public String translate(String sentence) 
   { 
      return "Lorem ipsum dolor sit amet"; 
   }
   
}
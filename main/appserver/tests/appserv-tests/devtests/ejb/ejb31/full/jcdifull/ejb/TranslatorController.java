package org.jboss.weld.examples.translator;

public interface TranslatorController
{
   
   public String getText();
   
   public void setText(String text);
   
   public void translate();
   
   public String getTranslatedText();
   
   public void remove();
   
}

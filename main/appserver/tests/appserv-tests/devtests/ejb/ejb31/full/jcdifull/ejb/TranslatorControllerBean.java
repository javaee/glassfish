package org.jboss.weld.examples.translator;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Stateful
@RequestScoped
@Named("translator")
    public class TranslatorControllerBean implements TranslatorController , TranslatorController2 
{
   
   @Inject
   private TextTranslator translator;
   
   private String inputText;
   
   private String translatedText;
   
   public String getText()
   {
      return inputText;
   }
   
   public void setText(String text)
   {
      this.inputText = text;
   }
   
   public void translate()
   {
      translatedText = translator.translate(inputText);
   }
   
   public String getTranslatedText()
   {
      return translatedText;
   }

    public void foo() {}
   
   @Remove
   public void remove()
   {
      
   }
   
}

package test.extension;


import javax.inject.Inject;

public class Interceptors
{
   
   @Inject
   private PackagePrivateConstructorExtension interceptorExtension;
   
   private Interceptors()
   {
   }
   
   public boolean isInterceptorEnabled(Class<?> clazz)
   {
      return interceptorExtension.getEnabledInterceptors().contains(clazz);
   }

}

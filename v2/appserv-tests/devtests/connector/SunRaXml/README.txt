========================================
Connector 1.0 tests // sun-ra.xml tests
CONTACT: Kanwar.Oberoi@Sun.COM
========================================
                                                                                                                                
This suite contains of 2 tests:
- cci
- cci-embedded
                                                                                                                                
                                                                                                                                
** cci **
- cci tests deploy the rar and the application independently. The rar is bundled with sun-ra.xml from cci/descriptor/sun-ra.xml
- to execute the cci tests: 
        - cd <appserv-tests>/devtests/connector/SunRaXml/
        - execute `asant cci'
                                                                                                                                
** cci-embedded **
- cci-embedded tests bundle the rar inside the application and then deploy the resulting archive. The rar is bundled with sun-ra.xml from cci-embedded/descriptor/sun-ra.xml
- to execute the cci-embedded tests: 
        - cd <appserv-tests>/devtests/connector/SunRaXml/
        - execute `asant cci-embedded'
                                                                                                                                
** executing the complete test suite **
- cd <appserv-tests>/devtests/connector/SunRaXml/
- execute `asant all'
                                                                                                                                
** Known issues **
                                                                                                                                
1.
        Issue: Appclient throws exceptions if a test is re-run. 
  Description: After executing `asant cci' if user executes `asant cci' or `asant cci-embedded', the appclients in the tests throw exceptions. 
   Workaround: Restart domain between subsequent runs. 
       Reason: UNDER INVESTIGATION. 

2.                                                                                                                              
        Issue: While executing `asant all', the second test (cci-embedded) fails.
  Description: See the description for issue 1. 	
   Workaround: Execute `asant cci'; restart domain; execute `asant cci-embedded'
       Reason: UNDER INVESTIGATION. This issue will be fixed when issue 1 is resolved.



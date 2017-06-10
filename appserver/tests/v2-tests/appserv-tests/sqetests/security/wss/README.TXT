Description: WSS message security

Total Tests: 5

Test 0.
	        Request: Signing(auth-source="content")
 AppClient---------------------------------------------> JAXWS Servlet Webservice Endpoint 
          <--------------------------------------------
	        Response: Signing(auth-source="content")


Test 1.
	        Request: Signing(auth-source="content")
 AppClient---------------------------------------------> EJB Webservice Endpoint 
          <--------------------------------------------
	        Response: Signing(auth-source="content")


Test 2.
	        Request: Sign and Encryption(auth-source="content" auth-recipient="after-conent")
 AppClient---------------------------------------------> EJB Webservice Endpoint 
          <--------------------------------------------
	        Response: Sign and Encryption(auth-source="content" auth-recipient="after-content")


Test 3.

		Request: Signing(auth-source="content")
 AppClient---------------------------------------------> Servlet Webservice Endpoint 
          <--------------------------------------------
	        Response: Signing(auth-source="content")


Test 4.
	        Request: Sign and Encryption(auth-source="content" auth-recipient="after-conent")
 AppClient---------------------------------------------> Servlet Webservice Endpoint 
          <--------------------------------------------
	        Response: Sign and Encryption(auth-source="content" auth-recipient="after-content")


Runtime:
o  ant all


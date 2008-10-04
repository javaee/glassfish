package org.glassfish.embed;


public class Console
{  
	/** print a prompt on the console but don't print a newline
      @param prompt the prompt string to display
    */

	public static void printPrompt(String prompt)
	{  
		System.out.print(prompt + " ");
		System.out.flush();
	}

	/** read a string from the console. The string is 
		terminated by a newline
		@return the input string (without the newline)
	*/

	public static String readLine()
	{  
		int ch;
		String r = "";
		boolean done = false;
		
		while (!done)
		{  
			try
			{  
				ch = System.in.read();
				if (ch < 0 || (char)ch == '\n')
					done = true;
				
				else if ((char)ch != '\r') // weird--it used to do \r\n translation
					r = r + (char) ch;
			}
			catch(java.io.IOException e)
			{  
				done = true;
			}
		}
		
		return r;
	}

	/**	read a string from the console. The string is 
		terminated by a newline
		@param prompt the prompt string to display
		@return the input string (without the newline)
	*/

	public static char getKey(String prompt)
	{  
		printPrompt(prompt);
		int ch = '\n';
		
		try
		{  
			ch = System.in.read();
		}
		catch(java.io.IOException e)
		{  
		}
		return (char)ch;
		
	}	
	
	/**	read a string from the console. The string is 
		terminated by a newline
		@param prompt the prompt string to display
		@return the input string (without the newline)
	*/

	public static String readLine(String prompt)
	{  
		printPrompt(prompt);
		return readLine();
	}

	/**	read an integer from the console. The input is 
	terminated by a newline
	@param prompt the prompt string to display
	@return the input value as an int
	@exception NumberFormatException if bad input
	*/

	public static int readInt(String prompt)
	{  
		while(true)
		{  
			printPrompt(prompt);
			
			try
			{  
				return Integer.valueOf
				(readLine().trim()).intValue();
			} 
			catch(NumberFormatException e)
			{  
				System.out.println("Not an integer. Please try again!");
			}
		}
	}

	/** read a floating point number from the console. 
	The input is terminated by a newline
	@param prompt the prompt string to display
	@return the input value as a double
	@exception NumberFormatException if bad input
	*/

	public static double readDouble(String prompt)
	{  
		while(true)
		{  
			printPrompt(prompt);
			
			try
			{  
				return Double.parseDouble(readLine().trim());
			} 
			catch(NumberFormatException e)
			{  
				System.out.println("Not a floating point number. Please try again!");
			}
		}
	}
}


import javax.xml.datatype.*;
import java.util.*;

public class DateTime {

    
    public static void main(String[] args) {

	String xsdDateTime = args[0];

	System.out.println("Converting xsdDateTime " + xsdDateTime + " ...");

	try {
	    DatatypeFactory factory = DatatypeFactory.newInstance();

	    XMLGregorianCalendar xmlGreg = factory.newXMLGregorianCalendar(xsdDateTime);

	    GregorianCalendar greg = xmlGreg.toGregorianCalendar();

	    Date date = greg.getTime();

	    System.out.println("Date = " + date);

	    GregorianCalendar reverseCalendar = new GregorianCalendar();
	    reverseCalendar.setTime(date);


	    XMLGregorianCalendar reverseXmlGreg = 
		factory.newXMLGregorianCalendar(reverseCalendar);

	    String reverseXsdDateTime = reverseXmlGreg.toXMLFormat();

	    System.out.println("Back to xsdDateTime = " + reverseXsdDateTime);

	} catch(Exception e) {
	    e.printStackTrace();
	}


    }

}
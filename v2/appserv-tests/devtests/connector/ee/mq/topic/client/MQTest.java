
import javax.jms.*;
import javax.naming.*;
import java.sql.*;


public class MQTest {


    public static void main(String[] args) throws Exception {
         com.sun.messaging.TopicConnectionFactory myTConnFactory = 
                  new com.sun.messaging.TopicConnectionFactory();
         myTConnFactory.setProperty("imqBrokerHostName", "javasoft12"); 
         myTConnFactory.setProperty("imqBrokerHostPort", "7677");
         TopicConnection myTConn =
                  myTConnFactory.createTopicConnection();
         TopicSession myQSess = myTConn.createTopicSession(false,
                  Session.AUTO_ACKNOWLEDGE);
         Topic myTopic = new com.sun.messaging.Topic("Topic");
         TopicPublisher myTopicPublisher = myQSess.createPublisher(myTopic);
         for (int i=0; i < 5; i++) {
              TextMessage myTextMsg = myQSess.createTextMessage(); 
              myTextMsg.setText("Hello World :" + i); 
              System.out.println("Publishing Message: " + myTextMsg.getText()); 
              myTopicPublisher.publish(myTextMsg);
         }
         myQSess.close(); 
         myTConn.close();

    } // main
} // class


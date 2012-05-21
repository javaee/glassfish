package com.sun.s1asdev.ejb.ejb30.hello.session3;

import javax.annotation.PostConstruct;
import javax.annotation.mail.MailSessionDefinition;
import javax.annotation.mail.MailSessionDefinitions;
import javax.ejb.Stateful;
import javax.naming.InitialContext;

@MailSessionDefinitions(
        value = {
                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:global/mail/HelloStatefulEJB_MailSession",
                        storeProtocol = "IMAP",
                        storeProtocolClass = "com.example.mail.imap.IMAPStore",
                        transportProtocol = "SMTP",
                        transportProtocolClass = "com.sun.mail.smtp.SMTPTransport",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),

                @MailSessionDefinition(description = "Mail Session Description 3",
                        name = "java:app/mail/HelloStatefulEJB_MailSession",
                        storeProtocol = "IMAP",
                        storeProtocolClass = "com.example.mail.imap.IMAPStore",
                        transportProtocol = "SMTP",
                        transportProtocolClass = "com.sun.mail.smtp.SMTPTransport",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 4",
                        name = "java:module/mail/HelloStatefulEJB_MailSession",
                        storeProtocol = "IMAP",
                        storeProtocolClass = "com.example.mail.imap.IMAPStore",
                        transportProtocol = "SMTP",
                        transportProtocolClass = "com.sun.mail.smtp.SMTPTransport",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                )
        }
)
@Stateful
public class HelloStatefulEJB implements HelloStateful {


    @PostConstruct
    public void postConstruction() {
        System.out.println("In HelloStatefulEJB::postConstruction()");
    }

    public void hello() {
        boolean global = lookupMailSession("java:global/mail/HelloStatefulEJB_MailSession", true);
        boolean appHelloStatefulEjb = lookupMailSession("java:app/mail/HelloStatefulEJB_MailSession", true);

        boolean globalHelloEJB = lookupMailSession("java:global/mail/HelloEJB_MailSession", true);
        boolean moduleHelloEjb = lookupMailSession("java:module/mail/HelloEJB_MailSession", true);

        boolean globalServlet = lookupMailSession("java:global/env/Servlet_MailSession", true);
        boolean appServlet = lookupMailSession("java:app/env/Servlet_MailSession", true);
        boolean moduleServlet = lookupMailSession("java:module/mail/Servlet_MailSession", false);

        boolean globalServlet_MS_MailSession = lookupMailSession("java:global/mail/Servlet_MS_MailSession", true);

        boolean globalHelloStateful_MS_MailSession = lookupMailSession("java:global/mail/HelloStatefulEJB_MS_MailSession", true);

        boolean globalHello_MS_MailSession = lookupMailSession("java:global/mail/HelloEJB_MS_MailSession", true);

        boolean globalAppLevel_MS_MailSession = lookupMailSession("java:global/mail/Application_Level_MailSession", true);
        boolean appAppLevel_MS_MailSession = lookupMailSession("java:app/mail/Application_Level_MailSession", true);

        if (global && globalHelloEJB && globalServlet && appServlet &&
                globalServlet_MS_MailSession && globalHelloStateful_MS_MailSession
                && globalHello_MS_MailSession && appHelloStatefulEjb && moduleHelloEjb && !moduleServlet
                && globalAppLevel_MS_MailSession && appAppLevel_MS_MailSession) {
            System.out.println("StatefulEJB mail-session Success");

        } else {
            System.out.println("StatefulEJB mail-session Failure");
            throw new RuntimeException("StatefulEJB mail-session Failure");
        }
    }

    public void sleepFor(int sec) {
        try {
            for (int i = 0; i < sec; i++) {
                Thread.currentThread().sleep(1000);
            }
        } catch (Exception ex) {
        }
    }

    public void ping() {
    }

    private boolean lookupMailSession(String mailSessionName, boolean expectSuccess) {
        try {
            InitialContext ic = new InitialContext();
            Object ds = ic.lookup(mailSessionName);
            return true;
        } catch (Exception e) {
            if (expectSuccess) {
                e.printStackTrace();
            }
            return false;
        }
    }

}

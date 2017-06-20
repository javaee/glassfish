/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.mail.MailSessionDefinition;
import javax.mail.MailSessionDefinitions;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@MailSessionDefinitions(
        value = {
                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:global/mail/Appclient_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {
				"mail.imap.class=com.sun.mail.imap.IMAPStore",
				"mail.smtp.class=com.sun.mail.smtp.SMTPTransport"
			}
                ),
                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:comp/env/Appclient_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {
				"mail.imap.class=com.sun.mail.imap.IMAPStore",
				"mail.smtp.class=com.sun.mail.smtp.SMTPTransport"
			}

                ),
                @MailSessionDefinition(
                        name = "java:app/mail/Application_Level_MailSession_Partial",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        properties = {
				"mail.imap.class=com.sun.mail.imap.IMAPStore",
				"mail.smtp.class=com.sun.mail.smtp.SMTPTransport"
			}
                ),
                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:app/mail/Application_Level_MailSession_Override",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {
				"mail.imap.class=com.sun.mail.imap.IMAPStore",
				"mail.smtp.class=com.sun.mail.smtp.SMTPTransport"
			}
                )
        }
)

public class Client {

    private String host;
    private String port;

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");


    public Client(String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? args[1] : "4848";
    }

    public static void main(String[] args) {
        stat.addDescription("mail-sessionclient");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("mail-sessionID");
    }

    public void doTest() {

        String env = null;
        try {

            boolean moduleds = lookupMailSession("java:module/mail/moduleds", true);

            boolean appds = lookupMailSession("java:app/mail/appclient/appds", true);

            boolean globalds = lookupMailSession("java:global/mail/ts/mail-session/appclient/globalds", true);

            boolean comp = lookupMailSession("java:comp/env/Appclient_MailSession", true);

            boolean globalAppclient = lookupMailSession("java:global/mail/Appclient_MailSession", true);

            boolean globalServlet_MailSession = lookupMailSession("java:global/env/Servlet_MailSession", true);

            boolean globalHelloSfulEJB = lookupMailSession("java:global/mail/HelloStatefulEJB_MailSession", true);

            boolean appHelloStatefulEjb = lookupMailSession("java:app/mail/HelloStatefulEJB_MailSession", true);

            boolean globalHelloEJB = lookupMailSession("java:global/mail/HelloEJB_MailSession", true);

            boolean globalHelloStateful_MS_MailSession = lookupMailSession("java:global/mail/HelloStatefulEJB_MS_MailSession", true);

            boolean globalHello_MS_MailSession = lookupMailSession("java:global/mail/HelloEJB_MS_MailSession", true);

            boolean appLevelMailSessionMerge = lookupMailSession("java:app/mail/Application_Level_MailSession_Partial", true);

            boolean appLevelMailSessionOverride = lookupMailSession("java:app/mail/Application_Level_MailSession_Override", true);

            if (moduleds && appds && globalds && comp && globalAppclient && globalServlet_MailSession && globalHelloSfulEJB &&
                    globalHelloEJB && globalHelloStateful_MS_MailSession
                    && globalHello_MS_MailSession && appHelloStatefulEjb && appLevelMailSessionMerge && appLevelMailSessionOverride) {
                System.out.println("AppClient successful lookup of mail-session !");
                stat.addStatus("Mai-Session-appclient-test", stat.PASS);
            } else {
                System.out.println("AppClient lookup not successful");
                stat.addStatus("Mai-Session-appclient-test", stat.FAIL);
                throw new RuntimeException("Appclient failure during lookup of mail-session");
            }

            String url = "http://" + host + ":" + port +
                    "/mail-session/servlet";
            System.out.println("invoking Mai-Session test servlet at " + url);
            int code = invokeServlet(url);


            if (code != 200) {
                System.out.println("Incorrect return code: " + code);
                stat.addStatus("Mai-Session-web-ejb-test", stat.FAIL);
            } else {
                stat.addStatus("Mai-Session-web-ejb-test", stat.PASS);
            }
        } catch (Exception ex) {
            System.out.println("Mai-Session web & ejb test failed.");
            stat.addStatus("Mai-Session-web-ejb-test", stat.FAIL);
            ex.printStackTrace();
        }

        return;

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

    private int invokeServlet(String url) throws Exception {

        URL u = new URL(url);

        HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null)
            System.out.println(line);
        if (code != 200) {
            System.out.println("Incorrect return code: " + code);
        }
        return code;
    }

}


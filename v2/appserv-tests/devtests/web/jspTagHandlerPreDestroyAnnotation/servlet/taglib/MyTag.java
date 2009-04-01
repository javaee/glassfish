package taglib;

import java.io.*;
import javax.annotation.PreDestroy;
import javax.servlet.jsp.tagext.TagSupport;

public class MyTag extends TagSupport {

    @PreDestroy
    public void myPreDestroy() {
        try {
            FileOutputStream fos = new FileOutputStream("/tmp/mytest");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write("SUCCESS");
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

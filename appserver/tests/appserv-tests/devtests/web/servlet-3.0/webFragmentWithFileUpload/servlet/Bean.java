package fileupload;

import java.io.IOException;
import java.util.Scanner;
import javax.faces.bean.ManagedBean;
import javax.servlet.http.Part;

@ManagedBean
public class Bean {

    private Part file;
    private String fileContent;

    public void upload() {
        try {
            fileContent = new Scanner(file.getInputStream()).useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Upload completed" + fileContent);
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

}

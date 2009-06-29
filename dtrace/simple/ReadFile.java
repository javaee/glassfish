import java.util.*;
// test
// xxxxxxxxxxxxxxxxxxx




class ReadFile {
	native byte[] loadFile(String name);

	static {
		System.loadLibrary("nativelib");
	}

	public static void main(String args[]) {
		byte buf[];
		ReadFile mappedFile=new ReadFile();
		buf=mappedFile.loadFile("ReadFile.java");

		for(int i=0;i<buf.length;i++) {
			System.out.print((char)buf[i]);
		}
	}
}


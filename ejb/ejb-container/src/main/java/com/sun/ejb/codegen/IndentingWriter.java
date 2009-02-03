package com.sun.ejb.codegen;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/*
 * //FIXME: Temporary class
 */
public class IndentingWriter {

	private int indentLevel;

	private PrintWriter pw;

	public IndentingWriter(OutputStreamWriter osw) {
		this.pw = new PrintWriter(osw);
	}

	public void p(String s) {
		indent();
		pw.print(s);
	}
	
	public void plnI(String s) {
		indent();
		pw.print(s);
		indentLevel++;
	}

	public void pln(String s) {
		indent();
		pw.println(s);
	}

	public void pOln(String s) {
		indent();
		pw.println(s);
		indentLevel--;
	}
	
	public void close() {
		pw.flush();
		pw.close();
	}
	
	private void indent() {
		for (int i=0; i<indentLevel; i++) {
			pw.print("    ");
		}
	}
}

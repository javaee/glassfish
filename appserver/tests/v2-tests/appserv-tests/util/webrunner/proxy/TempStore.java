package com.sun.ejte.ccl.webrunner.proxy;

import java.io.*;


	/**
	* This class is for copying array of bytes. 
	* @author       Deepa Singh(deepa.singh@sun.com)
        *Company: Sun Microsystems Inc
	* 
	* 
	*/
public class TempStore
{
	String url;
	MimeHeader mh;
	byte data[];
	int length=0;

	/**
	* Constructor that Takes two Stringa as parameter 
	* @author       Deepa Singh(deepa.singh@sun.com)
	* 
	* @param		u	The string URL
	* @param		m	mimeheader
	*/
	public TempStore(String u,MimeHeader m)
	{
		url=u;
		mh=m;
		String cl=mh.get((String)"Content-Length");
		if(cl!=null)
		{
		data=new byte[Integer.parseInt(cl)];
		}
	}



	/**
	* 
	* @author       Deepa Singh(deepa.singh@sun.com)
	* 
	* @param		d[]	Byte array
	* @param		n Integer
	*/
	void append(byte d[],int n)
	{
		if(data==null)
		{
			data=new byte[n];
			System.arraycopy(d,0,data,0,n);
			length+=n;
		} else if(length+n>data.length)
		{
		byte old[]=data;
		data=new byte[old.length+n];
		System.arraycopy(old,0,data,0,old.length);
		System.arraycopy(d,0,data,old.length,n);
		length+=n;
		}else
		{
		System.arraycopy(d,0,data,length,n);
		length+=n;
		}
	}
}



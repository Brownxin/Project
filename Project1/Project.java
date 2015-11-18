package Project1;

import java.util.*;
import java.net.*;
import java.io.*;

import javax.net.ssl.*;
import javax.security.*;

/**
 * @author Canhui Zhao
 * @author Wei Zhang
 * @author Xin Wang
 */

public class Project {
	private int port=27993; //default port
	private String neuid, hostname;
	private boolean usingSSL=false;
	private String cert_path=System.getProperty("user.dir")+"\\jsseccacerts";
	
	private static void error(String errInfo){
		System.err.println(errInfo);
		System.exit(1);
	}
	
	private int calculateStatus(String recv){
		int num1=0,num2=0;
		StringTokenizer st=new StringTokenizer(recv);
		st.nextToken();
		st.nextToken();
		num1=Integer.valueOf(st.nextToken());
		String op=st.nextToken();
		num2=Integer.valueOf(st.nextToken());
		switch (op){
		case "+":
			return num1+num2;
		case "-":
			return num1-num2;
		case "*":
			return num1*num2;
		case "/":
			return (int)(num1/num2);
		}
		error("Wrong Formula.");
		return -1;
	}
	
	private boolean config(String[] args){
		int argv=args.length;
		if((argv<2) || (argv>5)){
			error("Illeagal Arguments.");
		}
		for(int i=0;i<argv;i++){
			if(args[0].equals("-s")){
				usingSSL=true;
				System.setProperty("javax.net.ssl.trustStore", cert_path);
				port=27994;	
			}
			if(args[i].equals("-p")){
				port=Integer.valueOf(args[i+1].toString());
			}
		}
		hostname=args[argv-2];
		neuid=args[argv-1];
		return true;
	}
	
	private void processTrans(PrintWriter out,BufferedReader in){
		String flag;
		try{
			out.println("cs5700fall2015 HELLO "+neuid+"\n");
			out.flush();
			while(true){
				String recvdata=in.readLine();
				String msgheads="cs5700fall2015 STATUS";
				String msgheade="cs5700fall2015 BYE";
				if(recvdata.startsWith(msgheads)){
					int res=calculateStatus(recvdata);
					out.println("cs5700fall2015 "+res+"\n");
					out.flush();
				}
				else if(recvdata.startsWith(msgheade)){
					int offset=msgheade.length();
					int length=64;
					flag=String.copyValueOf(recvdata.toCharArray(), offset, length);
					System.out.println(flag);
					break;
				}
				else{error("Received data has a wrong formate.");}
			}
		}catch (IOException e){
			error("IO Errors.");
		}
	}
	
	private boolean connect(){
		PrintWriter out;
		BufferedReader in;
		
		String flag;
		try{
			if(usingSSL){
				SSLSocketFactory fc=(SSLSocketFactory) SSLSocketFactory.getDefault();
				SSLSocket ss=(SSLSocket) fc.createSocket(hostname, port);
				ss.startHandshake();
				out=new PrintWriter(ss.getOutputStream(),true);
				in=new BufferedReader(new InputStreamReader(ss.getInputStream()));
				processTrans(out,in);
				ss.close();
			}
			else{
				Socket ss=new Socket(hostname,port);
				out=new PrintWriter(ss.getOutputStream(),true);
				in=new BufferedReader(new InputStreamReader(ss.getInputStream()));
				processTrans(out,in);
				ss.close();
			}
		}
		catch(UnknownHostException e){
			error("Unknown Host.");
		}
		catch(SSLException e){
			error(e.toString());
		}
		catch(IOException e){
			error("IO Errors.");
		}
		return true;
	}
	
	public static void main(String args[]){
		Project socket1=new Project();
		socket1.config(args);
		socket1.connect();
	}
}

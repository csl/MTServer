package com.MT;

import java.io.* ;
import java.net.* ;


public class MTServer {

public static final int MTPORT = 12341;
public static final int NUM_THREADS = 10;
public static Socket clientSocket;

public boolean rangeGPSSet;
public boolean hasNowStatus;
public String GPSRangeData;  
public String NowStatus;  


public MTServer(int port, int numThreads)
{
	ServerSocket servSock;
	rangeGPSSet = false;
	hasNowStatus = false; 
	GPSRangeData = "";
	NowStatus  = "";
	
	try {
		servSock = new ServerSocket(MTPORT);
	
	} catch(IOException e) {

		System.err.println("Could not create ServerSocket " + e);
		System.exit(1);
		return;
	}
	
	for (int i=0; i<NUM_THREADS; i++)
		new Thread(new Handler(servSock, i)).start();
}

	class Handler extends Thread 
	{
		ServerSocket servSock;
		int threadNumber;
		
		Handler(ServerSocket s, int i) {
			super();
			servSock = s;
			threadNumber = i;
			setName("Thread " + threadNumber);
		}
	
		@SuppressWarnings("deprecation")
		public void run()
		{
			while (true)
			{
				try {
					Socket clientSocket;
					synchronized(servSock) {
						clientSocket = servSock.accept();
					}
					System.out.println(getName() + " starting, IP=" +clientSocket.getInetAddress());
	
					//reponse
					DataInputStream in = new DataInputStream(clientSocket.getInputStream());
				
					String line;
					line = in.readUTF();
					System.out.println("my " + line);
					//os.print(line + "\r\n");
					if(line.equals("SetNowStatus"))
					{
						line = in.readUTF();
						NowStatus = line;
						hasNowStatus = true;
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						out.writeUTF("OK");
						out.flush();
					}
					else if(line.equals("nowStatus"))
					{
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						if (hasNowStatus == true)
						{
							out.writeUTF(NowStatus);
						}
						else
						{
							out.writeUTF("NoStatus");
						}
						out.flush();
					}
					else if(line.equals("SetGPSRange"))
					{
						//SetData
						line = in.readUTF();
						//lat,log,Isoverrange
						GPSRangeData = line;
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						out.writeUTF("OK");
						out.flush();
					}
					else if(line.equals("GetGPSRange"))
					{
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						if (hasNowStatus == true)
						{
							out.writeUTF(GPSRangeData);
						}
						else
						{
							out.writeUTF("NoRangeData");
						}

						out.flush();
					}
					System.out.println("data: " + line);
			       in.close();
					clientSocket.close();
				} catch (IOException ex) {
					System.out.println(getName() + ": IO Error on socket " + ex);
					return;
				}
			}	
		}
	}
	
	public static void main(String[] av)
	{
		new MTServer(MTPORT, NUM_THREADS);
	}
}


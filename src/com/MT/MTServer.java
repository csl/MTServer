package com.MT;

import java.io.* ;
import java.net.* ;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import db.jdbcmysql;


public class MTServer {

public static final int MTPORT = 12341;
public static final int NUM_THREADS = 100;
public static Socket clientSocket;

public boolean rangeGPSSet;
public boolean hasNowStatus;
public String GPSRangeData;  
public String NowStatus;  
public jdbcmysql db;

public List<GPSStruct> gpslist;

public MTServer(int port, int numThreads)
{
	ServerSocket servSock;
	rangeGPSSet = false;
	hasNowStatus = false; 
	GPSRangeData = "";
	NowStatus  = "";
	
	db = new jdbcmysql(this);
    db.createTable();
    
    gpslist = new ArrayList<GPSStruct>();
	
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
					//os.print(line + "\r\n");
					if(line.equals("SetNowStatus"))
					{
						line = in.readUTF();
						NowStatus = line;
						hasNowStatus = true;

						//String clock = in.readUTF();
						//db.SelectCTTable(clock);
						
						//System.out.println("req clock: " + clock);
						
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						out.writeUTF("OK");
						/*
						if (gpslist.size() != 0)
						{
							GPSStruct gps = gpslist.get(0);
							out.writeUTF("nowGPSRange");	
							out.writeUTF(gps.name);
							out.writeUTF(gps.gps);
							out.writeUTF(gps.stime);
							out.writeUTF(gps.dtime);
							
							System.out.println(gps.name);
							System.out.println(gps.gps);
							System.out.println(gps.stime);
							System.out.println(gps.dtime);
						}
						else
						{
							out.writeUTF("NoGPSRange");							
						}
						*/					
						out.flush();
					}
					else if(line.equals("nowStatus"))
					{
						//check clock
						String clock = in.readUTF();
						db.SelectCTTable(clock);

						System.out.println("req clock: " + clock);
						
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
						
						if (gpslist.size() != 0)
						{
							GPSStruct gps = gpslist.get(0);
							out.writeUTF("nowGPSRange");	
							out.writeUTF(gps.name);
							out.writeUTF(gps.gps);
							out.writeUTF(gps.stime);
							out.writeUTF(gps.dtime);
							
							System.out.println(gps.name);
							System.out.println(gps.gps);
							System.out.println(gps.stime);
							System.out.println(gps.dtime);
						}
						else
						{
							out.writeUTF("NoGPSRange");							
						}
						
						out.flush();
					}
					else if(line.equals("SetGPSRange"))
					{
						//SetData
						String name = in.readUTF();
						String stime = in.readUTF();
						String dtime = in.readUTF();
						String gps = in.readUTF();
						
						System.out.println(name);
						System.out.println(gps);
						System.out.println(stime);
						System.out.println(dtime);

						//implement check
						int sstime = 0, ddtime = 0;
					    StringTokenizer stoken = null;
				        stoken = new StringTokenizer( stime, ":" );
				        if ( stoken.hasMoreTokens() )
				        {
					            sstime = Integer.parseInt(stoken.nextToken());
					    }						
				        stoken = new StringTokenizer( dtime, ":" );
				        if ( stoken.hasMoreTokens() )
				        {
					            ddtime = Integer.parseInt(stoken.nextToken());
					    }			
				        
				        if (ddtime - sstime < 0)
		        		{
							//InsertData
							db.insertTable(name, gps, stime, "23:59");
							db.insertTable(name, gps, "00:00", dtime);				        	
		        		}
				        else
				        {
							//InsertData
							db.insertTable(name, gps, stime, dtime);
				        }
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						out.writeUTF("OK");
						out.flush();
					}
					else if(line.equals("GetGPSRange"))
					{
						//RepData
						String clock = in.readUTF();
						db.SelectCTTable(clock);
						
						System.out.println("req clock: " + clock);
						
						//Query
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						if (gpslist.size() != 0)
						{
							GPSStruct gps = gpslist.get(0);
							out.writeUTF("nowGPSRange");	
							out.writeUTF(gps.name);
							out.writeUTF(gps.gps);
							out.writeUTF(gps.stime);
							out.writeUTF(gps.dtime);
							
							System.out.println(gps.name);
							System.out.println(gps.gps);
							System.out.println(gps.stime);
							System.out.println(gps.dtime);
						}
						else
						{
							out.writeUTF("NoGPSRange");							
						}
						
						out.flush();
					}
					else if(line.equals("LGPS"))
					{
						//Query
						db.SelectTable();
						
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						out.writeUTF(Integer.toString(gpslist.size()));
						for (int i=0; i<gpslist.size(); i++)
						{
							GPSStruct gps = gpslist.get(i);
							out.writeUTF(gps.id);
							out.writeUTF(gps.name);
							out.writeUTF(gps.gps);
							out.writeUTF(gps.stime);
							out.writeUTF(gps.dtime);
						}
						out.flush();
					}
					else if(line.equals("UGPS"))
					{
						//SetData
						String id = in.readUTF();
						String name = in.readUTF();
						String stime = in.readUTF();
						String dtime = in.readUTF();
						String gps = in.readUTF();

						System.out.println(name);
						System.out.println(gps);
						System.out.println(stime);
						System.out.println(dtime);

						//implement check
						int sstime = 0, ddtime = 0;
					    StringTokenizer stoken = null;
				        stoken = new StringTokenizer( stime, ":" );
				        if ( stoken.hasMoreTokens() )
				        {
					            sstime = Integer.parseInt(stoken.nextToken());
					    }						
				        stoken = new StringTokenizer( dtime, ":" );
				        if ( stoken.hasMoreTokens() )
				        {
					            ddtime = Integer.parseInt(stoken.nextToken());
					    }			
				        
				        if (ddtime - sstime < 0)
		        		{
							//InsertData
							db.deleteListTable(name);
							db.insertTable(name, gps, stime, "23:59");
							db.insertTable(name, gps, "00:00", dtime);				        	
		        		}
				        else					
							//InsertData
							db.updateTable(id, name, gps, stime, dtime);
						
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						out.writeUTF("OK");
						out.flush();
					}
					else if(line.equals("DGPS"))
					{
						//SetData
						String name = in.readUTF();
						
						//InsertData
						db.deleteListTable(name);
						
						//RepData
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						out.writeUTF("OK");
						out.flush();
					}
					else if(line.equals("CGPST"))
					{
						//SetData
						String ctime = in.readUTF();

						//Query
						db.SelectCTTable(ctime);
						
						DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
						
						//GPS request for time
						GPSStruct gps = gpslist.get(0);
						out.writeUTF(gps.id);
						out.writeUTF(gps.name);
						out.writeUTF(gps.gps);
						out.writeUTF(gps.stime);
						out.writeUTF(gps.dtime);
						out.flush();
					}
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
	    //db.dropTable();
	    //db.insertTable("yku", "12356");
	    //db.insertTable("yku2", "7890");
	    //db.SelectTable();
		
		new MTServer(MTPORT, NUM_THREADS);
	}
}


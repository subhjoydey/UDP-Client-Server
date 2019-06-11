package udpSend.Receive;

import java.util.*;
import java.lang.*;


import java.io.*;
import java.net.*;
//this program uses Coordinates class and ValueReader class

//this client creates packets for BOTH files separately and take necessary steps

public class Client {

	private static final int SERVER_PORT=1300;
	private static final int ALLOWED_ATTEMPTS=4;
	//client port set as 1300

	public static void main(String[] args) throws IOException
	{

		DatagramSocket socket=new DatagramSocket();	//Creating Socket for the Transmitter end
		DatagramPacket DATApacket, DACKpacket;

		BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Subhojoy\\eclipse-workspace\\New folder\\data01.txt"));			//object for class ValueReader to extract values
		ValueReader Values= new ValueReader(reader);
		Values.arrCoordinates.clear();
		//initializing the Valuereader class to read the two files separately

		byte lastcorrect;		//stores last correct sequence
		byte[] previousPacket=new byte[405];		//stores previous packet to resend

		InetAddress localHostAddress= InetAddress.getLocalHost();	//localhostIP

		int x;		//x coordinate int value
		int y;		//y coordinate int value

		byte sequenceCount=0;		//sequence number count
		byte b3,b2,b1,b0;	//byte values of 32 bit integer
		int VectorTotal=0;				//the vector number of the vector list

		for(int i=0;i<2;i++)
		{
			if(i==0)
			{
				//initializing the values for the first file
				sequenceCount=0;
				VectorTotal=0;
				reader = new BufferedReader(new FileReader("C:\\Users\\Subhojoy\\eclipse-workspace\\New folder\\data01.txt"));
				Values= new ValueReader(reader);
				//reding the first file
				System.out.println("SENDING FIRST FILE");
			}
			else if(i==1)
			{
				//resetting the values for the second file
				sequenceCount=0;
				VectorTotal=0;
				reader = new BufferedReader(new FileReader("C:\\Users\\Subhojoy\\eclipse-workspace\\New folder\\data02.txt"));
				System.out.println("SENDING SCOND FILE");
				Values= new ValueReader(reader);
				//reading the second file
			}


			while(VectorTotal!=Values.arrCoordinates.size())
			{
				socket.setSoTimeout(3000);

				byte vectorCount=0;						//number of vectors count
				byte[] byteEncoded= new byte[405];		//creating the Datapacket bytearray
				DATApacket= new DatagramPacket(byteEncoded, byteEncoded.length,localHostAddress,SERVER_PORT);
				//creating data packet header

				int j=5;	//initializing the byte array pointer to the start of vector coordinates

				int timesCount=0;			//count number of time the packet is sent without being received
				int timeoutCount=1;			//count the timeout skips

				System.out.println("sending packet");

				byteEncoded[0]=0;
				//packet type 0 for data

				byteEncoded[1]=(byte)i;
				//the sequence count first byte to distinguish between first file and second file
				byteEncoded[3]=0;
				//total number of vector first byte is set as 0
				byteEncoded[2]=sequenceCount;
				//the sequence number is the second byte of sequence number

				while(j<405 && VectorTotal<Values.arrCoordinates.size())
				{
					//running loop to fill the data byte array
					x=(int)((Values.arrCoordinates.get(VectorTotal).x)*100);
					y=(int)((Values.arrCoordinates.get(VectorTotal).y)*100);
					//generating the int values of the coordinates from valuereader class fields

					b3 = (byte)((x>>24));
					byteEncoded[j]=b3;
					j++;
					b2 = (byte)((x>>16)&255);
					byteEncoded[j]=b2;
					j++;
					b1 = (byte)((x>>8)&255);
					byteEncoded[j]=b1;
					j++;
					b0 = (byte)((x)&255);
					byteEncoded[j]=b0;
					j++;
					//individual bytes for x coordinates created

					b3 = (byte)((y>>24));
					byteEncoded[j]=b3;
					j++;
					b2 = (byte)((y>>16)&255);
					byteEncoded[j]=b2;
					j++;
					b1 = (byte)((y>>8)&255);
					byteEncoded[j]=b1;
					j++;
					b0 = (byte)((y)&255);
					byteEncoded[j]=b0;
					j++;
					//individual bytes for y coordinates created

					byteEncoded[4]=vectorCount;
					//setting the total number of vectors sent

					vectorCount++;
					VectorTotal++;
					//incrementing vector count for each packet and the total vectors in the file
				}

				timesCount=0;
				timeoutCount=1;		//resetting the timescount and timeout count values for each packet

				try
				{																// Try block begins

					while(timesCount<ALLOWED_ATTEMPTS)
					{
						socket.setSoTimeout(timeoutCount*1000);
						timeoutCount=timeoutCount*2;

						DATApacket= new DatagramPacket(byteEncoded, byteEncoded.length,localHostAddress,SERVER_PORT);
						socket.send(DATApacket);
						//sending the data packet

						previousPacket=byteEncoded;
						//copying byte array in case to resend

						byte[] DACKbyte= new byte[3];
						DACKpacket= new DatagramPacket(DACKbyte,DACKbyte.length);

						try {
							socket.receive(DACKpacket);
						}
						catch (SocketTimeoutException ste) {
							System.out.println("Socket timed out!");
							socket.close();			//Closes when the DACK sequence number is wrong
							System.exit(0);			//exit
						}
						//creating acknowledgement receive packet

						if(DACKbyte[0]==1 && DACKbyte[2]==byteEncoded[2])
						{
							//checking the authenticity for data type and sequence number
							System.out.println("The packet has been approved: ");
							sequenceCount++;
							lastcorrect= byteEncoded[2];
							break;
							//if correct packet check then repeat loop
						}
						else if(DACKbyte[0]==1 && DACKbyte[2]!=byteEncoded[2])
						{
							System.out.println("Wrong sequence resend packet");
							DATApacket= new DatagramPacket(previousPacket, previousPacket.length,localHostAddress,SERVER_PORT);
							timesCount++;
							socket.send(DATApacket);
							continue;
							//if incorrect asked to resend the previous packet and attempts increases
						}
						else if(DACKbyte[0]!=1)
						{
							System.out.println("Packet is not approved wrong type");
							timesCount++;
							continue;
							//incorrect data type
						}
						else 
						{
							timesCount++;
							continue;

						}
					}//data send loop

					if(timesCount==ALLOWED_ATTEMPTS)
						throw new Exception("");

				}//try block
				catch (Exception e)
				{
					e.printStackTrace();

					if(timesCount==ALLOWED_ATTEMPTS ){ 

						System.out.println("Client's Socket Timeout");
						System.out.println("\nError: Communication Failure");
						System.out.println("Socket Close");
						socket.close();			//Closes when the DACK sequence number is wrong
						System.exit(0);			//exit
					}
				}

			}//data packet loop

			int timesCount=0;
			int timeoutCount=1;
			//resetting timeout and timescount

			try {
				while(timesCount<ALLOWED_ATTEMPTS)
				{
					//setting the REQ packet byte array
					byte[] REQbyte= new byte[1];
					REQbyte[0]=2;

					DatagramPacket REQpacket= new DatagramPacket(REQbyte, REQbyte.length,localHostAddress,SERVER_PORT);
					socket.send(REQpacket);
					//sending the REQpacket 

					socket.setSoTimeout(timeoutCount*1000);
					timeoutCount=timeoutCount*2;

					byte[] RACKbyte= new byte[1];
					DatagramPacket RACKpacket= new DatagramPacket(RACKbyte, RACKbyte.length);
					try {
						socket.receive(RACKpacket);
					}
					catch (SocketTimeoutException ste) {
						System.out.println("Socket timed out!");
						socket.close();			//Closes when the DACK sequence number is wrong
						System.exit(0);			//exit
					}
					//receiving the RACKpacket

					if(RACKbyte[0]==3)
					{
						System.out.println("ALL PACKETS FOR FILE "+ (i+1)+" HAS BEEN RECEIVED AND ACKNOWLEDGED, START COMPUTATION");
						break;
						//if RACK packet received correctly then end send loop
					}
					else
					{
						System.out.println("NEED TO RESEND REQ");
						timesCount++;
						//else increase the attempt variable
					}
				}//req packet block

				if(timesCount==ALLOWED_ATTEMPTS)
					throw new Exception("");

			}//try block
			catch(Exception e)
			{
				if(timesCount==ALLOWED_ATTEMPTS)
				{
					System.out.println("Client's Socket Timeout");
					System.out.println("\nError: Communication Failure");
					System.out.println("Socket Close");
					System.exit(0);			//exit
				}

			}

		}//file loop
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		List<List<Coordinates>> ClusList= new ArrayList<List<Coordinates>>();
		List<Coordinates> Clus1= new ArrayList<Coordinates>();
		List<Coordinates> Clus2= new ArrayList<Coordinates>();
		ClusList.add(Clus1);
		ClusList.add(Clus2);
		//creating list of two lists of coordinates class objects to store centroids for two files

		int vectorX,vectorY;	//temporary values to store int values of coordinates
		int track=0;		//tracks the files number

		DatagramSocket Random= new DatagramSocket(2400);
		//creating new socket to receive

		InetAddress clientAddress= null;
		int portAddress = SERVER_PORT;
		//initializing port and address

		try {
			while(true)
			{
				int bytePosition=1;		//initializing the byte position from which value is stored

				byte[] CLUSbyte=new byte[17];
				byte[] CACKbyte= new byte[1];
				//initializing the CLUS and CACK byte arrays

				DatagramPacket CLUSpacket= new DatagramPacket(CLUSbyte,CLUSbyte.length);
				socket.setSoTimeout(30000);
				try {
					Random.receive(CLUSpacket);
				}
				catch (SocketTimeoutException ste) {
					System.out.println("Socket timed out!");
					socket.close();			//Closes when the DACK sequence number is wrong
					System.exit(0);			//exit
				}
				//receiving the bytearray for CLUSpacket

				clientAddress = CLUSpacket.getAddress();
				portAddress = CLUSpacket.getPort();
				//retrieving port and address info

				System.out.println("Received "+(track+1)+" CLUS packet");				

				if(CLUSbyte[0]==4)
				{
					while(bytePosition<17)
					{
						vectorX = CLUSbyte[bytePosition] << 24 | (CLUSbyte[bytePosition + 1] & 0xff) << 16
								| (CLUSbyte[bytePosition + 2] & 0xff) << 8 | (CLUSbyte[bytePosition + 3] & 0xff);
						vectorY = CLUSbyte[bytePosition + 4] << 24 | (CLUSbyte[bytePosition + 5] & 0xff) << 16
								| (CLUSbyte[bytePosition + 6] & 0xff) << 8 | (CLUSbyte[bytePosition + 7] & 0xff);

						//converting the 4 bytes to integer values for x and y coordinates of centroids

						bytePosition=bytePosition+8;

						Coordinates temp= new Coordinates();
						temp.x= (float)vectorX/100;
						temp.y= (float)vectorY/100;
						//storing values in coodinates clss objects

						if(track==0)
							Clus1.add(temp);
						if(track==1)
							Clus2.add(temp);
						//adding coordinates to list of Centroid list

					}

					CACKbyte[0]=5;
					DatagramPacket CACKpacket= new DatagramPacket(CACKbyte,CACKbyte.length,clientAddress,portAddress);
					Random.send(CACKpacket);
					//sending acknowledge for receiving the CLUS packet

					if(track==1)
						break;
					else
						track++;
					//keeping track of file number and to stop infinite loop

				}

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			socket.close();
			System.exit(0);

		}//CACK loop

		for(int m=0;m<2;m++)
		{
			System.out.println("Centroids for the File "+ (m+1)+" are "+ "\n"+ ClusList.get(m).get(0).x+" and "+ ClusList.get(m).get(0).y+"\n"+ClusList.get(m).get(1).x+" and "+ClusList.get(m).get(1).y);

		}


	}//main loop

}//class loop

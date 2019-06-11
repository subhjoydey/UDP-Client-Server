import java.io.IOException;
import java.util.Random;
import java.net.*;
import java.util.*;
//this program uses Coordinates class and ValueReader class

//this server receives BOTH files separately and utilizes them to get separate results

public class Server {

	private static final int SERVER_PORT = 1300;
	private static final int ALLOWED_ATTEMPTS = 4;
	// server port set as 1300

	public static List<Coordinates> arrCoordinates = new ArrayList<Coordinates>();
	public static List<List<Coordinates>> listOfArrCoordinates = new ArrayList<List<Coordinates>>();
	// initiating data structure array list of coordinates objects

	public static void main(String[] args) throws IOException {

		DatagramSocket listener = new DatagramSocket(SERVER_PORT); // Listens for incoming connections.
		DatagramPacket ACKpacket, DATApacket, RACKpacket; // Creating Datagram object

		InetAddress clientAddress = null;
		int portAddress = SERVER_PORT;
		// initializing the socket and the IP address

		System.out.println("Listening on port: " + SERVER_PORT);

		int vectorTotal = 243;
		// variable to store the incoming file size

		int vectorCount = 0; // counts the number of vectors in the packet
		int vectorX, vectorY; // x and y value of a coordinate as integer

		int g = 0; // counter variable to receive the two files separately

		byte lastSequence = 0; // the count for the last sequence number of packet
		byte lastCorrect = 0; // to store sequence number of the last correct data received

		System.out.println("Receiving the first file");

		try {
			while (true) { // to listen continuously till result is achieved

				int bytePosition = 5;
				// to check the data from the 5th position of the DATA packet

				byte[] receiveDATA = new byte[405];
				// the receive data size

				DATApacket = new DatagramPacket(receiveDATA, receiveDATA.length);
				listener.setSoTimeout(30000);

				try {
					listener.receive(DATApacket);
					// initiating packet to accept incoming data packets
				} catch (SocketTimeoutException ste) {
					System.out.println("Socket timed out!");
					listener.close(); // Closes when the DACK sequence number is wrong
					System.exit(0); // exit
				}

				if (receiveDATA[1] == 0 && receiveDATA[2] == 0) {
					vectorTotal = 243;
					/*
					 * setting the total number of vectors if the received packet sequence number of
					 * two bytes has 0 as first byte and 0 as second byte indicating first file
					 */

				} else if (receiveDATA[1] == 1 && receiveDATA[2] == 0) {
					System.out.println("Receiving the second file");
					vectorTotal = 114;
					/*
					 * setting the total number of vectors if the received packet sequence number of
					 * two bytes has 1 as first byte and 0 as second byte indicating second file
					 */

					// when the fist packet of 2nd file is receied all parameters reset
					lastSequence = 0;
					vectorCount = 0;
					lastCorrect = 0;
					g++;
					// incrementing g to indicate second file
				}

				clientAddress = DATApacket.getAddress(); // Obtaining client's IP address
				portAddress = DATApacket.getPort(); // obtaining the client's port number

				byte[] ACKbyte = new byte[3];

				if (receiveDATA[0] == 0 && receiveDATA[2] == lastSequence) {

					ACKbyte[0] = 1; // data type is 1
					ACKbyte[1] = receiveDATA[1]; // sequence number copied
					ACKbyte[2] = receiveDATA[2];
					lastCorrect = lastSequence; // the last sequence is equal to last correct packet

					while (bytePosition < receiveDATA.length && vectorCount < vectorTotal)
					// loop to fill the data packet avoiding 0 values for excess packet bye array
					{

						vectorX = receiveDATA[bytePosition] << 24 | (receiveDATA[bytePosition + 1] & 0xff) << 16
								| (receiveDATA[bytePosition + 2] & 0xff) << 8 | (receiveDATA[bytePosition + 3] & 0xff);
						vectorY = receiveDATA[bytePosition + 4] << 24 | (receiveDATA[bytePosition + 5] & 0xff) << 16
								| (receiveDATA[bytePosition + 6] & 0xff) << 8 | (receiveDATA[bytePosition + 7] & 0xff);
						// convert byte values to int

						Coordinates coordinates = new Coordinates();
						coordinates.setX((float) vectorX / 100);
						coordinates.setY((float) vectorY / 100);
						// putting x and y values into coordinate class object

						arrCoordinates.add(coordinates);
						// adding coordinates to array list

						bytePosition = bytePosition + 8;
						vectorCount++;
						// incrementing the array position and the number of vectors

					}

					lastSequence++; // incrementing the sequence byte

					ACKpacket = new DatagramPacket(ACKbyte, ACKbyte.length, clientAddress, portAddress);
					// defining the acknowledgement packet
					listener.send(ACKpacket);
					// sending ACK with proper processing
				}
				// defining the course of action for sequence correct and packet type correct

				else if (receiveDATA[0] == 0 && receiveDATA[2] != lastSequence) {
					// defining the course of action for when sequence number do not match
					ACKbyte[0] = 1;
					ACKbyte[1] = receiveDATA[1];
					ACKbyte[2] = lastCorrect;
					// sends sequence number of last correctly received packet
					System.out.println("Resending");
					ACKpacket = new DatagramPacket(ACKbyte, ACKbyte.length, clientAddress, portAddress);
					// defining the acknowledgement packet
					listener.send(ACKpacket);
					continue;

					// sending ACK with proper processing
				}
				// defining for packet type correct but wrong sequence

				else if (receiveDATA[0] == 2)
				// condition for sending rack and detecting Req
				{

					System.out.println("REQ packet received: End of file: " + (g + 1));
					listener.setSoTimeout(30000);
					byte[] RACKbyte = new byte[1];
					RACKpacket = new DatagramPacket(RACKbyte, RACKbyte.length, clientAddress, portAddress);
					// creating rack packet
					RACKbyte[0] = 3;

					listener.send(RACKpacket); // sending the RACK packet

					listOfArrCoordinates.add(arrCoordinates);
					// upon completion of a file the list of coordinates is stored in another separate list

					arrCoordinates = new ArrayList<Coordinates>();
					// upon finishing a file the list of coordinates is re-initiated

					if (g == 1)
						break;
					// server stops to receive after receiving the complete second file

				} else
					System.out.println("Wrong packet information header");

			} // receive packet end

		} // try block end
		catch (Exception e) {
			listener.close();
		}

		float[] rangeMaximumx = new float[2]; // initializing range values for x coordinated and y coordinates
		float[] rangeMaximumy = new float[2];
		float[] rangeMinimumx = new float[2];
		float[] rangeMinimumy = new float[2];

		for (int i = 0; i < listOfArrCoordinates.size(); i++) {

			List<Coordinates> arrCoordinates02 = listOfArrCoordinates.get(i);
			// printing the values of the list of coordinates

			int count = 0;
			System.out.println("The received data elements for " + (i + 1) + "th file is: ");

			while (count < arrCoordinates02.size()) {
				System.out.println(arrCoordinates02.get(count).x + " and " + arrCoordinates02.get(count).y);

				if (rangeMaximumx[i] < arrCoordinates02.get(count).x)
					rangeMaximumx[i] = arrCoordinates02.get(count).x;
				if (rangeMaximumy[i] < arrCoordinates02.get(count).y)
					rangeMaximumy[i] = arrCoordinates02.get(count).y;
				if (rangeMinimumx[i] > arrCoordinates02.get(count).x)
					rangeMinimumx[i] = arrCoordinates02.get(count).x;
				if (rangeMinimumy[i] > arrCoordinates02.get(count).y)
					rangeMinimumy[i] = arrCoordinates02.get(count).y;
				// setting values for the maximum and minimum values of x and y coordinate
				// ranges
				count++;

			}
		}

		List<List<Coordinates>> newCentroids02 = new ArrayList<List<Coordinates>>(); // initializing final list of 2
																						// centroids for both
		List<Coordinates> newCentroids021 = new ArrayList<Coordinates>();
		List<Coordinates> newCentroids022 = new ArrayList<Coordinates>();
		newCentroids02.add(newCentroids021);
		newCentroids02.add(newCentroids022);
		// centroids for both files

		List<List<Coordinates>> Cluster01 = new ArrayList<List<Coordinates>>(); // initializing cluster m1
		List<Coordinates> Cluster011 = new ArrayList<Coordinates>();
		List<Coordinates> Cluster012 = new ArrayList<Coordinates>();
		Cluster01.add(Cluster011);
		Cluster01.add(Cluster012);
		// cluster1 list for both files

		List<List<Coordinates>> Cluster02 = new ArrayList<List<Coordinates>>(); // initializing cluster m2
		List<Coordinates> Cluster021 = new ArrayList<Coordinates>();
		List<Coordinates> Cluster022 = new ArrayList<Coordinates>();
		Cluster02.add(Cluster021);
		Cluster02.add(Cluster022);
		// cluster2 list for both files

		float e = (float) Math.pow(10, -5);
		// setting the error constraint

		for (int i = 0; i < 2; i++) {

			float Centroid01x = (float) Math.random() * (rangeMaximumx[i] - rangeMinimumx[i]) + rangeMinimumx[i];
			float Centroid01y = (float) Math.random() * (rangeMaximumy[i] - rangeMinimumy[i]) + rangeMinimumy[i];
			float Centroid02x = (float) Math.random() * (rangeMaximumx[i] - rangeMinimumx[i]) + rangeMinimumx[i];
			float Centroid02y = (float) Math.random() * (rangeMaximumy[i] - rangeMinimumy[i]) + rangeMinimumy[i];
			// randomizing 2 centroids for K means Clustering

			Coordinates Centroid1 = new Coordinates(); // centroid1
			Centroid1.setX(Centroid01x);
			Centroid1.setY(Centroid01y);
			Coordinates Centroid2 = new Coordinates(); // centroid2
			Centroid2.setX(Centroid02x);
			Centroid2.setY(Centroid02y);

			newCentroids02.get(i).add(Centroid1);
			newCentroids02.get(i).add(Centroid2);
			// adding to the list of centroids

			List<Coordinates> arrCoordinates02 = listOfArrCoordinates.get(i);
			// list of file coordinates

			int count = 0;
			// counting the number of files

			int p = 0;
			int h = 0;
			// to prevent searching for the size of list when it is empty

			float distanceCentroid01 = 10;
			float distanceCentroid02 = 10;
			// initializing the Eucledian distance

			while ((distanceCentroid01 + distanceCentroid02) > e) {
				// condition for error

				float temp1 = arrCoordinates02.get(count).x - newCentroids02.get(i).get(0).x;
				float temp2 = arrCoordinates02.get(count).y - newCentroids02.get(i).get(0).y;
				float temp3 = arrCoordinates02.get(count).x - newCentroids02.get(i).get(1).x;
				float temp4 = arrCoordinates02.get(count).y - newCentroids02.get(i).get(1).y;
				distanceCentroid01 = (float) (Math.sqrt(Math.pow(temp1, 2) + Math.pow(temp2, 2))); // distance vector1
				distanceCentroid02 = (float) (Math.sqrt(Math.pow(temp3, 2) + Math.pow(temp4, 2))); // distance vector2
				// calculating new distance vectors from coordinates list

				if (distanceCentroid01 < distanceCentroid02) {
					Cluster01.get(i).add(arrCoordinates02.get(count)); // saving to cluster1
					p++;
				} else if (distanceCentroid01 > distanceCentroid02) {
					Cluster02.get(i).add(arrCoordinates02.get(count)); // else saving to cluster2
					h++;
				}

				List<Coordinates> Centroids02 = deepCopyArray(newCentroids02.get(i)); // list of 2 centroids temporary
				// copying the new centroids to old centroid

				float avgx = 0, avgy = 0;

				if (p > 0) {
					for (int j = 0; j < Cluster01.get(i).size(); j++) {
						avgx = avgx + Cluster01.get(i).get(j).x;
						avgy = avgy + Cluster01.get(i).get(j).y;
						// calculating avg of the cluster 1 for the new centroid 1
					}
					Centroid1.x = avgx / Cluster01.get(i).size();
					Centroid1.y = avgy / Cluster01.get(i).size();
				}
				avgx = 0;
				avgy = 0;
				if (h > 0) {
					for (int j = 0; j < Cluster02.get(i).size(); j++) {
						// Centroid1.x=
						avgx = avgx + Cluster02.get(i).get(j).x;
						avgy = avgy + Cluster02.get(i).get(j).y;
						// calculating avg of the cluster 1 for the new centroid 2
					}
					Centroid2.x = avgx / Cluster02.get(i).size();
					Centroid2.y = avgy / Cluster02.get(i).size();
					// saving to temporary centroid
				}

				if (i == 0) {
					newCentroids021 = new ArrayList<Coordinates>();
					newCentroids021.add(Centroid1);
					newCentroids021.add(Centroid2);
					newCentroids02.add(newCentroids021);
					// if first file storing new centroid list for the first one
				}
				if (i == 1) {
					newCentroids022 = new ArrayList<Coordinates>();
					newCentroids022.add(Centroid1);
					newCentroids022.add(Centroid2);
					newCentroids02.add(newCentroids022);
					// if second file storing new centroid list for the second one
				}

				temp1 = newCentroids02.get(i).get(0).x - Centroids02.get(0).x;
				temp2 = newCentroids02.get(i).get(0).y - Centroids02.get(0).y;
				temp3 = newCentroids02.get(i).get(1).x - Centroids02.get(1).x;
				temp4 = newCentroids02.get(i).get(1).y - Centroids02.get(1).y;

				distanceCentroid01 = (float) (Math.sqrt(Math.pow(temp1, 2) + Math.pow(temp2, 2))); // distance vector1
				distanceCentroid02 = (float) (Math.sqrt(Math.pow(temp3, 2) + Math.pow(temp4, 2))); // distance vector2
				// calculating new eucledean distance

				count++;
				// incrementing the count which points to the Vector number from file
				if (count == arrCoordinates02.size())
					count = 0;
				// repeating file till error margin is e

			} // centroid calc

			System.out
					.println("The E-value for " + (i + 1) + "th file is " + (distanceCentroid01 + distanceCentroid02));

		} // two different files

		for (int i = 0; i < 2; i++) {

			System.out.println("Centroid 1 of the " + (i + 1) + "th file is " + newCentroids02.get(i).get(0).x + " and "
					+ newCentroids02.get(i).get(0).y);
			System.out.println("Centroid 2 of the " + (i + 1) + "th file is " + newCentroids02.get(i).get(1).x + "and "
					+ newCentroids02.get(i).get(1).y);
		}

		InetAddress localHostAddress = InetAddress.getLocalHost();
		// getting local address to start new socket

		int change = 0; // file counter
		DatagramSocket Random = new DatagramSocket(); // new docket initialized
		int timesCount = 0;
		int timeoutCount = 1;

		for (int i = 0; i < 2; i++) {

			try {
				timesCount = 0; // renew timeout and timescount variables for each packet
				timeoutCount = 1;
				while (timesCount < ALLOWED_ATTEMPTS) {
					Random.setSoTimeout(timeoutCount * 1000);
					timeoutCount = timeoutCount * 2;
					int x = 0;
					int y = 0;
					// temporary x and y int variables to convert to byte values

					byte b3 = 0, b2 = 0, b1 = 0, b0 = 0;
					// byte values

					int j = 1; // bytearray position counter
					byte[] CLUSbyte = new byte[17]; // defining CLUS packet bytearray

					CLUSbyte[0] = 4; // setting CLUS packet data type

					while (j < 17) { // array position filling loop
						for (int l = 0; l < 2; l++) {
							// loop for each centroid

							x = (int) ((newCentroids02.get(i).get(l).x) * 100);
							y = (int) ((newCentroids02.get(i).get(l).y) * 100);

							b3 = (byte) ((x >> 24));
							CLUSbyte[j] = b3;
							j++;
							b2 = (byte) ((x >> 16) & 255);
							CLUSbyte[j] = b2;
							j++;
							b1 = (byte) ((x >> 8) & 255);
							CLUSbyte[j] = b1;
							j++;
							b0 = (byte) ((x) & 255);
							CLUSbyte[j] = b0;
							j++;

							b3 = (byte) ((y >> 24));
							CLUSbyte[j] = b3;
							j++;
							b2 = (byte) ((y >> 16) & 255);
							CLUSbyte[j] = b2;
							j++;
							b1 = (byte) ((y >> 8) & 255);
							CLUSbyte[j] = b1;
							j++;
							b0 = (byte) ((y) & 255);
							CLUSbyte[j] = b0;
							j++;
							// byte values calculations and creating the byte array for CLUS packet
						}

					}

					DatagramPacket CLUSpacket = new DatagramPacket(CLUSbyte, CLUSbyte.length, localHostAddress, 2400);
					Random.send(CLUSpacket);
					// sending the CLUSpacket

					byte[] CACKbyte = new byte[1];
					DatagramPacket CACKpacket = new DatagramPacket(CACKbyte, CACKbyte.length);
					try {
						Random.receive(CACKpacket);
						// receiving the CLUS acknowledgement
					} catch (SocketTimeoutException ste) {
						System.out.println("Socket timed out!");
						Random.close(); // Closes when the DACK sequence number is wrong
						System.exit(0); // exit
					}
					Random.receive(CACKpacket);
					// receiving the CLUS acknowledgement

					if (CACKbyte[0] == 5 && change == 0) {
						System.out.println("Scessfully sent 1st file Centroids");
						// CLUSpacket acknowledgement received for the first file
						break;
					}

					else if (CACKbyte[0] == 5 && change == 1) {
						System.out.println("Scessfully sent 2nd file Centroids");
						System.out.println("END SERVER");
						break;
						// CLUSpacket acknowledgement received for the second file
					}

					else {
						System.out.println("Packet is not approved and has to be sent again");
						timesCount++;
						continue;
					}
				} // while allowed

				if (timesCount == ALLOWED_ATTEMPTS)
					throw new Exception("");

			} // try block

			catch (Exception f) {
				if (timesCount == ALLOWED_ATTEMPTS) {
					System.out.println("Client's Socket Timeout");
					System.out.println("\nError: Communication Failure");
					System.out.println("Socket Close");
					Random.close(); // Closes when the DACK sequence number is wrong
					System.exit(0); // exit
				}
			}

			change++;

		} // for receive

	}// main loop

	private static List<Coordinates> deepCopyArray(List<Coordinates> inputArrayList) {
		// method to deep copy between lists

		List<Coordinates> returnList = new ArrayList<Coordinates>();
		for (Coordinates coordinates : inputArrayList) {
			Coordinates newCoordinates = new Coordinates();
			newCoordinates.x = coordinates.x;
			newCoordinates.y = coordinates.y;
			// deep copying between two lists

			returnList.add(newCoordinates);
		}

		return returnList;
	}
}// class loop

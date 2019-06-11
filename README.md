# UDP-Client-Server-Packet-Creation-and-Encryption-with-K-Means-Clustering

## Overview

Distributed networking application in Java consisting of a client and a server. The client will send a block of data to the 
server, the server will perform some computations and return the result to the client. The application uses Java’s UDP 
sockets (classes DatagramPacket and DatagramSocket and their methods) and provide the necessary reliable data transfer 
functionality on the top of UDP’s unreliable communication services by implementing the data transfer protocol described 
below.

### Packet Structure

The Java application uses the following packets for communication between the client and the server. When transmitting multi-byte quantities (e.g. sequence numbers, acknowledgement numbers, etc.), they are transmitted in network byte order. That is, the most significant byte is transmitted first, the second most significant byte is transmitted next, and so on. The least significant byte is transmitted last.

Using the data packet (referred to as DATA packet), the client will send a number of two-dimensional (2D) vectors to the server. The DATA packet has have the following structure:

<p align="center">
<img src="/images/udp1.png" "Packet Structure">
</p>

The fields of the DATA packet are as follows:
* Packet type (1 byte): This field describes the type of the packet, and it has a value of 00h (the byte values will be       given in hexadecimal notation).
* Sequence number (2 bytes): The sequence number is a 16-bit unsigned integer, and it counts packets (and NOT bytes). Its     initial value is 0, and it is incremented by one for each additional sent DATA packet.
* Number of data vectors: This is a 16-bit unsigned integer, and its value is the number of 2D vectors carried by the DATA     packet. The maximum value of this field is 50; that is, a DATA packet can carry at most 50 2D data vectors.
* Data vectors: This field is the payload of the packet and carries the floating-point 2D data vectors in a byte-encoded       format. The process of encoding the floating-point data vectors into a sequence of data bytes will be described later.

The data acknowledgment (DACK) packet will be sent by the server to the client to acknowledge every DATA packet. Its structure is shown below.

<p align="center">
<img src="/images/udp2.png" "DACK Packet Structure">
</p>

The DACK packet has the following fields:
* Packet type (1 byte): This field describes the type of the packet, and it has a value of 01h.
* Acknowledgment number: This 16-bit field contains the sequence number of the last correctly received DATA packet.
  The request (REQ) packet will be sent by the client to the server once all data vectors have been sent, and it will         request the server to start the computation on the sent data vectors. The REQ packet has a very simple structure:
  
 <p align="center">
<img src="/images/udp3.png" "REQ Packet Structure">
  </p>
 
 The packet type field (1 byte) has the value of 02h.
 
The request acknowledgement (RACK) packet will also consist of a single byte, and it will be sent by the server to the client to signal the receipt of the REQ packet. The RACK packet is shown below:

<p align="center">
<img src="/images/udp4.png" "RACK Packet Structure">
</p>

The packet type field (1 byte) has the value of 03h.

The cluster information (CLUS) packet will be sent by the server to the client and will contain the
result of the computation, which will be two 2D vectors representing the calculated cluster centroids. The
CLUS packet will have the following structure

<p align="center">
<img src="/images/udp5.png" "CLUS Packet Structure">
</p>

The CLUS packet has the following fields:
* Packet type (1 byte): This field describes the type of the packet, and it has a value of 04h.
* Data vectors (16 bytes): This field is carries 2 floating-point 2D data vectors in a byte-encoded format. These two data vectors are the result of the server’s computation, and contain the 2 cluster centroids. The process of encoding the floating-point data vectors into a sequence of data bytes will be described later. 

The cluster information acknowledgment (CACK) packet will be sent by the client to the server, acknowledging the receipt of the CLUS packet. The CACK packet will consist of a single byte:

<p align="center">
<img src="/images/udp6.png" "CACK Packet Structure">
 </p>

### Data Vector Byte Encoding

The data vectors are 2D real vectors, and their coordinates (components) is stored and manipulated as floating-point values. However, in order to be able to transmit them over a byte-oriented transport protocol, they will need to be converted into a byte sequence. This conversion takes place as follows.

First, each floating-point value representing a dimension (coordinate) of the data vector is multiplied by 100 and then converted to a 32-bit integer value. Then, using bit shift and bit masking operations, all 4 bytes of the 32-bit integer is extracted and represented as individual byte values. As the result, a single floating-point dimension will be represented as 4 bytes, and a 2D data vector will be represented as 8 bytes.

Both dimensions (coordinates) is converted to a byte sequence and transmitted. The 1st dimension (coordinate) is transmitted first, and the 2nd dimension (coordinate) is transmitted last. The 4 bytes of each dimension (coordinate) is transmitted in network byte order. The reverse (byte sequence to floating-point value) conversion follows the same steps in reverse order. A 32-bit integer value is assembled from the contributing 4 byte values, the integer is converted to a floating-point value, and this value is divided by 100.

### Protocol Operation

#### CLIENT

First, you need to set up the client and server UDP socket parameters (IP addresses, port numbers etc.) so that the two sides could communicate with each other. Then, the client reads in the floating-point 2D data vectors from a data file (provided with the project description) and store them in an appropriate data structure (array or ArrayList ). The data files contain one 2D data vector per line enclosed in parentheses, and the dimensions (coordinates) of the vector are separated by commas. The first value on each line is the 1 st dimension (coordinate), and the second value is the 2 nd dimension (coordinate) of the 2D data vector. Two data files will be provided ( data01.txt and data02.txt ), The file data01.txt contains 243 2D vectors, while the data02.txt file contains 114 vectors. The application runs with both data files in two separate runs and the output recorded for both runs separately. The system works in three consecutive phases: the data vector upload phase, the computation request phase and the result download phase. The client protocol operates as follows:

  * Data vector upload phase
    * The client creates as many DATA packets as necessary to be able to send all data vectors to the server, fill out the         header fields of each DATA packet and insert the byte-encoded data vectors in the payload. Then, it sends the DATA           packets one by one to the server.
    * The client waits for a DACK packet from the server before sending the next DATA packet. A DACK packet can only be           accepted if: a) it has the correct packet type, and b) the ACK number is equal to the sequence number of the                 previously sent DATA packet. All other received packets are discarded (ignored).
    * The client also starts a timer upon sending each DATA packet, and retransmit the packet if the timer expires before         receiving a DACK packet from the server. The initial timeout value is set to 1 second and is doubled on each timeout         event. After the 4th timeout event, the transmitter declares communication failure and print an error message.
       
  * Computation request phase
    * The client sends a REQ packet to the server, signaling that all data vectors have been sent and asking the server to         start computing the result. Then, the client waits for a RACK packet from the server. A RACK packet can only be             accepted if it has the correct packet type field.
    * The client also start a timer upon sending the REQ packet, and retransmit the packet if the timer expires before             receiving a RACK packet from the server. The initial timeout value is set to 1 second and is doubled on each timeout         event. After the 4th timeout event, the transmitter declares communication failure and print an error message.
       
  * Result download phase
    * In this phase, the client will become the receiver and wait for a CLUS packet from the server. If it does not receive       the CLUS packet in 30 seconds, it stops and declare server failure.
    * When the client receives the CLUS packet, it sends back a CACK packet to the server acknowledging the receipt of the         CLUS packet. A CLUS packet can only be accepted if it has the correct packet type.
    * Finally, the client extracts the two 2D data vectors from the CLUS packet, convert them into floating-point vectors         and display them on the screen. It also waits for any additional (duplicate) CLUS packets from the server, and send         back a CACK packet if needed.

#### SERVER

The server implements the following protocol.
  * Data vector upload phase
    * The server waits for and receive the DATA packets, and send a DACK packet to the client if the DATA packet is               correctly received. The ACK number field in the DACK packet is set to the sequence number of the last correctly             received DATA packet. The DATA packet is correctly received only if it has the correct sequence number and the               correct packet type. If the packet type is correct, but the sequence number is not, it sends back a DACK packet with         the ACK number field set to the sequence number of the last correctly received DATA packet, but the content of the           out-of-order DATA packet is ignored. All other received packets are discarded (ignored) without any response. The           server knows that the client’s initial sequence number is 0.
    * The server extracts the byte-encoded data vectors from each correctly received DATA packet, convert them to a               floating-point format and store them in an appropriate data structure. Note that at this point, the server does             not know the total number of data vectors it will receive, so storing the data vectors in an ArrayList seems more           preferable.
    * The server keeps receiving DATA packets until it receives a REQ packet. The REQ packet signals that all data vectors         have been sent by the client.
   
   * Computation request phase
    * Upon receiving the REQ packet, the server sends a RACK packet to the client. Note that a RACK packet may get lost, so       the server may receive it multiple times. The server starts a timer set to 3 seconds, and if it does not receive any         REQ packets during this time, it can assume that the RACK packet has been received by the client.
    * The server then starts the computations. It took all 2D data vectors received from the client, and perform K-means           clustering on them assuming two clusters. The details of the clustering algorithm are provided below. The result of         the computation will be two data vectors, representing the centroids of the two clusters.

   * Result download phase
    * When the server is done with its computations, it takes the resulting two cluster centroid vectors, convert them to         byte-encoded format, create the CLUS packet and send it to the client. 
    * The server waits for a CACK packet from the client and start a timer upon sending the CLUS packet. It retransmits the       CLUS packet if the timer expires before receiving a CACK packet from the client. The initial timeout valu is set to 1       second and is doubled on each timeout event. After the 4th timeout event, the server declares communication failure         and print an error message.

### K-means Clustering

The server performs K-means clustering on the received data vectors. K-means clustering is an unsupervised machine learning algorithm, which takes a number of input vectors and separates them into K clusters. In this case, there will be two clusters, so K = 2. Figure 1 shows an example of such data set.

<p align="center">
<img src="/images/udp7.png" "Input Data Distribution">
  </p>

After the algorithm has run, the data set will be partitioned into K non-overlapping (convex) clusters,
and the centroid (center point) of each cluster will be calculated. Theses centroids are used to represent
the clusters, and new data points can be assigned to the cluster whose centroid is closest to the new data
point. An example clustering result is depicted in Figure 2 for the data set of Figure 1 with two clusters.
The cluster assignment is shown by different colors, and the centroids are denoted by circles.

<p align="center">
<img src="/images/udp8.png" "Final Cluster Assignment and Cetroids">
</p>

<p align="center">
<img src="/images/udp9.png">
  </p>

The result will be the last cluster assignment and the last cluster centroid vectors, m1 and m2. These two cluster centroid vectors are returned by the server to the client.

## Software

JAVA Socket Programming (Compatible with different machines over same Network).

## Flow Chart

<p align="center">
<img src="/images/udp12.png" "Flow Chart">
  </p>

## UML Diagrams

<p align="center">
<img src="/images/udp11.png" "UML Diagrams">
  </p>

## OUTPUT

<p align="center">
  <img src="/images/udp10.png" "Outputs">
  </p>

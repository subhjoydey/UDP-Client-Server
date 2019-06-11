# UDP-Client-Server-Packet-Creation-and-Encryption-with-K-Means-Clustering

## Overview

Distributed networking application in Java consisting of a client and a server. The client will send a block of data to the 
server, the server will perform some computations and return the result to the client. The application uses Java’s UDP 
sockets (classes DatagramPacket and DatagramSocket and their methods) and provide the necessary reliable data transfer 
functionality on the top of UDP’s unreliable communication services by implementing the data transfer protocol described 
below.

### Packet Structure

The Java application should use the following packets for communication between the client and the
server. When transmitting multi-byte quantities (e.g. sequence numbers, acknowledgement numbers,
etc.), they should be transmitted in network byte order. That is, the most significant byte is transmitted
first, the second most significant byte is transmitted next, and so on. The least significant byte is
transmitted last.

Using the data packet (referred to as DATA packet), the client will send a number of two-dimensional
(2D) vectors to the server. The DATA packet should have the following structure:

![ScreenShot](/images/udp1.png "Packet Structure")

```
Give examples
```

### Installing

A step by step series of examples that tell you how to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc

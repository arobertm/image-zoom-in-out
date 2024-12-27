# -+image ZOOM IN-OUT

![+image Logo](github-background.png)

A distributed application for processing BMP images using Docker containers. The system consists of 6 containers handling different aspects of image processing:

- Java backend with REST API and JMS client
- Apache TomEE JMS broker
- MDB and RMI client container
- Two RMI server containers for image processing
- Node.js container with MongoDB and MySQL for data storage

## Features

- BMP image zoom in/out processing
- Distributed architecture using Docker
- Real-time processing status updates
- SNMP monitoring
- Blob storage for processed images

## Technologies

- Frontend: React
- Backend: Java (Javalin, Jakarta EE)
- Message Broker: Apache TomEE
- Database: MongoDB, MySQL
- Container Platform: Docker
- Monitoring: SNMP

## Author
Alexandru Robert-Mihai

# c01-javalin-service

This project is a simple image zoom in/out service built using Javalin.

## Prerequisites

- Java 11 or higher
- Maven

## Building the Project

To build the project, navigate to the project directory and run the following command:

```sh
mvn clean install
```

## Running the Service

To run the service, use the following command:

```sh
java -jar ./target/c01-image-service-1.0-SNAPSHOT.jar
```

## Usage

Once the service is running, you can access it at `http://localhost:7000`.

## Endpoints

- `GET /zoom-in` - Zooms in the image.
- `GET /zoom-out` - Zooms out the image.

## License

This project is licensed under the MIT License.
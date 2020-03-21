# Visable Chat

This project is aimed at providing a small chat api to showcase springboot written in kotlin
The Endpoints are:
```
POST /users - Create User to chat
GET /users/{username} - Find the Id of a user by nickname
POST /users/{userId}/messages - Send a chat message to another user
GET /users/{userId}/messages - Get a list of a users recieved messages
GET /users/{userId}/messages?sent=true - Get a list of a users sent messages
```

In addition
```$xslt
GET /users/{userId}/messages has few other abilities.
You can sort messages in ascending or descending by addeding sort=(asc|desc) as a query param.(by default results are sorted asc order)
Furthermore, you can get a list of messages a user has sent to or from another user by adding to or from as a query param.
(i.e. /users/{userId}/messages?to=<other_usedId> or /users/{userId}/messages?from=<other_usedId>
```

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
Here are few applications you will need to get started.

* [Docker](https://www.docker.com/products/docker-desktop) - Container Management
* [Maven](https://maven.apache.org/) - Dependency Management
* [Java8](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html) - JVM used to run kotlin

### Installing

Here are a series of commands to help you get up and running.

First clone this repo
```$xslt
git clone 
```

Use docker to spin the postgresDB, rabbitmq, nginx, and the chat api itself.
```$xslt
docker-compose up -d

docker-compose down //This shutsdown the services
```

In order to run this code through mvn or your IDE. You will need to spin up postgres and rabbitmq
```$xslt
docker-compose up -d postgres rabbitmq
```

## How to query API

1.) POST /users
```$xslt
curl --location --request POST 'http://localhost:8080/users' \
--header 'Content-Type: application/json' \
--data-raw '{
	"nickname":"testUser"
}'
```
2.) GET /users/{username}
```
curl --location --request GET 'http://localhost:8080/users/testUser'
```

3.) POST /users/{userId}/messages 
```$xslt
curl --location --request POST 'http://localhost:8080/users/1/messages' \
--header 'Content-Type: application/json' \
--data-raw '{
	"recipient":2,
	"message":"Hello"
}'
```

4.)GET /users/{userId}/messages (All Received Messages)
```$xslt
curl --location --request GET 'http://localhost:8080/users/1/messages'
```

5.)GET /users/{userId}/messages (All Sent Messages)
```$xslt
curl --location --request GET 'http://localhost:8080/users/1/messages?sent=true'
```

5.)GET /users/{userId}/messages (Sent to a Specific User)
```$xslt
curl --location --request GET 'http://localhost:8080/users/1/messages?sent=true&to=2'
```

6.)GET /users/{userId}/messages (Received from a Specific User)
```$xslt
curl --location --request GET 'http://localhost:8080/users/1/messages?from=2'
```

7.)GET /users/{userId}/messages (Sorting enabled)
```$xslt
curl --location --request GET 'http://localhost:8080/users/1/messages?sort=(asc|desc)'
```

## Running the tests

Before running tests you will need to insure your current docker-compose containers are not running
```$xslt
docker-compose down
```

In order to run the test do
```$xslt
mvnw tests
```
## Deployment

Pretty much any platform that supports java applications.

## Authors

* **Adefemi Adeyemi** - *Initial work* - [ImperiousEnterprise](https://github.com/ImperiousEnterprise)


## License

This project is licensed under the Creative Commons Zero v1.0 Universal - see the [License.md](LICENSE.md) file for details


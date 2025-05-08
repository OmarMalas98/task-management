# Keycloak Setup(Windows)

Install and Run Keycloak (Standalone) on Windows
Download Keycloak from the official site.

- Extract the ZIP file.

 - Start Keycloak using:

```bash
bin\kc.bat start-dev
```
- Open the UI (localhost:8080)
- Create an admin user using the UI

### Create a Realm and Client in Keycloak
Go to Admin Console then create a new Realm (e.g., task.management).

Under that realm, go to Clients then Create:
+ Client ID: task.management
+ Client Protocol: openid-connect
+ Root URL: `http://localhost:8000/ (Spring Boot app URL)`
Enable "Standard Flow" and "Direct Access Grants".

Then we need to get the Client secret(if the Client is confidential) and add it to the application.properties file under the key `spring.security.oauth2.client.registration.keycloak.client-secret`

 as well as the client id under 

`spring.security.oauth2.client.registration.keycloak.client-id` 

and the scope as 

`spring.security.oauth2.client.registration.keycloak.scope`

 and 

`spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/task.management`


From the left menu click on User to create a new user and credentials.

# Build and run instructions for the Spring Boot application.
You can build the finished code into a `jar` by running this line in the terminal:
```bash
.\mvnw.cmd clean package
```
And you can run by running this in the terminal:
```bash
java -jar target/task.api-0.0.1-SNAPSHOT.jar
```
## Instructions on obtaining a JWT from Keycloak for testing.
To obtain the JWT we can use Postman to make a `post` request to this url `http://localhost:8080/realms/task.management/protocol/openid-connect/token`
which has the following body:
+ grant_type: password
+ client_id: (the id of the client)
+ username: (the username of created user)
+ password: (the user password)
this will return a response which  include an access token and a refresh token

# Postman Collection
[Task Management.postman_collection.json](https://github.com/user-attachments/files/20101186/Task.Management.postman_collection.json)

# Design Decisions & Justifications

## Authorization logic
I chose the service layer authorization using a custom `SecurityUtils` to make sure that the user is authenticated and retrieve the Id of the user, this was used for reasons:
+ Makes this functionality more reusable and maintainable.
+ Makes the controller cleaner, with making all the business logic in the service layer.
## Keycloak explanation and over
###  When a user logs in to your Keycloak server:
+ Keycloak authenticates the user.
+ Keycloak issues a signed JWT (access token) containing user info, roles, etc.
+ The client (e.g., frontend or Postman) includes this token in the Authorization: Bearer <token> header in subsequent API requests.
### Spring Boot Configuration for JWT Validation
+ `issuer-uri` in application.properties points to the Keycloak realm.
+ Spring Security uses that uri to validate the JWT from the request.
### Extracting User ID from the JWT
Done in the `SecurityUtils` C[Task Management.postman_collection.json](https://github.com/user-attachments/files/20101180/Task.Management.postman_collection.json)
lass.

Note: The `SecurityConfig` Class is crucial as it registers the Spring Security as OAuth2 Resource Server and makes the paths protected.
## Challanges
+ KeyCloak was a new venture, but with enough searching and reading, it was setup in a couple of hours.


Note: in this project I used MySQL, the Keycloak is on port `8080` and the Spring app is on `8000`

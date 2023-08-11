# Account Service
 
This application is a RESTFul API developed for Hyperskill (by JetBrains Academy) course 
Spring Security for Java Backend Developers.

## Build and run

1. Build `./gradlew build`
2. Run `./gradlew bootRun`

You also might want to run it in your IDEA; just import it, build and run `AccountServiceApplication.java`

## OpenAPI

After starting the app, if you want to check the Swagger UI, please access the link below:

``https://localhost:28852/account-services/swagger-ui/index.html``

## About Spring Security and User Roles

### User Roles

This API has 4 different roles:
1. Administrator
2. User
3. Accountant
4. Auditor
5. 
This application has some rules about those user roles:
1. First created user will be Administrator by default;
2. Other new users will be created with `User` role. 

If you want to update any user role, you can use `PUT` endpoint available in the `UserController` - for more details please 
check our Swagger. 

### Authorization

Following below a table illustrating the endpoints and the respective user role necessary to hit it:

|                             | Administrator | User | Accountant | Auditor |
|-----------------------------|---------------|------|------------|---------|
| GET: /api/empl/payment      |               |   X  |      X     |         |
| POST: /api/auth/changepass  |       X       |   X  |      X     |    X    |
| POST: /api/acct/payments    |               |      |      X     |         |
| PUT: /api/acct/payments     |               |      |      X     |         |
| GET: /api/admin/user/       |       X       |      |            |         |
| PUT: /api/admin/user/role   |       X       |      |            |         |
| DELETE: /api/admin/user/**  |       X       |      |            |         |
| PUT: /api/admin/user/access |       X       |      |            |         |
| GET: /api/security/events/  |               |      |            |    X    |

## SSL

Finally, this application is configured to use TLS 1.2 with self-signed certificate located at `resources/keystore` folder;
feel free to change it as this certificate was created for learning purposes only. 
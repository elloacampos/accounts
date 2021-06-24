# challenge

Suggestion of improvements to go live a first version:
- Update SpringBoot library
- Change project to run using latest Gradle version
- Implement Authentication service and request a token to authorize clients
- Implement cache to improve performance, i.e. clients (the owners of the accounts) can be cached
- Use Spring Profiles to be easier to test and run on local environment
- Configure Spring Boot Actuator to monitor the endpoints
- Use Swagger to document the API
- Configure application to use database
- Configure any tool(s) to automate the deploy (uDeploy, TeamCity)
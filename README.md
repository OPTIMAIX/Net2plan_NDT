# OaaS Algorithm Repository: NDT with Net2Plan

## Run locally

In order to test the algorithms, use Spring Boot App as "Run Configurations" type. This will execute the microservice.

Go to: <http://localhost:55955/swagger-ui/index.html> in order to use the OpenAPI documentation.

## Docker Registry

* This Algorithm Repository for OaaS Platform is configured to be deployed on the E-Lighthouse Registry for OaaS: <https://registry.e-lighthouse.com/>

## How to push new image:

1. Inside "Run configurations", create new "Maven Build"
2. Complete the form as:

    * Base directory: workspace
    * Goals: `clean package`
    * Skip Tests: `True`
    * Parameters:

        * `jib.to.auth.username` = "user"
        * `jib.to.auth.password` = "password"


_Note_: change "user" and "password" with a valid user for the used registry.
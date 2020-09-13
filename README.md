# Time Travel API

## Technologies used
* Heroku(For deploying on cloud)
* JAVA 11
* Spring boot 2.3.3
* Cache(Caffeine)
* Mongo 4.x
* Maven
* Junit 5
* ELK stack for logs
* Docker
* Docker-compose
* Swagger

## How to Run

 * You can see the api endpoints at http://localhost:8080/swagger-ui.html
 * In the swagger endpoint is possible to try. Example of expected body -> `{
                                                                                  "date" : "2020-09-11",
                                                                                  "pgi" : "a1321",
                                                                                  "place" : "London"
                                                                              }`
 * It's possible to health check the api at `/actuator/health`
 
### Running local with everything on a container :
 `./mvnw clean package` 
 `cd deps`
 `docker-compose build`
 `docker-compose up -d`
 
 * You can edit the docker-compose yml and add a new-relic license key to see/monitoring the api at newrelic.
 * You can see logs on a local kibana at http://localhost:5601 just need to create an index on kibana for be able to 
 look at the logs.

### Running the jar with only mongo running on a docker: 
  `./mvnw clean package` 
  `cd deps`
  `docker-compose up -d mongo` 
  `java -jar target/time-travel-api-0.0.1.jar`
  
### Running on cloud
just access the endpoint `https://time-travel-api.herokuapp.com/actuator/health` 
and check if api is up(heroku free tier sleeps the container for not consume resources while not being used)
after that, just start to make request 
here are some endpoints examples `https://time-travel-api.herokuapp.com/swagger-ui.html`
  
## Body Example
`{
    "date" : "2020-09-11",
    "pgi" : "a1321",
    "place" : "London"
}`
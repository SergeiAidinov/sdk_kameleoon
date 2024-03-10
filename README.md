![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Debian](https://img.shields.io/badge/Debian-D70A53?style=for-the-badge&logo=debian&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

# Contents
 - [Description](#description) 
 - [Deployment](#deployment) 
 - [Properties](#properties)

## Description
A user interacts with application via Swagger, which opens at http://localhost:8080/swagger-ui/index.html

![Screenshot](https://github.com/SergeiAidinov/images/blob/main/Screenshot.png?raw=true)


## Deployment
For deployment execute the next commands: 

- clone the repository: git clone https://github.com/SergeiAidinov/sdk_kameleoon.git

- change the current directory: cd sdk_kameleoon/

- create a jar file: mvn package

- change the current directory: cd target

- start application: java -jar sdk_kameleoon-0.0.1-SNAPSHOT.jar 

- stop application at Linux operating system: pkill -f sdk_kameleoon-0.0.1-SNAPSHOT.jar

## Properties

| Property Name                     | Description                                                                                                                                                                                                                           |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| app.apiKey                        | API KEY for using weather API                                                                                                                                                                                                         |
| app.cache.retention.timeInMinutes | Time (in minutes) between cache updates                                                                                                                                                                                               |
| app.cache.size                    | Max quantity of the cities, whose weather reports kept in cache                                                                                                                                                                       |
| app.cache.mode                    | Mode of cache update: <b>on_demand</b> (when user requests weather report) or <b>polling</b> (in equal periods, defined with property 'app.cache.retention.timeInMinutes'                                                             |
| app.language                      | language of weather reports:  af, al, ar, az, bg, ca, cz, da, de, el, en, eu, fa, fi, fr, gl, he, hi, hr, hu, id, it, ja, kr, la, lt, mk, no, nl, pl, pt, pt_br, ro, ru, sv, sk, sl, sp, es, sr, th, tr, ua, uk, vi, zh_cn, zh_tw, zu |
| app.units                         | System of units of measurement: <br/>standard, metric, imperial                                                                                                                                                                       |

































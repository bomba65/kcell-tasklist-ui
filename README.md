## Getting started

#### Portainer docker web UI
Create portainer volume to persist data
```
docker volume create portainer_data
```
Run portainer
```
docker run --restart always --name portainer -d -p 9000:9000 -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer
```
---

#### Create docker images
docker build -t kcell/headless_chrome ./headless-chrome/

#### Install frontend npm packages
cd kcell-tasklist-ui
cd js
npm install

#### docker-compose commands
minimum
```
docker-compose -f docker-compose-ps.yml up -d
```
all services
```
docker-compose -f docker-compose-ps-test.yml up -d
```

#### Camunda website
http://localhost/camunda/app/welcome/default/#/login
<br>
login - demo 
<br>
pass - demo
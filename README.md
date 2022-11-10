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
local-dev
```
docker build -t kcell/headless_chrome ./headless-chrome/
```
test-environment
```
docker build -t kcell/process-app ./kcell-process-app
docker build -t kcell/asset-management ./asset-management
docker build -t kcell/directory-management ./directory-management
```

#### Install frontend npm packages
```
cd ./kcell-tasklist-ui/js
npm install
```
#### docker-compose commands
local-dev
```
docker-compose -f docker-compose-ps.yml up -d
```
test-environment
```
docker-compose -f docker-compose-ps-test.yml up -d
```

#### Camunda website
http://localhost/camunda/app/welcome/default/#/login
<br>
login - demo 
<br>
pass - demo

#### deploy to core.test-flow.kcell.kz
```
sudo su
cd /home/user/KWMS-BPM
git pull
docker build -t kcell/process-app ./kcell-process-app
docker-compose -f docker-compose-ps-test.yml up -d
```

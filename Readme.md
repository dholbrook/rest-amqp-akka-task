# rest-amqp-akka-task

This is a demonstrator project with the goal of showing how to provide both REST and AMQP interfaces 
to the same microservice.

## Dependencies

There is a `docker-compose.yml` file in the root of the project to provide the 
required instances of MySQL and RabbitMq.  You will need [Docker](http://www.docker.com/products/overview).
Tested with [Docker For Mac](http://www.docker.com/products/docker#/mac), which includes docker and docker-compose.

    $ docker-compose up -d

## run the database migrations

The database created by docker will be empty, run the migrations to create the table.

    $ sbt flywayMigrate

## run the application

The application can be run from sbt.

    $ sbt run

## using the application

### Rest (httpie examples)

create

    http -v POST localhost:9000/task description="first task"

save

    http -v PUT localhost:9000/task/1 description="first task updated" complete:=true id:=1

find all

    $ http -v GET localhost:9000/task

find one

    $ http -v GET localhost:9000/task/1


### Amqp examples

You will need to install [Python (Mac Instructions)](http://docs.python-guide.org/en/latest/starting/install/osx/)

then install [pika](https://pika.readthedocs.org)

    $ pip install pika

the python script in ./scripts/task_amqp_rpc.py 

create

    $ ./scripts/task_amqp_rpc.py task.create '{"description": "first task"}'
    [x] Requesting
    key: task.create
    payload:{"description": "first task"}
    key: task.create
    Got {"id":1,"description":"first task","complete":false}

save
        
    $ ./scripts/task_amqp_rpc.py task.save '{"id": 1 "description": "first task updated", "complete": true}'
    [x] Requesting
    key: task.save
    payload:{"id": 1, "description": "first task updated", "complete": true}
    key: task.save
    Got {"id":1,"description":"first task updated","complete":true}
    
 find all
    
    ./scripts/task_amqp_rpc.py task.find.all ''
 
 find one
    
    ./scripts/task_amqp_rpc.py task.find.one '{"id": 1}'

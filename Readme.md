# rest-amqp-akka-todo

This is a demonstrator project with the goal of showing how to provide both REST and AMQP interfaces 
to the same microservice.

## boot2docker / docker-compose

There is a `docker-compose.yml` file in the root of the project to provide the 
required instances of MySQL and RabbitMq.  If you are running Windows or Mac 
you will need [boot2docker](http://boot2docker.io/).  In addition to a working
docker environment you will have to install 
[docker-compose](https://docs.docker.com/compose/install/).

    $ boot2docker up
    $ eval "$(boot2docker shellinit)"
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

the python script in ./scripts/task_amqp_rpc.py uses pika you will need python 
and [pika](https://pika.readthedocs.org/en/0.9.14/) installed.

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

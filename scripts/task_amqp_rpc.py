#!/usr/bin/env python
import pika
import uuid

import sys

class TaskAmqpClient(object):
    def __init__(self):

        self.connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))

        self.channel = self.connection.channel()

        result = self.channel.queue_declare(exclusive=True)
        self.callback_queue = result.method.queue

        self.channel.basic_consume(self.on_response, no_ack=True,
                                   queue=self.callback_queue)

    def on_response(self, ch, method, props, body):
        if self.corr_id == props.correlation_id:
            self.response = body

    def call(self, key, n):
        print "key: " + key
        self.response = None
        self.corr_id = str(uuid.uuid4())
        self.channel.basic_publish(exchange='amq.direct',
                                   routing_key=key,
                                   properties=pika.BasicProperties(
                                       reply_to = self.callback_queue,
                                       correlation_id = self.corr_id,
                                   ),
                                   body=n)
        while self.response is None:
            self.connection.process_data_events()
        return self.response

task_rpc = TaskAmqpClient()

key = sys.argv[1]
payload = sys.argv[2]

print " [x] Requesting \nkey: " + key + "\npayload:" + payload
response = task_rpc.call(key,payload)
print "Got " + response

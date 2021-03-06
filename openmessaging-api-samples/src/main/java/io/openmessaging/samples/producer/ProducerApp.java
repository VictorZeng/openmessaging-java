/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.openmessaging.samples.producer;

import io.openmessaging.Future;
import io.openmessaging.FutureListener;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;
import java.nio.charset.Charset;

public class ProducerApp {
    public static void main(String[] args) {
        final MessagingAccessPoint messagingAccessPoint =
            OMS.getMessagingAccessPoint("oms:rocketmq://alice@rocketmq.apache.org/us-east:default_space");

        final Producer producer = messagingAccessPoint.createProducer();
        messagingAccessPoint.startup();
        producer.startup();

        //Sync
        {
            SendResult sendResult = producer.send(producer.createBytesMessage(
                "HELLO_QUEUE", "HELLO_BODY".getBytes(Charset.forName("UTF-8"))));

            System.out.println("Send sync message OK, message id is: " + sendResult.messageId());
        }

        //Async with Promise
        {
            final Future<SendResult> result = producer.sendAsync(producer.createBytesMessage(
                "HELLO_QUEUE", "HELLO_BODY".getBytes(Charset.forName("UTF-8"))));

            final SendResult sendResult = result.get(3000L);
            System.out.println("Send async message OK, message id is: " + sendResult.messageId());
        }

        //Async with FutureListener
        {
            final Future<SendResult> result = producer.sendAsync(producer.createBytesMessage(
                "HELLO_QUEUE", "HELLO_BODY".getBytes(Charset.forName("UTF-8"))));

            result.addListener(new FutureListener<SendResult>() {
                @Override
                public void operationSucceeded(Future<SendResult> promise) {
                    System.out.println("Send async message OK, message id is: " + promise.get().messageId());
                }

                @Override
                public void operationFailed(Future<SendResult> promise) {
                    System.out.println("Send async message Failed, cause is: " + promise.getThrowable().getMessage());
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                producer.shutdown();
                messagingAccessPoint.shutdown();
            }
        }));
    }
}

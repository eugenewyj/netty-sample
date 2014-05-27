/*
 * Copyright 2012-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fengbaoxp.netty.definitive.ch2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * @author Eugene Wang
 * @since 0.0.1
 */
public class AioTimeClientHandler implements CompletionHandler<Void, AioTimeClientHandler>, Runnable {
    private String  host;
    private int     port;
    private AsynchronousSocketChannel channel;
    private CountDownLatch latch;

    public AioTimeClientHandler(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            channel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        latch = new CountDownLatch(1);
        channel.connect(new InetSocketAddress(host, port), this, this);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void result, AioTimeClientHandler attachment) {
        byte[] bytes = "QUERY TIME ORDER".getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (attachment.hasRemaining()) {
                    channel.write(attachment, attachment, this);
                } else {
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    channel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            byte[] bytes = new byte[attachment.remaining()];
                            attachment.get(bytes);
                            try {
                                String body = new String(bytes, "UTF-8");
                                System.out.println("Now is :" + body);
                                latch.countDown();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                channel.close();
                                latch.countDown();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    channel.close();
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, AioTimeClientHandler attachment) {
        exc.printStackTrace();
        try {
            channel.close();
            latch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

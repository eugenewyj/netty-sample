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
package org.fengbaoxp.netty.inaction.ch1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Eugene Wang
 * @since 0.0.1
 */
public class PlainEchoServer {

    public void server(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);
        try {
            while (true) {
                final Socket clientSocket = socket.accept();
                System.out.println("Accepted connection from :" + clientSocket);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BufferedReader
                                br =
                                new BufferedReader(
                                    new InputStreamReader(clientSocket.getInputStream()));
                            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
                            while (true) {
                                String value = br.readLine();
                                System.out.println(value);
                                pw.write(value);
                                pw.flush();
                                if ("exit".equals(value)) {
                                    break;
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PlainEchoServer server = new PlainEchoServer();
        try {
            server.server(9000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

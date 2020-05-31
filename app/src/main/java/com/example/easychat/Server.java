package com.example.easychat;

import android.util.Log;
import android.util.Pair;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Server {

    private WebSocketClient client;
    private Map<Long, String> names = new ConcurrentHashMap<>();
    private Consumer<Pair<String, String>> onMessageReceived;

    public Server(Consumer<Pair<String, String>> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void connect() {
        URI address = null;

        try {
//            address = new URI("ws://35.214.1.221:8881");
            address = new URI("ws://lab.nnbabkov.ru:8881");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        client = new WebSocketClient(address) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("SERVER", "Connected to server");
            }

            @Override
            public void onMessage(String json) {
                Log.i("SERVER", "Got json from server: " + json);
                int type = Protocol.getType(json);
                if (type == Protocol.MESSAGE) {
                    displayIncoming(Protocol.unpackMessage(json));
                }
                if (type == Protocol.USER_STATUS) {
                    updateStatus(Protocol.unpackStatus(json));
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("SERVER", "Connection closed");
            }

            @Override
            public void onError(Exception ex) {
                Log.i("SERVER", "ERROR: " + ex.getMessage());
            }
        };
        client.connect();
    }

    public void sendName(String name) {
        Protocol.UserName userName = new Protocol.UserName(name);
        if (client != null && client.isOpen()) {
            client.send(Protocol.packName(userName));
        }
    }

    public void sendMessage(String text) {
        try {
            text = Crypto.encrypt(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Protocol.Message mess = new Protocol.Message(text);
        if (client != null && client.isOpen()) {
            client.send(Protocol.packMessage(mess));
        }
    }

    private void updateStatus(Protocol.UserStatus status) {
        Protocol.User user = status.getUser();

        if (status.isConnected()) {
            names.put(user.getId(), user.getName());
        } else {
            names.remove(user.getId());
        }
    }

    private void displayIncoming(Protocol.Message message) {
        String name = names.get(message.getSender());
        if (name == null) {
            name = "Unnamed";
        }

        try {
            String text = Crypto.decrypt(message.getEncodedText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        onMessageReceived.accept(
                new Pair<>(name, message.getEncodedText())
        );
    }
}

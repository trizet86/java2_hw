package com.java2.lesson_7_8.Client;

import com.java2.lesson_7_8.Channel.NetDataChannel;
import com.java2.lesson_7_8.CmdRsp;
import com.java2.lesson_7_8.Log;
import com.java2.lesson_7_8.Messages.*;
import com.java2.lesson_7_8.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Client implements ClientController {
    private static final String TAG = "CLIENT";
    private final int CONNECT_TRY = 3;
    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 8189;
    private boolean connected = false;
    private boolean subscribed = false;
    private int connectTry = 0;

    private NetDataChannel net;
    private User user;

    private ClientUI clientUI;
    private ClientUI logInUI;

    private Thread receiverThread = null;
    private List<String> usersList = new ArrayList<>();



    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        logInUI = new LoginWindow(this);
    }

    private void connectThreadStart() {
        new Thread(() -> {
            while(connectTry < CONNECT_TRY) {
                if (connected) {

                    if(subscribed) {
                        connectTry = 0;
                    }
                    //netSend("<ping>");
                    //if(!subscribed) {
                    //    netSend(CmdRsp.CMD_AUTH + " " + user.getNickName() + " " + user.getPass());
                   // }
                } else {
                    connectToServer();
                    connectTry++;
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            logInUI.statusCallback(CmdRsp.RSP_ERR);
        }).start();
    }

    private void msgReceiverThreadStart() {
        if(receiverThread == null) {
            receiverThread = new Thread(() -> {
                while (true) {
                    if (connected) {
                        Message msg = net.getMessage();
                        if (msg != null) {
                            parseMessage(msg);
                        } else {
                            Log.e(TAG, "getMessage error. Ошибка сети!");
                            net.close();
                            connected = false;
                            subscribed = false;
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            receiverThread.start();
        }
    }

    private void connectToServer() {
        if(net != null) {
            net.close();
        }

        Log.i(TAG, "Попытка подключения к серверу - " + SERVER_ADDR + ":" + SERVER_PORT);
        try {
            net = new NetDataChannel(SERVER_ADDR, SERVER_PORT);
            Log.i(TAG, "Подключено!");
            //net.sendMessage(new AuthMessage(user));
            connected = true;
        } catch (IOException e) {
            Log.e(TAG,"Ошибка сети..." + e.toString());
            subscribed = false;
            connected = false;
        }
    }

    private void parseMessage(Message msg) {
        switch (msg.getType()) {
            case BROADCAST_MESSAGE:
                BroadcastMessage m = (BroadcastMessage) msg;
                clientUI.addMessage(m.getFrom() + ": " + m.getText());
                break;

            case PRIVATE_MESSAGE:
                PrivateMessage p = (PrivateMessage) msg;
                clientUI.addMessage("(PM)" + p.getFrom() + ": " + p.getText());
                break;

            case USERS_LIST_MESSAGE:
                UsersListMessage u = (UsersListMessage) msg;
                clientUI.setUsersList(u.getUsers());
                break;

            case INFO_MESSAGE:
                InfoMessage i = (InfoMessage) msg;
                clientUI.addMessage("Info: " + i.getText());
                break;

            case RESPONSE_MESSAGE:
                parseResponse((ResponseMessage) msg);
                break;

            default:
                Log.e(TAG, "Wrong message type");
        }
    }

    private void parseResponse(ResponseMessage msg) {
        switch (msg.getRsp()) {
            case RSP_OK_AUTH:
                subscribed = true;
                logInUI.statusCallback(msg.getRsp());
                if (clientUI == null) {
                    clientUI = new MainWindow(this);
                }
                break;

            case RSP_WRONG_AUTH:
            case RSP_NICK_BUSY:
            case RSP_AUTH_TIMEOUT:
                subscribed = false;
                connectTry = CONNECT_TRY;
                logInUI.statusCallback(msg.getRsp());
                break;

            case RSP_USER_NOT_FOUND:
                clientUI.addMessage("Пользователь не найден.\n\r");
                break;

            //case RSP_USERS_LIST:
                // ПЕРЕДЕЛАТЬ
                /*usersList.clear();
                usersList.addAll(Arrays.asList(cmd.split(" ")));
                usersList.remove(0);// (CmdRsp.RSP_USERS_LIST);

                if (clientUI != null) {
                    clientUI.setUsersList(usersList.toArray(new String[0]));
                }*/
                //break;

            default:
                Log.e(TAG, "Wrong response type");
        }

    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public List<String> getUsersList() {
        return usersList;
    }

    @Override
    public void logIn(String nickName, String pass) {
        connectTry = 0;
        user = new User(nickName, pass);
        connectThreadStart();
        msgReceiverThreadStart();
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void sendTextMessage(String msg) {
        sendMessage(new BroadcastMessage(user.getNickName(), msg));
    }

    @Override
    public void sendTextMessage(String toUser, String msg) {
        sendMessage(new PrivateMessage(user.getNickName(), toUser, msg));
    }

    private void sendMessage(Message msg) {
        if (connected && subscribed) {
            if( !net.sendMessage(msg) ) {
                Log.e(TAG, "sendMessage Ошибка сети!");
            }
        } else {
            clientUI.addMessage("Нет подключения к серверу!");
            Log.e(TAG, "Нет подключения к серверу!");
        }
    }
}
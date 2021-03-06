package com.java2.lesson_7_8.Client;

import com.java2.lesson_7_8.CmdRsp;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame implements ClientUI {
    private JTextArea textAreaChat;
    private JTextArea textAreaUsersList;
    private JTextField textFieldSend;
    private JButton buttonSend;
    private ClientController clientController;


    public MainWindow(ClientController clientController) {
        this.clientController = clientController;

        setTitle("Main window");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(300, 300, 400, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        textAreaChat = new JTextArea();
        textAreaChat.setEditable(false);
        add(textAreaChat, BorderLayout.CENTER);

        textAreaUsersList = new JTextArea();
        textAreaUsersList.setEditable(false);
        textAreaUsersList.setBackground(Color.LIGHT_GRAY);
        add(textAreaUsersList, BorderLayout.LINE_END);
        textAreaUsersList.append("Users:\n\r");

        // Панель с текстовым полем для отправки и кнопкой
        JPanel panelSend = new JPanel();
        panelSend.setLayout(new BorderLayout());

        textFieldSend = new JTextField();
        panelSend.add(textFieldSend, BorderLayout.CENTER);

        buttonSend = new JButton();
        buttonSend.setText("Отправить");
        panelSend.add(buttonSend, BorderLayout.LINE_END);

        add(panelSend, BorderLayout.PAGE_END);
        setVisible(true);

        // Обработчики
        textFieldSend.addActionListener(e -> sendText());
        buttonSend.addActionListener(e -> sendText());
    }

    private void sendText() {
        String msg = textFieldSend.getText().trim();
        textFieldSend.setText("");
        String myNickName = clientController.getUser().getNickName();

        if(msg.length() > 0) {
            if(msg.startsWith("/")) {
                String[] parts = msg.split(" ", 2);
                String toUser = parts[0].substring(1);
                if ( (parts.length == 2) /*&& (clientController.getUsersList().contains(toUser))*/ ) {
                    clientController.sendTextMessage(toUser, parts[1]);
                    textAreaChat.append("Я(" + myNickName + ")->" + toUser + ": " + parts[1] + "\n\r");
                } else {
                    textAreaChat.append("Пользователь не найден или не верная команда.\n\r");
                }
                return;
            }

            textAreaChat.append("Я(" + myNickName + "): " + msg + "\n\r");
            clientController.sendTextMessage(msg);
        }
    }

    @Override
    public void setUsersList(String[] usersList) {
        textAreaUsersList.setText("Users:\n\r");
        for(String user: usersList) {
            textAreaUsersList.append(user + "\n\r");
        }
    }

    @Override
    public void addMessage(String msg) {
        textAreaChat.append(msg + "\n\r");
    }

    @Override
    public void statusCallback(CmdRsp s) {}
}

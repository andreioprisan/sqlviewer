package com.dev.sqlapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ResourceBundle;

public class ConnectionDialogController implements Initializable {
    @FXML
    private TextField tfHostname;

    @FXML
    private TextField tfPort;

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void onConnect(ActionEvent event) {
        System.out.println("connect clicked!");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String hostname = tfHostname.getText().trim();
            String port = tfPort.getText().trim();
            String username = tfUsername.getText().trim();
            String password = tfPassword.getText().trim();

            if("".equals(hostname)) {
                Main.warning("Invalid connection setting", "The hostname is a required field.");
                return;
            }

            if("".equals(port)) {
                Main.warning("Invalid connection setting", "The port is a required field.");
                return;
            }

            if("".equals(username)) {
                Main.warning("Invalid connection setting", "The username is a required field.");
                return;
            }

            Main.conn = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port, username, password);

            if(Main.conn != null) {
                Main.mainController.onDBConnect();
                closeStage(event);
            }

        } catch (Exception e) {
            System.out.println(e);
            Main.error("Error", e.getMessage());
        }
    }

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}

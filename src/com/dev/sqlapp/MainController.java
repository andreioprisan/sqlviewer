package com.dev.sqlapp;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private MenuBar menuBar;

    @FXML
    private GridPane leftPane;

    @FXML
    private ListView listviewDB;

    @FXML
    private ListView listviewTable;

    @FXML
    private TextArea txtSqlQuery;

    @FXML
    private TableView tableviewResult;

    @FXML
    private Label lblDatabases;

    @FXML
    private Label lblTables;

    @FXML
    private Label lblResult;

    @FXML
    private MenuItem mnuCloseConn;

    private String curDb = null;
    private String curTable = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * handler for new connection menu
     * @param event
     * @throws IOException
     */
    @FXML
    public void handleNewConnection(final ActionEvent event) throws IOException
    {
        try {
            if (!Main.conn.isClosed()) {
                Main.conn.close();
                clearAll();
                mnuCloseConn.setDisable(true);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("connectiondialog.fxml"));
        Parent parent = fxmlLoader.load();
        ConnectionDialogController dialog = fxmlLoader.<ConnectionDialogController>getController();

        Scene scene = new Scene(parent, 300, 200);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Connection Setting");
        stage.showAndWait();
    }

    /**
     * handler for close action on the menu
     * @param event
     */
    @FXML
    private void handleCloseConnection(final ActionEvent event)
    {
        try {
            Main.conn.close();
            clearAll();
            mnuCloseConn.setDisable(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle action related to "About" menu item.
     *
     * @param event Event on "About" menu item.
     */
    @FXML
    private void handleAboutAction(final ActionEvent event)
    {
        Main.info("SQLViewer", "SQLViewer with JavaFX. By Andrei Oprisan");
    }

    /**
     * handler for exit menu
     * @param event
     */
    @FXML
    private void handleExit(final ActionEvent event)
    {
        try {
            Main.conn.close();
            clearAll();
            mnuCloseConn.setDisable(true);
            Platform.exit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * when db connected successfully, it loads database
     */
    public void onDBConnect()
    {
        mnuCloseConn.setDisable(false);

        // list all database names
        loadDatabases();
    }

    // event handler when user selects a database
    public void onSelectDB()
    {
        this.curDb = listviewDB.getSelectionModel().getSelectedItem().toString();
        System.out.println("current db = " + curDb);
        this.loadTables();
    }

    // load all database names and add to listview
    public void loadDatabases() {
        this.clearAll();

        try {
            Statement stmt = Main.conn.createStatement();
            ResultSet resultset = stmt.executeQuery("SHOW DATABASES;");
            resultset = stmt.getResultSet();

            listviewDB.getItems().clear();

            int i = 0;
            // iterate every database name and add it to listview
            while (resultset.next()) {
                System.out.println(resultset.getString("Database"));
                listviewDB.getItems().add(resultset.getString("Database"));
                i++;
            }
            lblDatabases.setText("Databases (" + i + ")");

            resultset.close();
            stmt.close();

            // set first database as selected
            if(i > 0) {
                // load the tables of first database
                listviewDB.getSelectionModel().select(0);
                this.curDb = listviewDB.getItems().get(0).toString();
                loadTables();
            }
        }
        catch (SQLException ex){
            // handle any errors
            ex.printStackTrace();
        }
    }

    // load all table names and add to listview
    public void loadTables() {
        try {
            Statement stmt = Main.conn.createStatement();
            stmt.executeQuery("USE " + this.curDb + ";");
            ResultSet resultset = stmt.executeQuery("SHOW TABLES;");
            resultset = stmt.getResultSet();

            listviewTable.getItems().clear();

            int i = 0;
            // iterate every table name and add it to listview
            while (resultset.next()) {
                System.out.println(resultset.getString(1));
                listviewTable.getItems().add(resultset.getString(1));
                i++;
            }
            lblTables.setText("Tables (" + i + ")");

            resultset.close();
        }
        catch (SQLException ex){
            // handle any errors
            ex.printStackTrace();
        }
    }

    /**
     * handler when select a table on listview
     */
    public void onSelectTable() {
        if(listviewTable.getItems().size() > 0)
            this.curTable = listviewTable.getSelectionModel().getSelectedItem().toString();
        else
            this.curTable = "";

        if("".equals(this.curTable) || this.curTable == null) {
            return;
        }

        String sql = "SELECT * FROM " + this.curDb + "." + this.curTable;
        txtSqlQuery.setText(sql);

        this.onRunQuery();
        //test();
    }

    private void clearAll() {

        listviewDB.getItems().clear();
        listviewTable.getItems().clear();
        tableviewResult.getItems().clear();
        tableviewResult.getColumns().clear();
        txtSqlQuery.setText("");
        lblResult.setText("Result");
    }

    /**
     * handler when click run query button
     */
    public void onRunQuery()
    {
        try {
            tableviewResult.getItems().clear();
            tableviewResult.getColumns().clear();

            Statement stmt = Main.conn.createStatement();
            String sql = txtSqlQuery.getText();

            // if user run SELECT query
            if(sql.toLowerCase().startsWith("select")) {

                long start_time = System.currentTimeMillis();

                ResultSet resultset = stmt.executeQuery(sql);
                resultset = stmt.getResultSet();

                long end_time = System.currentTimeMillis();

                ResultSetMetaData rsmd = resultset.getMetaData();

                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    final int j = i;
                    TableColumn<String[], String> column = new TableColumn<>();
                    column.setText(rsmd.getColumnName(i));

                    column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> p) {
                            String[] x = p.getValue();
                            if (x != null && x.length>0) {
                                return new SimpleStringProperty(x[j-1]);
                            } else {
                                return new SimpleStringProperty("");
                            }
                        }
                    });

                    tableviewResult.getColumns().add(column);
                }

                int r = 0;
                ArrayList<String[]> data = new ArrayList<String[]>();

                while (resultset.next()) {
                    String[] row = new String[rsmd.getColumnCount()];
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        row[i-1] = resultset.getString(i);
                    }
                    data.add(row);
                    r++;
                }

                tableviewResult.getItems().addAll(data);
                lblResult.setText("Result: Total " + r + " records, Elapsed Time: " + ((end_time - start_time)) + " ms");

                resultset.close();
            }
            else { // insert, update, delete query processing
                long start_time = System.currentTimeMillis();

                int r = stmt.executeUpdate(sql);

                long end_time = System.currentTimeMillis();

                lblResult.setText("Result: Affected " + r + " rows, Elapsed Time: " + ((end_time - start_time)) + " ms" );
            }
        }
        catch (SQLException ex){
            // handle any errors
            ex.printStackTrace();
            Main.error("SQL Error", ex.getMessage());
        }
    }
}

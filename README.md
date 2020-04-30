# sqlviewer
Simple JavaFX-based SQL viewer app

Below is a recap of the core functionality, after which we’ll explain how we designed the code solution.

##Functionality:
#1. Connect:
    a. DB Connection screen asking for hostname, username, password, and port
    b. Validation check for empty fields
    c. DB Connection screen disappears on successful connection
    d. Ability to reconnect to a different DB without exiting application

#2. List:
	a. List all available databases and select the first one
	b. List all available tables for selected database

#3. Query:
	a. SQL Query input a free text input box that allow for any legal SQL query to be executed, with statistics on affected rows and execution time that it took.
	b. SQL results table that allows for SELECT query data results to show
	c. Show content for selected table on the left side by default, by writing a SQL SELECT statement and executing.

##Code design:
#1. FXMLs:
	a. ConnectionDialog:
	Ask for 4 data fields: hostname, port, username, password
	Required fields that need to display validation if missing: hostname, port, username
		 
	On connect, validate credentials and share DB connection with the main screen app.
	b. MainScreen:
	Create app menubar with File > New Connection, Open Connection, Exit (Bonus: time permitting, add Help > About).
	Main screen should be full sized to all available screen real-estate and contain 4 sections: 1. Databases, 2. Tables, 3. SQL Query, 4. Results
	On connect from previous screen, show list of all databases and pick the first one.
	Show all tables available in the selected database, but don’t select any specific table.
    
#2. Main.java: (scene setup and utils)
	Load MainScreen.fxml layout.
	Provide modals for warning, info, and error messaging
 
#3. ConnectionDialogController.java: (DB connection input validator and connect)
	Validate ConnectionDialog required inputs
	Connect to DB and make connection available to MainController
 
#3. MainController.java: (main app logic)
	Handle new database connections: If no connection available, open up ConnectionDialog and let ConnectionDialogController.java process
	Handle closing db connections on Close Connection menu item click or app close
	Handle Help > About app menu click with simple info alert text with app version 
	On DB connection, show list of databases in databases list panel
	On DB selection, show list of tables in selected database list panel
	On DB table selection, run SELECT * query and show results in results table.
	SQL Query box should take any legal SQL queries and run then, with statistics on affected rows and time it took to complete. If a SELECT query, show results of query in table.
	Bonus: if time permits, allow for sort of data based on data output.


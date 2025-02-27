# Database Connection and Table Management

This project provides a framework for managing database connections and performing CRUD operations on SQL tables using Java. It supports multiple database types, including MySQL, MariaDB, and SQLite.

## Project Structure

- `connection`: Contains classes for managing database connections.
    - `MariaDBConnection.java`: Provides a connection URL specific to MariaDB.
    - `MySQLConnection.java`: Provides methods to connect to a MySQL database.
    - `SQLiteConnection.java`: Provides methods to connect to a SQLite database.
    - `SQLConnection.java`: An interface that defines methods for connecting to a SQL database and executing queries.

- `table`: Contains classes for managing SQL tables and their fields.
    - `Table.java`: Represents a SQL table with methods for creating, inserting, selecting, updating, and deleting records.
    - `fields`: Contains annotations and classes for defining metadata for database fields.
        - `DataField.java`: An annotation used to define metadata for a database field.
        - `ForeignKey.java`: An annotation used to define a foreign key constraint on a database field.
        - `PrimaryKey.java`: An annotation used to define a primary key constraint on a database field.
        - `SqlField.java`: Represents a field in a SQL table with its associated metadata and methods for serialization and deserialization.
        - `SqlType.java`: An annotation used to define the SQL type of a database field.

- `tables`: Contains example classes representing database tables.
    - `Order.java`: Represents an order in the system with a product and a user.
    - `User.java`: Represents a user in the system with an ID and a name.

- `Settings.java`: Contains configuration settings for the application.

- `Main.java`: Demonstrates the usage of the SQLiteConnection and Table classes.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 21 or higher
- Gradle

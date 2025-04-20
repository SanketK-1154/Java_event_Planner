# Event Planner Desktop Application

## Project Overview

The Event Planner is a desktop application developed in Java Swing designed specifically for a college environment. Its primary purpose is to streamline the management of events, tasks, and groups within the college. It provides distinct functionalities for two types of users: standard **Users** (likely students) and **Admins** (faculty, department heads, or club organizers).

The goal is to create a centralized platform where Admins can easily schedule and assign events or tasks to specific groups, and Users can view these alongside their personal events, manage their group memberships, and organize their schedules through list and calendar views.

## Features

### User Functionality

* **Secure Authentication:** Register and log in with email and password (passwords securely hashed).
* **Role-Based Access:** Automatically directed to the User Dashboard after login.
* **Personal Dashboard:** View profile information, basic activity stats, and upcoming events.
* **Profile Management:** Edit personal details like name and password (requires current password verification).
* **Event Management:**
    * View a combined list of personal events and events assigned to joined groups.
    * Add, edit, and delete personal events.
* **Calendar View:** Interactive monthly calendar highlighting days with events. View detailed event lists for selected dates.
* **Group Management:**
    * View a list of groups currently joined.
    * Join new groups using an admin-provided unique join code.
    * Leave existing groups.

### Admin Functionality

* **Secure Authentication:** Log in with admin credentials. Automatically directed to the Admin Dashboard.
* **Group Management:**
    * Create new groups (application generates a unique join code).
    * View and manage groups created by the admin.
    * View members of a selected group.
    * Remove specific users from a group.
    * Delete entire groups (automatically handles member removal and event unassignment).
* **Event Management:**
    * View events created by the admin (personal and group-assigned).
    * Create new events, with the option to assign them to a specific group or keep them personal.
    * Basic validation for event creation (e.g., checking for title/date conflicts).
    * Delete events created by the admin.

## Technology Stack

* **Programming Language:** Java (Likely JDK 17+)
* **User Interface (GUI):** Java Swing
    * Utilized standard Swing components (`JFrame`, `JPanel`, `JButton`, `JTable`, `JCalendar` via external library, etc.) and various Layout Managers (`BorderLayout`, `CardLayout`, `GridBagLayout`, etc.).
* **Database:** MySQL (Relational Database Management System)
* **Database Connectivity:** JDBC (Java Database Connectivity) using `mysql-connector-j`.
* **Password Security:** jBCrypt Library (For secure password hashing).
* **Build & Dependency Management:** Apache Maven
* **External Libraries:**
    * `com.toedter:jcalendar` (for the calendar UI component)
    * `org.mindrot:jbcrypt` (for password hashing)
    * `mysql:mysql-connector-java` or `com.mysql:mysql-connector-j` (for database connection)
* **Executable Format:** JAR (packaged using Maven Shade plugin)

## Architecture

The project follows a layered architecture to separate concerns:

* **Presentation Layer (UI):** Handles all visual elements and user interactions (Java Swing panels and frames).
* **Service Layer:** Contains the core application logic and business rules.
* **Data Access Object (DAO) Layer:** Manages all direct database interactions (SQL queries via JDBC).
* **Model Layer:** Contains POJOs representing data entities (User, Event, Group).
* **Utility Layer:** Provides helper classes (e.g., `PasswordUtil`).

Data is stored in a MySQL database with tables including `users`, `groups1`, `group_memberships`, and `events`.

## Prerequisites

Before running the application, ensure you have the following installed:

* **Java Development Kit (JDK):** Version 17 or higher recommended.
* **Apache Maven:** For building the project.
* **MySQL Server:** A running instance of MySQL.
* **MySQL Client:** Command-line client or a GUI tool like MySQL Workbench to set up the database.

## Setup Instructions

1.  **Clone the Repository:**
    ```bash
    git clone <your-repository-url>
    cd event-planner-project
    ```
    *(Replace `<your-repository-url>` with the actual URL of your GitHub repository)*

2.  **Database Setup:**
    * Connect to your MySQL server.
    * Create a new database (e.g., `event_planner`):
        ```sql
        CREATE DATABASE event_planner;
        USE event_planner;
        ```
    * Execute the SQL scripts to create the necessary tables:
        *(You will need to create these SQL files yourself based on your database schema. A common practice is to have `schema.sql` for table creation and `data.sql` for initial data like an admin user.)*
        ```bash
        # Example command to run schema script
        mysql -u your_mysql_user -p event_planner < path/to/your/schema.sql
        # Example command to run data script (if you have one for an initial admin)
        mysql -u your_mysql_user -p event_planner < path/to/your/data.sql
        ```
        *(Replace `your_mysql_user` and `path/to/your/schema.sql` / `path/to/your/data.sql` as needed)*

3.  **Configure Database Connection:**
    * Open the `src/main/java/com/eventplanner/DatabaseConnection.java` file.
    * Update the `DB_URL`, `DB_USER`, and `DB_PASSWORD` constants with your MySQL database connection details.
    ```java
    private static final String DB_URL = "jdbc:mysql://localhost:3306/event_planner?useSSL=false&serverTimezone=UTC"; // <-- Update if necessary
    private static final String DB_USER = "your_mysql_user"; // <-- Update this
    private static final String DB_PASSWORD = "your_mysql_password"; // <-- Update this
    ```

4.  **Build the Project:**
    * Navigate to the project root directory in your terminal (where `pom.xml` is located).
    * Run the Maven build command:
        ```bash
        mvn clean package
        ```
    * This will compile the code, run tests (if any), and package the application into an executable JAR file in the `target` directory.

## How to Run

1.  Ensure your MySQL server is running and the database is set up.
2.  Open a terminal or command prompt.
3.  Navigate to the project's `target` directory.
    ```bash
    cd target
    ```
4.  Run the executable JAR file:
    ```bash
    java -jar event-planner-<version>-shaded.jar
    ```
    *(Replace `<version>` with the actual version number from your `pom.xml`, e.g., `1.0-SNAPSHOT`)*

## Screenshots

*(Add screenshots of your application here! Show the Login Screen, User Dashboard (Events & Calendar tabs), Admin Dashboard (Groups & Events tabs). This significantly helps users understand the application.)*

## Future Scope

Potential enhancements for this project include:

* Implementing notification systems (in-app or email).
* Adding more granular user roles and permissions.
* Integrating messaging or announcement features within groups.
* Allowing file attachments for events or groups.
* Developing a web or mobile interface.
* Integrating with other college systems (e.g., student information).
* Further improving the UI/UX design.
* Adding reporting features for administrators.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contributing

*(Optional: If you are open to contributions, add a brief section on how others can contribute to your project.)*

## Contact

*(Optional: Add your name or contact information if you wish.)*

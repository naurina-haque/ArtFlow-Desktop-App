# ArtFlow

A comprehensive JavaFX-based desktop application that connects artists and customers in a digital art marketplace. ArtFlow enables artists to showcase and manage their artwork while customers can browse, purchase, and track custom art commissions.

## Features

### Artist Features

- **Artist Dashboard**: View statistics, manage orders, and track earnings
- **Artwork Management**: Add, edit, and showcase artwork with detailed descriptions and pricing
- **Order Management**: Track completed orders and customer interactions
- **Profile Management**: Customize artist profile with bio and portfolio information
- **Order Tracking**: Monitor real-time updates on commission orders

### Customer Features

- **Browse Artwork**: Discover art from various artists with filtering and search
- **Detailed Art Viewing**: View high-resolution artwork with artist information
- **Order Placement**: Request custom artwork with specifications
- **Order Tracking**: Monitor commission progress in real-time
- **Profile Management**: Manage customer information and order history
- **Purchase History**: View completed orders and downloads

### General Features

- **User Authentication**: Secure login and signup for both artists and customers
- **Role-Based Access**: Separate dashboards and features for different user types
- **SQLite Database**: Local persistent data storage
- **Modern UI**: Clean, intuitive interface with CSS styling

## Tech Stack

- **Language**: Java 21
- **GUI Framework**: JavaFX 21.0.6
- **Build Tool**: Maven
- **Database**: SQLite
- **Additional Libraries**:
  - ControlsFX - Advanced JavaFX controls
  - ValidatorFX - Form validation
  - Ikonli - Icon library integration
  - BootstrapFX - Bootstrap-themed styling

## Project Structure

```
ArtFlow/
├── src/
│   └── main/
│       ├── java/com/example/artflow/
│       │   ├── Controllers/
│       │   │   ├── ArtistDashboardController.java
│       │   │   ├── ArtistLoginController.java
│       │   │   ├── CustomerDashboardController.java
│       │   │   ├── CustomerLoginController.java
│       │   │   └── ... (more controllers)
│       │   ├── Models/
│       │   │   ├── User.java
│       │   │   ├── ArtworkModel.java
│       │   │   ├── OrderModel.java
│       │   │   └── CurrentUser.java
│       │   ├── Database/
│       │   │   └── DatabaseHelper.java
│       │   ├── Stores/
│       │   │   └── ArtworkStore.java
│       │   └── Splashscreen.java (Entry Point)
│       └── resources/com/example/artflow/
│           ├── *.fxml (UI layouts)
│           └── *.css (Styling)
└── pom.xml (Maven configuration)
```

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- Windows, macOS, or Linux operating system

## Installation & Setup

### 1. Clone or Download the Project

```bash
cd ArtFlow
```

### 2. Build the Project

```bash
mvn clean install
```

This command will:

- Download all dependencies
- Compile the source code
- Package the application

### 3. Run the Application

```bash
mvn javafx:run
```

Alternatively, you can run using the Maven wrapper:

```bash
./mvnw javafx:run  # Linux/macOS
mvnw.cmd javafx:run  # Windows
```

## Database

The application uses SQLite for data persistence. The database file (`artflow.db`) is automatically created on first run with the following key tables:

- **users**: Stores user credentials and profile information
- **artwork**: Contains artwork details (title, description, price, artist ID)
- **orders**: Tracks customer orders and commission status

## User Workflows

### Artist Workflow

1. Sign up or login with artist credentials
2. Access artist dashboard
3. Add new artwork with images, titles, descriptions, and pricing
4. Monitor completed orders
5. Manage profile and portfolio

### Customer Workflow

1. Sign up or login with customer credentials
2. Browse available artwork
3. View detailed artwork information
4. Place orders for custom artwork
5. Track order progress
6. Manage profile and order history

## Default Test 



## Key Classes

| Class            | Purpose                                   |
| ---------------- | ----------------------------------------- |
| `Splashscreen`   | Application entry point                   |
| `DatabaseHelper` | Manages all database operations           |
| `User`           | User model for both artists and customers |
| `ArtworkModel`   | Represents artwork objects                |
| `OrderModel`     | Represents order/commission objects       |
| `CurrentUser`    | Tracks the currently logged-in user       |
| `ArtworkStore`   | Manages artwork collection in memory      |

## Configuration

All configuration is handled via the `DatabaseHelper` class. To modify database behavior, edit the relevant methods in `DatabaseHelper.java`.

## Development

### Building with IDE

- Open the project in IntelliJ IDEA or Eclipse
- Ensure the project SDK is set to Java 21
- Right-click on `pom.xml` and select "Maven > Reload Project"

### Running with Maven

```bash
mvn clean javafx:run
```

### Packaging for Distribution

```bash
mvn clean package
```

## Troubleshooting

| Issue                   | Solution                                                   |
| ----------------------- | ---------------------------------------------------------- |
| "No module found" error | Run `mvn clean install` to download dependencies           |
| Database locked error   | Close all instances of the application                     |
| JavaFX not loading      | Verify Java 21 is installed and JAVA_HOME is set correctly |
| FXML files not found    | Ensure `src/main/resources` is in the classpath            |

## License

This project is provided as-is for educational and commercial purposes.

## Support

For issues or feature requests, please review the code structure and refer to the inline documentation in each controller and model class.

---

**Last Updated**: January 2026
**Version**: 1.0-SNAPSHOT

# Build Instructions

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## Build Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/DokaiiMob/AzureAuth.git
   cd AzureAuth
   ```

2. **Build the project:**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Find the compiled JAR:**
   ```
   target/AzureAuth.jar
   ```

## Installation

1. Copy `target/AzureAuth.jar` to your server's `plugins` folder
2. Restart your server
3. Configure the plugin in `plugins/AzureAuth/config.yml`

## Dependencies

The following dependencies are automatically included via Maven Shade Plugin:
- SQLite JDBC 3.44.1.0
- SLF4J API 1.7.36

## Development

To set up for development:

1. Import the project into your IDE as a Maven project
2. Ensure Java 17 is configured
3. Maven will automatically download dependencies

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

Built with вќ¤пёЏ for AzureMyst server

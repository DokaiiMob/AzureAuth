# 🔐 AzureAuth

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-Plugin-brightgreen?style=for-the-badge&logo=minecraft" alt="Minecraft Plugin">
  <img src="https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java" alt="Java 17+">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="MIT License">
  <img src="https://img.shields.io/github/v/release/DokaiiMob/AzureAuth?style=for-the-badge" alt="Release">
</p>

<p align="center">
  <strong>Secure authentication plugin for Minecraft servers</strong><br>
  Developed specifically for AzureMyst server
</p>

---

## ✨ Features

- 🔒 **Secure Authentication** - Advanced player authentication system
- 💾 **SQLite Database** - Lightweight and efficient data storage
- ⚙️ **Configurable** - Flexible configuration options via `config.yml`
- 🏗️ **Modular Architecture** - Clean separation with managers and database layer
- 🛡️ **Security Focused** - Built with security best practices in mind
- 🎯 **AzureMyst Optimized** - Specifically designed for AzureMyst server requirements

## 📋 Prerequisites

- ☕ **Java 17** or higher
- 🔧 **Maven 3.6+** (for building)
- 🎮 **Minecraft Server** (Bukkit/Spigot/Paper compatible)

## 🚀 Quick Start

### Installation

1. **Download the latest release** from [Releases](https://github.com/DokaiiMob/AzureAuth/releases)
2. **Copy** `AzureAuth.jar` to your server's `plugins` folder
3. **Restart** your server
4. **Configure** the plugin in `plugins/AzureAuth/config.yml`

### Configuration

The plugin will generate a `config.yml` file on first run. Customize it according to your server needs.

## 🛠️ Building from Source

### Step 1: Clone the Repository
```bash
git clone https://github.com/DokaiiMob/AzureAuth.git
cd AzureAuth
```

### Step 2: Build the Project
```bash
mvn clean package -DskipTests
```

### Step 3: Find the JAR
The compiled plugin will be available at:
```
target/AzureAuth.jar
```

For detailed build instructions, see [BUILD.md](BUILD.md).

## 📁 Project Structure

```
src/
├── main/
│   ├── java/net/azuremyst/auth/
│   │   ├── database/          # Database operations
│   │   ├── managers/          # Core authentication managers
│   │   └── AzureAuth.java     # Main plugin class
│   └── resources/
│       ├── config.yml         # Plugin configuration
│       └── plugin.yml         # Plugin metadata
└── ...
```

## ⚙️ Architecture

### Core Components

- **AzureAuth.java** - Main plugin class and entry point
- **DatabaseManager** - Handles all database operations and connections
- **AuthManager** - Core authentication logic and player management
- **Configuration System** - Flexible YAML-based configuration

### Dependencies

- **SQLite JDBC 3.44.1.0** - Database connectivity
- **SLF4J API 1.7.36** - Logging framework

## 🔧 Development

### Setting up Development Environment

1. **Import** the project into your IDE as a Maven project
2. **Configure** Java 17 in your IDE
3. **Let Maven** automatically download dependencies
4. **Start coding!**

### Contributing Guidelines

1. 🍴 **Fork** the repository
2. 🌿 **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. ✨ **Make** your changes
4. 🧪 **Test** thoroughly
5. 📝 **Commit** your changes (`git commit -m 'Add amazing feature'`)
6. 📤 **Push** to the branch (`git push origin feature/amazing-feature`)
7. 🔄 **Submit** a pull request

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 🏷️ Versioning

We use [Semantic Versioning](http://semver.org/) for versioning. For available versions, see the [releases on this repository](https://github.com/DokaiiMob/AzureAuth/releases).

## 👥 Authors

- **DokaiiMob** - *Initial work* - [@DokaiiMob](https://github.com/DokaiiMob)

## 🙏 Acknowledgments

- Built with ❤️ for the **AzureMyst** server community
- Thanks to all contributors who help improve this project
- Special thanks to the Minecraft plugin development community

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/DokaiiMob/AzureAuth/issues) page
2. Create a new issue if your problem isn't already reported
3. Provide detailed information about your setup and the issue

---

<p align="center">
  <strong>🎮 Happy Gaming with Secure Authentication! 🔐</strong>
</p>

# TeleRTX Installation Guide

Complete installation guide from scratch for Linux, macOS, and Windows.

## Table of Contents
- [Prerequisites Installation](#prerequisites-installation)
  - [Linux (Ubuntu/Debian)](#linux-ubuntudebian)
  - [Linux (Fedora/RHEL/CentOS)](#linux-fedorарhelcentos)
  - [macOS](#macos)
  - [Windows](#windows)
- [TeleRTX Installation](#telertx-installation)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites Installation

TeleRTX requires:
- **Java Development Kit (JDK) 11 or higher**
- **Apache Maven 3.6 or higher**
- **Git** (for cloning the repository)

### Linux (Ubuntu/Debian)

#### 1. Update Package Manager
```bash
sudo apt update
sudo apt upgrade -y
```

#### 2. Install Java 11 (OpenJDK)
```bash
# Install OpenJDK 11
sudo apt install openjdk-11-jdk -y

# Verify installation
java -version
javac -version
```

Expected output:
```
openjdk version "11.0.x" ...
```

#### 3. Install Maven
```bash
# Install Maven
sudo apt install maven -y

# Verify installation
mvn -version
```

Expected output:
```
Apache Maven 3.x.x
Maven home: /usr/share/maven
Java version: 11.0.x ...
```

#### 4. Install Git
```bash
sudo apt install git -y

# Verify installation
git --version
```

### Linux (Fedora/RHEL/CentOS)

#### 1. Update Package Manager
```bash
sudo dnf update -y
# or for older versions
sudo yum update -y
```

#### 2. Install Java 11
```bash
# Fedora/RHEL 8+
sudo dnf install java-11-openjdk-devel -y

# RHEL/CentOS 7
sudo yum install java-11-openjdk-devel -y

# Verify installation
java -version
```

#### 3. Install Maven
```bash
# Fedora/RHEL 8+
sudo dnf install maven -y

# RHEL/CentOS 7
sudo yum install maven -y

# Verify installation
mvn -version
```

#### 4. Install Git
```bash
sudo dnf install git -y
# or
sudo yum install git -y

git --version
```

### macOS

#### Option 1: Using Homebrew (Recommended)

##### 1. Install Homebrew (if not already installed)
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

##### 2. Install Java
```bash
# Install OpenJDK 11
brew install openjdk@11

# Link it for system Java wrappers
sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk

# Verify installation
java -version
```

##### 3. Install Maven
```bash
brew install maven

# Verify installation
mvn -version
```

##### 4. Install Git
```bash
brew install git

git --version
```

#### Option 2: Manual Installation

##### 1. Install Java
1. Download JDK 11 from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
2. Open the `.dmg` file and follow the installer
3. Verify: Open Terminal and run `java -version`

##### 2. Install Maven
1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract the archive:
   ```bash
   tar -xzf apache-maven-3.*-bin.tar.gz
   sudo mv apache-maven-3.* /opt/maven
   ```
3. Add to PATH in `~/.zshrc` or `~/.bash_profile`:
   ```bash
   export PATH=/opt/maven/bin:$PATH
   ```
4. Reload shell: `source ~/.zshrc`
5. Verify: `mvn -version`

##### 3. Install Git
Git comes pre-installed on macOS. If not:
```bash
xcode-select --install
```

### Windows

#### Option 1: Using Chocolatey (Recommended)

##### 1. Install Chocolatey
Open PowerShell as Administrator and run:
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

##### 2. Install Java, Maven, and Git
```powershell
# Install OpenJDK 11
choco install openjdk11 -y

# Install Maven
choco install maven -y

# Install Git
choco install git -y

# Refresh environment variables
refreshenv
```

##### 3. Verify Installations
Open a new Command Prompt or PowerShell:
```cmd
java -version
mvn -version
git --version
```

#### Option 2: Manual Installation

##### 1. Install Java
1. Download JDK 11 from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
2. Run the installer (`.exe` or `.msi`)
3. During installation, note the installation path (e.g., `C:\Program Files\Java\jdk-11`)
4. Add to PATH:
   - Open "Environment Variables" (Search in Windows)
   - Under "System variables", find `Path` and click "Edit"
   - Click "New" and add: `C:\Program Files\Java\jdk-11\bin`
   - Click OK to save
5. Open a new Command Prompt and verify: `java -version`

##### 2. Install Maven
1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract the `.zip` file to `C:\Program Files\Maven`
3. Add to PATH:
   - Open "Environment Variables"
   - Under "System variables", find `Path` and click "Edit"
   - Click "New" and add: `C:\Program Files\Maven\apache-maven-3.x.x\bin`
   - Click OK to save
4. Create `MAVEN_HOME` environment variable:
   - Click "New" under "System variables"
   - Variable name: `MAVEN_HOME`
   - Variable value: `C:\Program Files\Maven\apache-maven-3.x.x`
   - Click OK
5. Open a new Command Prompt and verify: `mvn -version`

##### 3. Install Git
1. Download Git from [git-scm.com](https://git-scm.com/download/win)
2. Run the installer
3. Accept default options (or customize as needed)
4. Open a new Command Prompt and verify: `git --version`

---

## TeleRTX Installation

Once you have Java, Maven, and Git installed, follow these steps:

### 1. Clone the Repository

```bash
git clone https://github.com/thertxnetwork/telertx.git
cd telertx
```

### 2. Build TeleRTX

```bash
# Clean and build the project
mvn clean package
```

This will:
- Download all dependencies (first time only)
- Compile the source code
- Run tests (if any)
- Create the executable JAR file in `target/` directory

**Note**: The first build may take several minutes as Maven downloads dependencies.

### 3. Run TeleRTX

```bash
# Run the application
java -jar target/telertx-*-jar-with-dependencies.jar
```

Or use Maven directly:
```bash
mvn exec:java -Dexec.mainClass="com.thertxnetwork.telertx.TeleRTX"
```

---

## Verification

### Check Java Installation
```bash
java -version
javac -version
```

Expected output should show version 11 or higher:
```
openjdk version "11.0.x" 2023-xx-xx
OpenJDK Runtime Environment (build 11.0.x+x)
OpenJDK 64-Bit Server VM (build 11.0.x+x, mixed mode)
```

### Check Maven Installation
```bash
mvn -version
```

Expected output:
```
Apache Maven 3.x.x
Maven home: /path/to/maven
Java version: 11.0.x, vendor: ...
```

### Check Git Installation
```bash
git --version
```

Expected output:
```
git version 2.x.x
```

### Test TeleRTX Build
```bash
cd telertx
mvn clean compile
```

If successful, you should see:
```
[INFO] BUILD SUCCESS
```

---

## Troubleshooting

### Java Issues

#### Problem: `java: command not found`
**Solution**: Java is not in your PATH. 
- **Linux/macOS**: Add to `~/.bashrc` or `~/.zshrc`:
  ```bash
  export JAVA_HOME=/path/to/java
  export PATH=$JAVA_HOME/bin:$PATH
  ```
  Then: `source ~/.bashrc`
- **Windows**: Add Java bin directory to PATH in Environment Variables

#### Problem: Wrong Java version
```bash
# Check what versions are installed
# Linux
update-alternatives --config java

# macOS with multiple JDKs
/usr/libexec/java_home -V

# Set JAVA_HOME to specific version
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
```

### Maven Issues

#### Problem: `mvn: command not found`
**Solution**: Maven is not in your PATH.
- Add Maven's `bin` directory to your PATH (see manual installation steps above)

#### Problem: Maven build fails with "JAVA_HOME not set"
**Solution**: 
```bash
# Linux/macOS
export JAVA_HOME=/path/to/java
export PATH=$JAVA_HOME/bin:$PATH

# Windows (Command Prompt)
set JAVA_HOME=C:\Program Files\Java\jdk-11
set PATH=%JAVA_HOME%\bin;%PATH%
```

#### Problem: Maven can't download dependencies
**Solution**: 
- Check internet connection
- Try deleting `~/.m2/repository` and rebuilding
- Check if behind a proxy (configure in `~/.m2/settings.xml`)

### TeleRTX Build Issues

#### Problem: "Unable to create a system terminal"
**Solution**: This is a warning, not an error. The app will work in "dumb" terminal mode. To fix:
- Make sure you're running in an actual terminal (not IDE console)
- On Windows, use Windows Terminal, Git Bash, or PowerShell

#### Problem: Build fails with compilation errors
**Solution**:
1. Ensure Java 11 or higher: `java -version`
2. Clean and rebuild: `mvn clean package`
3. Check that all files were cloned: `git status`

#### Problem: Character encoding issues (emoji not showing)
**Solution**:
- **Linux/macOS**: Ensure UTF-8 locale:
  ```bash
  export LC_ALL=en_US.UTF-8
  export LANG=en_US.UTF-8
  ```
- **Windows**: Use Windows Terminal or set console to UTF-8:
  ```cmd
  chcp 65001
  ```

### Platform-Specific Issues

#### Linux: Permission denied when running scripts
```bash
chmod +x script-name.sh
```

#### macOS: "cannot be opened because the developer cannot be verified"
```bash
xattr -d com.apple.quarantine /path/to/file
```

#### Windows: Execution policy prevents running scripts
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

## Quick Reference Card

### One-Command Installation (Linux/macOS)

#### Ubuntu/Debian
```bash
sudo apt update && sudo apt install -y openjdk-11-jdk maven git
```

#### Fedora
```bash
sudo dnf install -y java-11-openjdk-devel maven git
```

#### macOS (with Homebrew)
```bash
brew install openjdk@11 maven git
```

### Build and Run TeleRTX
```bash
git clone https://github.com/thertxnetwork/telertx.git
cd telertx
mvn clean package
java -jar target/telertx-*-jar-with-dependencies.jar
```

---

## Next Steps

After successful installation:
1. Read [QUICKSTART.md](QUICKSTART.md) for usage guide
2. Read [README.md](README.md) for feature documentation
3. Read [ARCHITECTURE.md](ARCHITECTURE.md) for technical details

## Support

If you encounter issues not covered here:
- Check [README.md](README.md) for additional troubleshooting
- Open an issue at https://github.com/thertxnetwork/telertx/issues
- Include your OS, Java version, Maven version, and error message

---

**Success?** You should now be able to run TeleRTX! 🎉

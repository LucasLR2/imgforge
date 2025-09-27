# ImgForge

## Overview
**ImgForge** is a Java-based application designed to batch convert images into common formats such as `.png` and `.jpg`.
- **Start date:** September 25, 2025
- **Current version:** v1.0 (Phase 1 completed - September 26, 2025)
- **Status:** Functional CLI application with PNG/JPG conversion

---

## Features

### ✅ Implemented (Phase 1)
- **CLI interface** with comprehensive options
- **Batch conversion** of PNG ↔ JPG formats
- **Quality control** for JPEG output (0.0 - 1.0)
- **Recursive folder scanning** for images
- **Automatic output folder creation**
- **Detailed logging and statistics**
- **Dry-run mode** for previewing operations
- **Verbose output** with file size information

### 🚧 Planned (Future phases)
- **WebP format support** (dependency integration pending)
- Additional formats (BMP, GIF, TIFF)
- Image resizing and compression options
- Graphical user interface (JavaFX)
- Automatic file renaming
- Metadata preservation

---

## Usage

### Quick Start
```bash
# Build the application
mvn clean package

# Convert PNG to JPG
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./input -o ./output -f jpg

# Convert with custom quality
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./input -o ./output -f jpg -q 0.9

# Verbose output with recursive scanning
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./input -o ./output -f png -r -v
```

### Command Line Options

| Option | Long Form | Description |
|--------|-----------|-------------|
| `-i` | `--input` | Input folder containing images (required) |
| `-o` | `--output` | Output folder for converted images (required) |
| `-f` | `--format` | Output format: `png`, `jpg`, `jpeg` (required) |
| `-q` | `--quality` | JPEG quality 0.0-1.0 (default: 0.85) |
| `-r` | `--recursive` | Scan subfolders recursively |
| `-v` | `--verbose` | Show detailed information |
| | `--overwrite` | Overwrite existing files |
| | `--dry-run` | Preview operations without executing |
| `-h` | `--help` | Show help message |
| `-V` | `--version` | Show version information |

### Examples
```bash
# Preview conversion without executing
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f png --dry-run

# High quality JPEG conversion with verbose output
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f jpg -q 0.95 -v

# Recursive conversion with overwrite
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f png -r --overwrite
```

---

## Technical Details

### Project Structure
```
ImgForge/
├── src/main/java/com/example/imgforge/
│   ├── App.java              # Main application entry point
│   ├── CLIHandler.java       # Command line interface
│   ├── ImageConverter.java   # Core conversion logic
│   └── FileUtils.java        # File handling utilities
├── src/test/java/com/example/imgforge/
│   └── AppTest.java          # Unit tests
├── pom.xml                   # Maven configuration
└── README.md
```

### Technologies
- **Java 21** (minimum Java 17+)
- **Maven** for dependency management and building
- **[Picocli 4.7.5](https://picocli.info/)** for CLI framework
- **[TwelveMonkeys ImageIO 3.10.1](https://github.com/haraldk/TwelveMonkeys)** for extended image format support
- **[JUnit 5](https://junit.org/junit5/)** for testing

### Supported Formats
- **Input**: PNG, JPG, JPEG, BMP, GIF
- **Output**: PNG, JPG, JPEG (WebP support pending dependency resolution)

---

## Development

### Build Requirements
- Java 21 or higher
- Maven 3.6+

### Building
```bash
# Clone the repository
git clone <repository-url>
cd imgforge

# Build and test
mvn clean compile test

# Create executable JAR
mvn package
```

### Testing
```bash
# Run unit tests
mvn test

# Create test images and try conversion
mkdir -p test-images/input test-images/output
wget -O test-images/input/sample.jpg "https://picsum.photos/400/300"
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i test-images/input -o test-images/output -f png -v
```

---

## Roadmap

### Phase 1: Core Functionality ✅ COMPLETED
- Maven setup with dependencies
- Basic class structure (ImageConverter, FileUtils, CLIHandler)
- PNG ↔ JPG conversion functionality
- Complete CLI interface

### Phase 2: Extended Formats 🚧 PLANNED
- WebP format integration
- Additional input format support
- Advanced compression options

### Phase 3: Advanced CLI Features 🚧 PLANNED
- Configuration files
- Batch processing improvements
- Performance optimizations

### Phase 4: Graphical Interface 🚧 PLANNED
- JavaFX-based GUI
- Drag-and-drop functionality
- Visual preview and settings

---

## License
This project is currently in development phase.
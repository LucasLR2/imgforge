# ImgForge

## Overview
**ImgForge** is a Java-based application designed to batch convert images into common formats such as `.png`, `.jpg`, `.bmp`, `.gif`, `.tiff`, and more.
- **Start date:** September 25, 2025
- **Current version:** v1.1 (Phase 2 completed - September 28, 2025)
- **Status:** Enhanced CLI application with extended format support

---

## Features

### ✅ Implemented (Phase 2 - Extended Formats)
- **Extended format support** for input and output
    - Input: PNG, JPG, JPEG, BMP, GIF, TIFF, TIF, WBMP
    - Output: PNG, JPG, JPEG, BMP, GIF, TIFF, WBMP
- **Format-specific optimizations**
    - Automatic transparency handling
    - Color space conversions (RGB, indexed, grayscale)
    - Format-specific compression and encoding
- **Advanced CLI options**
    - Format filtering (`--filter-format`)
    - Directory structure preservation (`--preserve-structure`)
    - Detailed image information (`--info`)
    - Format statistics (`--stats`)
    - Supported formats listing (`--list-formats`)
- **Enhanced file processing**
    - Recursive directory preservation
    - Detailed image metadata analysis
    - Format distribution statistics
    - Batch conversion with progress tracking

### ✅ Implemented (Phase 1)
- CLI interface with comprehensive options
- Batch conversion of PNG ↔ JPG formats
- Quality control for JPEG output (0.0 - 1.0)
- Recursive folder scanning
- Automatic output folder creation
- Detailed logging and statistics
- Dry-run mode for previewing operations
- Verbose output with file size info

### 🚧 Planned (Future Phases)
- WebP format support (Phase 3)
- Image resizing and compression options
- Graphical user interface (JavaFX)
- Automatic file renaming
- Advanced metadata preservation
- Batch watermarking
- Image optimization algorithms

---

## Usage

### Quick Start
```bash
# Build the application
mvn clean package

# Convert PNG to JPG
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./input -o ./output -f jpg

# Convert to TIFF with high quality
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./input -o ./output -f tiff -q 0.95

# List supported formats
java -jar target/imgforge-1.0.0-SNAPSHOT.jar --list-formats

# Show detailed info and statistics
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./input -o ./output -f png --info --stats
```

### Command Line Options
| Short | Long | Description |
|-------|------|-------------|
| `-i` | `--input` | Input folder containing images |
| `-o` | `--output` | Output folder for converted images |
| `-f` | `--format` | Output format: png, jpg, jpeg, bmp, gif, tiff, wbmp |
| `-q` | `--quality` | JPEG/TIFF quality 0.0–1.0 (default: 0.85) |
| `-r` | `--recursive` | Scan subfolders recursively |
| `-v` | `--verbose` | Show detailed information |
| | `--overwrite` | Overwrite existing files |
| | `--dry-run` | Preview operations without executing |
| | `--preserve-structure` | Maintain directory structure (with `--recursive`) |
| | `--info` | Show detailed image information |
| | `--stats` | Show format distribution statistics |
| | `--list-formats` | List all supported formats |
| | `--filter-format` | Filter input files (e.g., jpg,png) |
| | `--png-compression` | PNG compression level 0–9 (default: 6) |
| | `--preserve-quality` | Ignore quality setting, keep original quality |
| `-h` | `--help` | Show help message |
| `-V` | `--version` | Show version info |

---

## Technical Details

### Project Structure
```
ImgForge/
├── src/main/java/com/example/imgforge/
│   ├── App.java            # Main entry point
│   ├── CLIHandler.java     # Command line interface
│   ├── ImageConverter.java # Conversion logic
│   └── FileUtils.java      # File utilities and stats
├── src/test/java/com/example/imgforge/
│   └── AppTest.java        # Unit tests
├── test-images/            # Sample test images
├── pom.xml                 # Maven configuration
└── README.md
```

### Technologies
- **Java 21** (minimum Java 17+)
- **Maven** for build management
- **[Picocli](https://picocli.info/)** for CLI framework
- **[TwelveMonkeys ImageIO](https://github.com/haraldk/TwelveMonkeys)** for extended format support
- **JUnit 5** for testing

### Supported Formats
- **Input:** PNG, JPG/JPEG, BMP, GIF, TIFF/TIF, WBMP
- **Output:** PNG, JPG/JPEG, BMP, GIF, TIFF, WBMP
- **Features:**
    - Automatic transparency handling
    - Color space conversions
    - Optimized encoding per format
    - Quality preservation option

---

## Roadmap

- **Phase 1:** Core functionality ✅
- **Phase 2:** Extended formats ✅
- **Phase 3:** WebP & advanced features 🚧
- **Phase 4:** Image enhancement 🚧
- **Phase 5:** GUI (JavaFX) 🚧

---

## Version History
### v1.1.0-Phase2 (September 28, 2025)
- Extended format support: BMP, GIF, TIFF, WBMP
- Advanced CLI options (`--filter-format`, `--info`, `--stats`, etc.)
- Enhanced file processing with structure preservation
- Metadata analysis and format statistics

### v1.0.0 (September 26, 2025)
- Core PNG ↔ JPG conversion
- CLI interface with basic options
- Batch processing, recursive scan, quality control

---

## License
This project is currently in development phase.

---

## Contributing
Contributions are welcome! Current focus is **Phase 3 (WebP)** and **Phase 4 (enhancements)**.  
For testing, use the included sample images in `test-images/input/`.
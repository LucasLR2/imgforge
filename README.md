# ImgForge

## Overview
**ImgForge** is a Java-based application designed to batch convert images into common formats such as `.png`, `.jpg`, `.bmp`, `.gif`, `.tiff`, and more.
- **Start date:** September 25, 2025
- **Current version:** v1.1 (Phase 2 completed - September 27, 2025)
- **Status:** Enhanced CLI application with extended format support

---

## Features

### ✅ Implemented (Phase 2 - Extended Formats)
- **Extended format support** for input and output
    - Input: PNG, JPG, JPEG, BMP, GIF, TIFF, TIF, WBMP
    - Output: PNG, JPG, JPEG, BMP, GIF, TIFF, WBMP
- **Format-specific optimizations**
    - Automatic transparency handling
    - Color space conversions
    - Format-specific compression
- **Advanced CLI options**
    - Format filtering (`--filter-format`)
    - Directory structure preservation (`--preserve-structure`)
    - Detailed image information (`--info`)
    - Format statistics (`--stats`)
    - Supported formats listing (`--list-formats`)
- **Enhanced file processing**
    - Recursive directory structure preservation
    - Detailed image metadata analysis
    - Format distribution statistics

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
- **WebP format support** (Phase 3)
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

# List all supported formats
java -jar target/imgforge-1.0.0-SNAPSHOT.jar --list-formats

# Show detailed image information
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./input -o ./output -f png --info --stats
```

### Command Line Options

| Option | Long Form | Description |
|--------|-----------|-------------|
| `-i` | `--input` | Input folder containing images (required) |
| `-o` | `--output` | Output folder for converted images (required) |
| `-f` | `--format` | Output format: `png`, `jpg`, `jpeg`, `bmp`, `gif`, `tiff`, `wbmp` (required) |
| `-q` | `--quality` | JPEG/TIFF quality 0.0-1.0 (default: 0.85) |
| `-r` | `--recursive` | Scan subfolders recursively |
| `-v` | `--verbose` | Show detailed information |
| | `--overwrite` | Overwrite existing files |
| | `--dry-run` | Preview operations without executing |
| | `--preserve-structure` | Maintain directory structure in output (with `--recursive`) |
| | `--info` | Show detailed image information |
| | `--stats` | Show format distribution statistics |
| | `--list-formats` | List all supported formats and capabilities |
| | `--filter-format` | Filter input files by format (e.g., `jpg,png`) |
| | `--png-compression` | PNG compression level 0-9 (default: 6) |
| | `--preserve-quality` | Maintain original quality (ignore quality setting) |
| `-h` | `--help` | Show help message |
| `-V` | `--version` | Show version information |

### Examples

#### Basic Conversion
```bash
# Convert all images to PNG
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f png

# High quality JPEG conversion
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f jpg -q 0.95
```

#### Advanced Options (Phase 2)
```bash
# Preserve directory structure with recursive conversion
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f tiff -r --preserve-structure

# Filter and convert only JPEG files to PNG
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f png --filter-format jpg,jpeg

# Show detailed information and statistics before conversion
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f bmp --info --stats --dry-run

# List all supported formats and their capabilities
java -jar target/imgforge-1.0.0-SNAPSHOT.jar --list-formats
```

#### Format-Specific Examples
```bash
# Convert to GIF (automatically handles color reduction)
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f gif

# Convert to WBMP for mobile devices (grayscale)
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f wbmp

# High-quality TIFF with compression
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i ./photos -o ./converted -f tiff -q 0.9
```

---

## Technical Details

### Project Structure
```
ImgForge/
├── src/main/java/com/example/imgforge/
│   ├── App.java              # Main application entry point
│   ├── CLIHandler.java       # Enhanced command line interface (Phase 2)
│   ├── ImageConverter.java   # Extended conversion logic with format-specific handling
│   └── FileUtils.java        # Enhanced file utilities with format statistics
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

### Supported Formats (Phase 2)

#### Input Formats
- **PNG**: Portable Network Graphics (lossless, transparency)
- **JPG/JPEG**: Joint Photographic Experts Group (lossy)
- **BMP**: Windows Bitmap (uncompressed)
- **GIF**: Graphics Interchange Format (256 colors, animation support)
- **TIFF/TIF**: Tagged Image File Format (high quality, optional compression)
- **WBMP**: Wireless Bitmap (monochrome, mobile devices)

#### Output Formats
- **PNG**: Lossless compression, transparency support, configurable compression levels
- **JPG/JPEG**: Lossy compression with quality control (0.0-1.0)
- **BMP**: Uncompressed bitmap format
- **GIF**: 256-color indexed format with transparency
- **TIFF**: High-quality format with optional compression
- **WBMP**: Monochrome format for wireless devices

#### Format-Specific Features
- **Automatic transparency handling**: Formats that don't support transparency get white background
- **Color space conversions**: Automatic conversion between RGB, indexed, and grayscale
- **Format optimization**: Each format uses optimized encoding parameters
- **Quality preservation**: Option to maintain original quality where applicable

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

# Test with various formats (Phase 2)
mkdir -p test-images/{input,output}

# Download sample images in different formats
wget -O test-images/input/sample.jpg "https://picsum.photos/400/300"
wget -O test-images/input/sample.png "https://picsum.photos/300/400"

# Test extended format support
java -jar target/imgforge-1.0.0-SNAPSHOT.jar -i test-images/input -o test-images/output -f tiff --info --stats -v
```

---

## Roadmap

### Phase 1: Core Functionality ✅ COMPLETED
- Maven setup with dependencies
- Basic class structure (ImageConverter, FileUtils, CLIHandler)
- PNG ↔ JPG conversion functionality
- Complete CLI interface

### Phase 2: Extended Formats ✅ COMPLETED
- Extended input format support (BMP, GIF, TIFF, WBMP)
- Enhanced output format support
- Format-specific optimizations and handling
- Advanced CLI options (filtering, structure preservation, statistics)
- Detailed image information and metadata analysis

### Phase 3: WebP and Advanced Features 🚧 PLANNED
- WebP format integration (input and output)
- Advanced image processing options
- Performance optimizations
- Configuration file support

### Phase 4: Image Enhancement 🚧 PLANNED
- Image resizing and scaling
- Quality enhancement algorithms
- Batch watermarking
- Color profile management

### Phase 5: Graphical Interface 🚧 PLANNED
- JavaFX-based GUI
- Drag-and-drop functionality
- Visual preview and settings
- Progress visualization

---

## Version History

### v1.1.0-Phase2 (September 27, 2025)
- ✅ Extended format support: BMP, GIF, TIFF, WBMP
- ✅ Format-specific optimizations and automatic conversions
- ✅ Advanced CLI options: `--filter-format`, `--preserve-structure`, `--info`, `--stats`, `--list-formats`
- ✅ Enhanced file processing with directory structure preservation
- ✅ Detailed image metadata analysis and format statistics

### v1.0.0 (September 26, 2025)
- ✅ Core PNG ↔ JPG conversion functionality
- ✅ Complete CLI interface with comprehensive options
- ✅ Batch processing with recursive directory scanning
- ✅ Quality control and compression settings

---

## License
This project is currently in development phase.

---

## Contributing
ImgForge is actively being developed. Current focus is on Phase 3 (WebP support) and Phase 4 (image enhancement features).
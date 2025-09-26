# ImgForge

## Overview
**ImgForge** is a Java-based application designed to batch convert images into common formats such as `.png`, `.jpg`, or `.webp`.  
The project is currently under development.
- **Start date:** September 25, 2025
- **Current version:** v1 (development stage)

---

## Technical Details

### Planned Features
- Select an input folder containing images (`.jpg`, `.jpeg`, `.bmp`, `.gif`, `.png`, etc.).
- Convert all images into a chosen format (`.png`, `.jpg`, `.webp`).
- Save the converted images into an output folder.
- Optional features in future versions: resizing, compression, automatic renaming.
- Possible graphical user interface (JavaFX or Swing).

### Project Structure

```
ImgForge/
├── src/
│ ├── main/java/com/example/imgforge/App.java
│ ├── main/resources/
│ └── test/java/com/example/imgforge/AppTest.java
├── README.md
└── pom.xml (planned for Maven)
```


### Technologies
- Java 17+
- [JUnit 5](https://junit.org/junit5/) (testing framework)
- [TwelveMonkeys ImageIO](https://github.com/haraldk/TwelveMonkeys) (extended image format support)
- [webp-imageio](https://github.com/sejda-pdf/webp-imageio) (WebP support)

---

## Current Commit
- Initial project setup.
- Basic class `App.java`.
- Simple test `AppTest.java`.
- README with initial description.
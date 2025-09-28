package com.example.imgforge;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.logging.Logger;

/**
 * Manejador de interfaz de línea de comandos usando Picocli.
 * PHASE 2: Extended format support and advanced options
 */
@Command(
        name = "imgforge",
        description = "ImgForge - Conversor batch de imágenes (PHASE 2: Extended formats)",
        version = "ImgForge 1.1.0-PHASE2",
        mixinStandardHelpOptions = true
)
public class CLIHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(CLIHandler.class.getName());

    @Option(names = {"-i", "--input"},
            description = "Carpeta de entrada con imágenes")
    private String inputFolder;

    @Option(names = {"-o", "--output"},
            description = "Carpeta de salida para imágenes convertidas")
    private String outputFolder;

    @Option(names = {"-f", "--format"},
            description = "Formato de salida: png, jpg, jpeg, bmp, gif, tiff, wbmp")
    private String outputFormat;

    @Option(names = {"-q", "--quality"},
            description = "Calidad para JPG/TIFF (0.0 - 1.0, default: 0.85)",
            defaultValue = "0.85")
    private float quality;

    @Option(names = {"-r", "--recursive"},
            description = "Buscar imágenes recursivamente en subcarpetas",
            defaultValue = "false")
    private boolean recursive;

    @Option(names = {"-v", "--verbose"},
            description = "Mostrar información detallada",
            defaultValue = "false")
    private boolean verbose;

    @Option(names = {"--overwrite"},
            description = "Sobrescribir archivos existentes",
            defaultValue = "false")
    private boolean overwrite;

    @Option(names = {"--dry-run"},
            description = "Mostrar qué se haría sin ejecutar la conversión",
            defaultValue = "false")
    private boolean dryRun;

    @Option(names = {"--png-compression"},
            description = "Nivel de compresión PNG (0-9, default: 6)",
            defaultValue = "6")
    private int pngCompression;

    @Option(names = {"--preserve-quality"},
            description = "Mantener calidad original (ignora configuración de calidad)",
            defaultValue = "false")
    private boolean preserveQuality;

    // PHASE 2: Nuevas opciones
    @Option(names = {"--preserve-structure"},
            description = "Mantener estructura de directorios en salida (solo con --recursive)",
            defaultValue = "false")
    private boolean preserveStructure;

    @Option(names = {"--info"},
            description = "Mostrar información detallada de las imágenes encontradas",
            defaultValue = "false")
    private boolean showInfo;

    @Option(names = {"--list-formats"},
            description = "Listar todos los formatos soportados",
            defaultValue = "false")
    private boolean listFormats;

    @Option(names = {"--filter-format"},
            description = "Filtrar solo archivos del formato especificado (ej: jpg,png)",
            defaultValue = "")
    private String filterFormat;

    @Option(names = {"--stats"},
            description = "Mostrar estadísticas de formatos encontrados",
            defaultValue = "false")
    private boolean showStats;

    // Instancias de nuestras clases utilitarias
    private ImageConverter converter;
    private int totalFiles = 0;
    private int processedFiles = 0;
    private int errorFiles = 0;

    public CLIHandler() {
        this.converter = new ImageConverter();
    }

    @Override
    public void run() {
        logger.info("Iniciando ImgForge Phase 2...");

        // PHASE 2: Manejar opciones especiales primero
        if (listFormats) {
            showSupportedFormats();
            return;
        }

        // Validar entrada
        if (!validateInput()) {
            return;
        }

        // Buscar archivos de imagen
        List<String> imageFiles = FileUtils.findImageFiles(inputFolder, recursive);

        // PHASE 2: Aplicar filtro de formato si se especifica
        if (!filterFormat.isEmpty()) {
            imageFiles = filterFilesByFormat(imageFiles);
        }

        if (imageFiles.isEmpty()) {
            System.out.println("❌ No se encontraron archivos de imagen en: " + inputFolder);
            if (!filterFormat.isEmpty()) {
                System.out.println("   (con filtro de formato: " + filterFormat + ")");
            }
            return;
        }

        totalFiles = imageFiles.size();
        System.out.printf("📁 Encontrados %d archivos de imagen%n", totalFiles);

        // PHASE 2: Mostrar estadísticas si se solicita
        if (showStats) {
            System.out.println();
            System.out.println("📊 " + FileUtils.getFormatStatistics(imageFiles));
        }

        // PHASE 2: Mostrar información detallada si se solicita
        if (showInfo) {
            showDetailedInfo(imageFiles);
        }

        if (verbose) {
            System.out.println("📋 Configuración:");
            System.out.println("   Entrada: " + inputFolder);
            System.out.println("   Salida: " + outputFolder);
            System.out.println("   Formato: " + outputFormat.toUpperCase());
            System.out.println("   Calidad: " + (quality * 100) + "%");
            System.out.println("   Recursivo: " + (recursive ? "Sí" : "No"));
            System.out.println("   Mantener estructura: " + (preserveStructure ? "Sí" : "No"));
            System.out.println("   Sobrescribir: " + (overwrite ? "Sí" : "No"));
            System.out.printf("   Compresión PNG: %d%n", pngCompression);
            System.out.printf("   Preservar calidad: %s%n", (preserveQuality ? "Sí" : "No"));
            if (!filterFormat.isEmpty()) {
                System.out.println("   Filtro formato: " + filterFormat);
            }
            System.out.println();
        }

        // Crear carpeta de salida
        if (!FileUtils.ensureFolderExists(outputFolder)) {
            System.out.println("❌ No se pudo crear la carpeta de salida: " + outputFolder);
            return;
        }

        // Dry run - mostrar qué se haría
        if (dryRun) {
            performDryRun(imageFiles);
            return;
        }

        // Procesar archivos
        processFiles(imageFiles);

        // Mostrar estadísticas finales
        showFinalStats();
    }

    /**
     * PHASE 2: Muestra los formatos soportados.
     */
    private void showSupportedFormats() {
        System.out.println("🎨 Formatos soportados en ImgForge Phase 2:");
        System.out.println();
        System.out.println("📥 Formatos de ENTRADA:");
        List<String> inputFormats = FileUtils.getSupportedInputFormats();
        for (int i = 0; i < inputFormats.size(); i++) {
            if (i % 5 == 0 && i > 0) System.out.println();
            System.out.printf("   %-6s", inputFormats.get(i).toUpperCase());
        }
        System.out.println();
        System.out.println();

        System.out.println("📤 Formatos de SALIDA:");
        List<String> outputFormats = FileUtils.getSupportedOutputFormats();
        for (int i = 0; i < outputFormats.size(); i++) {
            if (i % 5 == 0 && i > 0) System.out.println();
            System.out.printf("   %-6s", outputFormats.get(i).toUpperCase());
        }
        System.out.println();
        System.out.println();

        System.out.println("💡 Características por formato:");
        System.out.println("   PNG:  Sin pérdida, transparencia ✓");
        System.out.println("   JPG:  Con pérdida, calidad ajustable");
        System.out.println("   BMP:  Sin pérdida, archivos grandes");
        System.out.println("   GIF:  256 colores, transparencia ✓, animación");
        System.out.println("   TIFF: Alta calidad, compresión opcional");
        System.out.println("   WBMP: Monocromático, para dispositivos móviles");
    }

    /**
     * PHASE 2: Filtra archivos por formato especificado.
     */
    private List<String> filterFilesByFormat(List<String> imageFiles) {
        String[] formats = filterFormat.toLowerCase().split(",");
        return imageFiles.stream()
                .filter(path -> {
                    String ext = FileUtils.getFileExtension(new java.io.File(path).getName()).toLowerCase();
                    for (String format : formats) {
                        if (ext.equals(format.trim())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * PHASE 2: Muestra información detallada de las imágenes.
     */
    private void showDetailedInfo(List<String> imageFiles) {
        System.out.println();
        System.out.println("📋 Información detallada de imágenes:");
        System.out.println();

        int count = Math.min(5, imageFiles.size()); // Mostrar máximo 5 para no saturar
        for (int i = 0; i < count; i++) {
            String imagePath = imageFiles.get(i);
            System.out.println("📸 " + new java.io.File(imagePath).getName());
            String info = converter.getDetailedImageInfo(imagePath);
            String[] lines = info.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    System.out.println("   " + line);
                }
            }
            System.out.println();
        }

        if (imageFiles.size() > 5) {
            System.out.printf("   ... y %d archivos más%n", imageFiles.size() - 5);
            System.out.println();
        }
    }

    /**
     * Valida los parámetros de entrada.
     */
    private boolean validateInput() {
        // Si solo se pide listar formatos, no validar otros parámetros
        if (listFormats) {
            return true;
        }

        // Validar que los parámetros requeridos estén presentes
        if (inputFolder == null || inputFolder.trim().isEmpty()) {
            System.out.println("❌ Carpeta de entrada es requerida. Use -i o --input");
            return false;
        }

        if (outputFolder == null || outputFolder.trim().isEmpty()) {
            System.out.println("❌ Carpeta de salida es requerida. Use -o o --output");
            return false;
        }

        if (outputFormat == null || outputFormat.trim().isEmpty()) {
            System.out.println("❌ Formato de salida es requerido. Use -f o --format");
            return false;
        }

        // Validar carpeta de entrada
        if (!FileUtils.isValidFolder(inputFolder)) {
            System.out.println("❌ La carpeta de entrada no existe o no es accesible: " + inputFolder);
            return false;
        }

        // PHASE 2: Validar formato de salida con lista extendida
        if (!FileUtils.isOutputFormatSupported(outputFormat)) {
            System.out.println("❌ Formato no soportado: " + outputFormat);
            System.out.println("   Formatos soportados: " +
                    String.join(", ", FileUtils.getSupportedOutputFormats()));
            System.out.println("   Usa --list-formats para ver detalles");
            return false;
        }

        // Validar calidad
        if (quality < 0.0f || quality > 1.0f) {
            System.out.println("❌ La calidad debe estar entre 0.0 y 1.0. Valor actual: " + quality);
            return false;
        }

        // Validar compresión PNG
        if (pngCompression < 0 || pngCompression > 9) {
            System.out.println("❌ La compresión PNG debe estar entre 0 y 9. Valor actual: " + pngCompression);
            return false;
        }

        // PHASE 2: Validar que preserve-structure solo se use con recursive
        if (preserveStructure && !recursive) {
            System.out.println("⚠️  --preserve-structure solo tiene efecto con --recursive");
        }

        return true;
    }

    /**
     * Muestra qué archivos se procesarían sin ejecutar la conversión.
     */
    private void performDryRun(List<String> imageFiles) {
        System.out.println("🔍 Modo dry-run - Archivos que se procesarían:");
        System.out.println();

        for (String inputPath : imageFiles) {
            String outputPath;

            // PHASE 2: Usar estructura preservada si se solicita
            if (preserveStructure && recursive) {
                outputPath = FileUtils.generateOutputPathWithStructure(
                        inputPath, inputFolder, outputFolder, outputFormat);
            } else {
                outputPath = FileUtils.generateOutputPath(inputPath, outputFolder, outputFormat);
            }

            System.out.printf("   %s -> %s%n", inputPath, outputPath);
        }

        System.out.println();
        System.out.printf("📊 Total: %d archivos serían procesados%n", imageFiles.size());
    }

    /**
     * Procesa todos los archivos de imagen.
     */
    private void processFiles(List<String> imageFiles) {
        System.out.println("🚀 Iniciando conversión...");
        System.out.println();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < imageFiles.size(); i++) {
            String inputPath = imageFiles.get(i);
            processFile(inputPath, i + 1);
        }

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        System.out.println();
        System.out.printf("⏱️  Tiempo total: %.2f segundos%n", durationMs / 1000.0);
    }

    /**
     * Procesa un archivo individual.
     */
    private void processFile(String inputPath, int fileNumber) {
        String outputPath;

        // PHASE 2: Usar estructura preservada si se solicita
        if (preserveStructure && recursive) {
            outputPath = FileUtils.generateOutputPathWithStructure(
                    inputPath, inputFolder, outputFolder, outputFormat);
        } else {
            outputPath = FileUtils.generateOutputPath(inputPath, outputFolder, outputFormat);
        }

        // Verificar si ya existe y no se debe sobrescribir
        if (!overwrite && new java.io.File(outputPath).exists()) {
            System.out.printf("[%d/%d] ⏭️  Omitido (ya existe): %s%n",
                    fileNumber, totalFiles, new java.io.File(inputPath).getName());
            return;
        }

        // Mostrar progreso
        if (verbose) {
            System.out.printf("[%d/%d] 🔄 Convirtiendo: %s%n",
                    fileNumber, totalFiles, new java.io.File(inputPath).getName());
        } else {
            System.out.printf("[%d/%d] 🔄 %s%n",
                    fileNumber, totalFiles, new java.io.File(inputPath).getName());
        }

        // Realizar conversión
        boolean success = converter.convertImage(inputPath, outputPath, outputFormat,
                quality, pngCompression, preserveQuality);

        if (success) {
            processedFiles++;
            if (verbose) {
                long inputSize = FileUtils.getFileSize(inputPath);
                long outputSize = FileUtils.getFileSize(outputPath);
                System.out.printf("   ✅ %s -> %s%n",
                        FileUtils.formatFileSize(inputSize),
                        FileUtils.formatFileSize(outputSize));
            }
        } else {
            errorFiles++;
            System.out.printf("   ❌ Error al procesar: %s%n", new java.io.File(inputPath).getName());
        }
    }

    /**
     * Muestra estadísticas finales del procesamiento.
     */
    private void showFinalStats() {
        System.out.println();
        System.out.println("📊 Estadísticas finales:");
        System.out.printf("   Total de archivos: %d%n", totalFiles);
        System.out.printf("   Procesados exitosamente: %d%n", processedFiles);
        System.out.printf("   Errores: %d%n", errorFiles);

        if (processedFiles > 0) {
            double successRate = (processedFiles * 100.0) / totalFiles;
            System.out.printf("   Tasa de éxito: %.1f%%%n", successRate);

            // PHASE 2: Mostrar formato de salida
            System.out.printf("   Formato de salida: %s%n", outputFormat.toUpperCase());
        }

        if (errorFiles > 0) {
            System.out.println("⚠️  Algunos archivos no pudieron ser procesados. Revisa los logs para más detalles.");
        } else if (processedFiles > 0) {
            System.out.println("🎉 ¡Conversión completada exitosamente!");
        }
    }

    /**
     * Método principal para ejecutar el CLI.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CLIHandler()).execute(args);
        System.exit(exitCode);
    }

    // Getters para testing
    public String getInputFolder() { return inputFolder; }
    public String getOutputFolder() { return outputFolder; }
    public String getOutputFormat() { return outputFormat; }
    public float getQuality() { return quality; }
    public boolean isRecursive() { return recursive; }
    public boolean isVerbose() { return verbose; }
    public boolean isOverwrite() { return overwrite; }
    public boolean isDryRun() { return dryRun; }
    public boolean isPreserveStructure() { return preserveStructure; }
    public boolean isShowInfo() { return showInfo; }
    public String getFilterFormat() { return filterFormat; }
}
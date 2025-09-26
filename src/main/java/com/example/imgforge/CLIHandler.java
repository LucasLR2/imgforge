package com.example.imgforge;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.logging.Logger;

/**
 * Manejador de interfaz de línea de comandos usando Picocli.
 * Define todas las opciones y argumentos disponibles para ImgForge.
 */
@Command(
        name = "imgforge",
        description = "ImgForge - Conversor batch de imágenes",
        version = "ImgForge 1.0.0",
        mixinStandardHelpOptions = true
)
public class CLIHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(CLIHandler.class.getName());

    @Option(names = {"-i", "--input"},
            description = "Carpeta de entrada con imágenes",
            required = true)
    private String inputFolder;

    @Option(names = {"-o", "--output"},
            description = "Carpeta de salida para imágenes convertidas",
            required = true)
    private String outputFolder;

    @Option(names = {"-f", "--format"},
            description = "Formato de salida: png, jpg, jpeg",
            required = true)
    private String outputFormat;

    @Option(names = {"-q", "--quality"},
            description = "Calidad para JPG (0.0 - 1.0, default: 0.85)",
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
        logger.info("Iniciando ImgForge...");

        // Validar entrada
        if (!validateInput()) {
            return;
        }

        // Buscar archivos de imagen
        List<String> imageFiles = FileUtils.findImageFiles(inputFolder, recursive);

        if (imageFiles.isEmpty()) {
            System.out.println("❌ No se encontraron archivos de imagen en: " + inputFolder);
            return;
        }

        totalFiles = imageFiles.size();
        System.out.printf("📁 Encontrados %d archivos de imagen%n", totalFiles);

        if (verbose) {
            System.out.println("📋 Configuración:");
            System.out.println("   Entrada: " + inputFolder);
            System.out.println("   Salida: " + outputFolder);
            System.out.println("   Formato: " + outputFormat.toUpperCase());
            System.out.println("   Calidad: " + (quality * 100) + "%");
            System.out.println("   Recursivo: " + (recursive ? "Sí" : "No"));
            System.out.println("   Sobrescribir: " + (overwrite ? "Sí" : "No"));
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
     * Valida los parámetros de entrada.
     */
    private boolean validateInput() {
        // Validar carpeta de entrada
        if (!FileUtils.isValidFolder(inputFolder)) {
            System.out.println("❌ La carpeta de entrada no existe o no es accesible: " + inputFolder);
            return false;
        }

        // Validar formato de salida
        try {
            ImageConverter.SupportedFormat.fromString(outputFormat);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Formato no soportado: " + outputFormat);
            System.out.println("   Formatos soportados: png, jpg, jpeg");
            return false;
        }

        // Validar calidad
        if (quality < 0.0f || quality > 1.0f) {
            System.out.println("❌ La calidad debe estar entre 0.0 y 1.0. Valor actual: " + quality);
            return false;
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
            String outputPath = FileUtils.generateOutputPath(inputPath, outputFolder, outputFormat);
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
        String outputPath = FileUtils.generateOutputPath(inputPath, outputFolder, outputFormat);

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
        boolean success = converter.convertImage(inputPath, outputPath, outputFormat, quality);

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
}
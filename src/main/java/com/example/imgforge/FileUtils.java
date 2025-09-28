package com.example.imgforge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilidades para manejo de archivos e imágenes.
 * Incluye búsqueda de archivos, validación de rutas y operaciones batch.
 *
 * PHASE 2: Extended format support added with advanced file handling
 */
public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    // Extensiones de imagen soportadas (PHASE 2: Extended support)
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
            // Formatos básicos
            "jpg", "jpeg", "png", "bmp", "gif",
            // Formatos extendidos (Fase 2)
            "tiff", "tif", "wbmp"
    );

    // Extensiones de salida soportadas
    private static final List<String> OUTPUT_FORMATS = Arrays.asList(
            "png", "jpg", "jpeg", "bmp", "gif", "tiff", "wbmp"
    );

    // Mapeo de formatos a descripciones
    private static final Map<String, String> FORMAT_DESCRIPTIONS = new HashMap<String, String>() {{
        put("png", "Portable Network Graphics - Sin pérdida, transparencia");
        put("jpg", "JPEG - Con pérdida, calidad ajustable");
        put("jpeg", "JPEG - Con pérdida, calidad ajustable");
        put("bmp", "Windows Bitmap - Sin compresión, archivos grandes");
        put("gif", "Graphics Interchange Format - 256 colores, transparencia, animación");
        put("tiff", "Tagged Image File Format - Alta calidad, compresión opcional");
        put("tif", "Tagged Image File Format - Alta calidad, compresión opcional");
        put("wbmp", "Wireless Bitmap - Monocromático, dispositivos móviles");
    }};

    /**
     * Busca todos los archivos de imagen en una carpeta.
     *
     * @param folderPath Ruta de la carpeta a buscar
     * @param recursive Si debe buscar recursivamente en subcarpetas
     * @return Lista de rutas de archivos de imagen encontrados
     */
    public static List<String> findImageFiles(String folderPath, boolean recursive) {
        List<String> imageFiles = new ArrayList<>();

        try {
            File folder = new File(folderPath);

            if (!folder.exists()) {
                logger.warning("La carpeta no existe: " + folderPath);
                return imageFiles;
            }

            if (!folder.isDirectory()) {
                logger.warning("La ruta no es una carpeta: " + folderPath);
                return imageFiles;
            }

            if (recursive) {
                imageFiles.addAll(findImageFilesRecursive(folder));
            } else {
                imageFiles.addAll(findImageFilesInFolder(folder));
            }

            logger.info("Encontrados " + imageFiles.size() + " archivos de imagen en: " + folderPath);

        } catch (Exception e) {
            logger.severe("Error al buscar archivos de imagen: " + e.getMessage());
        }

        return imageFiles;
    }

    /**
     * Busca archivos de imagen solo en la carpeta especificada (no recursivo).
     */
    private static List<String> findImageFilesInFolder(File folder) {
        List<String> imageFiles = new ArrayList<>();

        File[] files = folder.listFiles();
        if (files == null) {
            return imageFiles;
        }

        for (File file : files) {
            if (file.isFile() && isImageFile(file.getName())) {
                imageFiles.add(file.getAbsolutePath());
            }
        }

        return imageFiles;
    }

    /**
     * Busca archivos de imagen recursivamente en todas las subcarpetas.
     */
    private static List<String> findImageFilesRecursive(File folder) {
        List<String> imageFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(folder.getAbsolutePath()))) {
            imageFiles = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(path -> isImageFile(new File(path).getName()))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.severe("Error en búsqueda recursiva: " + e.getMessage());
        }

        return imageFiles;
    }

    /**
     * Verifica si un archivo es una imagen soportada por la extensión.
     */
    public static boolean isImageFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        String extension = getFileExtension(fileName).toLowerCase();
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    /**
     * PHASE 2: Verifica si un formato de salida es soportado.
     */
    public static boolean isOutputFormatSupported(String format) {
        if (format == null || format.isEmpty()) {
            return false;
        }
        return OUTPUT_FORMATS.contains(format.toLowerCase());
    }

    /**
     * PHASE 2: Obtiene la lista de formatos de entrada soportados.
     */
    public static List<String> getSupportedInputFormats() {
        return new ArrayList<>(SUPPORTED_EXTENSIONS);
    }

    /**
     * PHASE 2: Obtiene la lista de formatos de salida soportados.
     */
    public static List<String> getSupportedOutputFormats() {
        return new ArrayList<>(OUTPUT_FORMATS);
    }

    /**
     * PHASE 2: Obtiene la descripción de un formato.
     */
    public static String getFormatDescription(String format) {
        return FORMAT_DESCRIPTIONS.getOrDefault(format.toLowerCase(), "Formato desconocido");
    }

    /**
     * PHASE 2: Obtiene todas las descripciones de formatos.
     */
    public static Map<String, String> getAllFormatDescriptions() {
        return new HashMap<>(FORMAT_DESCRIPTIONS);
    }

    /**
     * Obtiene la extensión de un archivo.
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDot + 1);
    }

    /**
     * Genera la ruta de salida para un archivo convertido.
     *
     * @param inputPath Ruta del archivo original
     * @param outputFolder Carpeta de salida
     * @param newExtension Nueva extensión sin el punto (ej: "jpg")
     * @return Ruta completa del archivo de salida
     */
    public static String generateOutputPath(String inputPath, String outputFolder, String newExtension) {
        File inputFile = new File(inputPath);
        String fileName = removeFileExtension(inputFile.getName());
        String outputFileName = fileName + "." + newExtension.toLowerCase();

        return Paths.get(outputFolder, outputFileName).toString();
    }

    /**
     * PHASE 2: Genera ruta de salida manteniendo estructura de carpetas para conversión recursiva.
     */
    public static String generateOutputPathWithStructure(String inputPath, String inputBaseFolder,
                                                         String outputFolder, String newExtension) {
        File inputFile = new File(inputPath);
        File baseFolder = new File(inputBaseFolder);

        try {
            // Obtener ruta relativa
            String relativePath = baseFolder.toPath().relativize(inputFile.toPath()).toString();

            // Cambiar extensión
            String fileName = removeFileExtension(new File(relativePath).getName());
            String newFileName = fileName + "." + newExtension.toLowerCase();

            // Construir nueva ruta manteniendo estructura de directorios
            Path relativeDirPath = new File(relativePath).toPath().getParent();
            if (relativeDirPath != null) {
                return Paths.get(outputFolder, relativeDirPath.toString(), newFileName).toString();
            } else {
                return Paths.get(outputFolder, newFileName).toString();
            }
        } catch (Exception e) {
            logger.warning("Error al generar ruta con estructura: " + e.getMessage());
            // Fallback a método simple
            return generateOutputPath(inputPath, outputFolder, newExtension);
        }
    }

    /**
     * Remueve la extensión de un nombre de archivo.
     */
    public static String removeFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return fileName;
        }

        return fileName.substring(0, lastDot);
    }

    /**
     * Valida que una carpeta existe y es accesible.
     */
    public static boolean isValidFolder(String folderPath) {
        if (folderPath == null || folderPath.trim().isEmpty()) {
            return false;
        }

        File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory() && folder.canRead();
    }

    /**
     * Crea una carpeta si no existe.
     *
     * @param folderPath Ruta de la carpeta a crear
     * @return true si la carpeta existe o fue creada exitosamente
     */
    public static boolean ensureFolderExists(String folderPath) {
        if (folderPath == null || folderPath.trim().isEmpty()) {
            return false;
        }

        try {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (created) {
                    logger.info("Carpeta creada: " + folderPath);
                } else {
                    logger.warning("No se pudo crear la carpeta: " + folderPath);
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            logger.severe("Error al crear carpeta " + folderPath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * PHASE 2: Crea la estructura de directorios completa para una ruta de archivo.
     */
    public static boolean ensureDirectoryStructure(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    logger.info("Estructura de directorios creada: " + parentDir.getAbsolutePath());
                }
                return created;
            }

            return true;
        } catch (Exception e) {
            logger.severe("Error al crear estructura de directorios para " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el tamaño de un archivo en bytes.
     */
    public static long getFileSize(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                return file.length();
            }
        } catch (Exception e) {
            logger.warning("Error al obtener tamaño de archivo " + filePath + ": " + e.getMessage());
        }
        return -1;
    }

    /**
     * Formatea el tamaño de archivo en una cadena legible.
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 0) {
            return "Desconocido";
        }

        if (bytes == 0) {
            return "0 B";
        }

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double size = bytes;
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        // Formatear con diferente precisión según la unidad
        if (unitIndex == 0) {
            return String.format("%d %s", (long)size, units[unitIndex]);
        } else if (size < 10) {
            return String.format("%.2f %s", size, units[unitIndex]);
        } else if (size < 100) {
            return String.format("%.1f %s", size, units[unitIndex]);
        } else {
            return String.format("%.0f %s", size, units[unitIndex]);
        }
    }

    /**
     * Cuenta el número de archivos de imagen en una carpeta.
     */
    public static int countImageFiles(String folderPath, boolean recursive) {
        return findImageFiles(folderPath, recursive).size();
    }

    /**
     * PHASE 2: Obtiene estadísticas de archivos por formato.
     */
    public static String getFormatStatistics(List<String> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return "No hay archivos para analizar";
        }

        // Contar por formato
        Map<String, Integer> formatCount = new HashMap<>();
        long totalSize = 0;
        Map<String, Long> formatSizes = new HashMap<>();

        for (String filePath : imageFiles) {
            String extension = getFileExtension(new File(filePath).getName()).toLowerCase();
            formatCount.put(extension, formatCount.getOrDefault(extension, 0) + 1);

            long fileSize = getFileSize(filePath);
            if (fileSize > 0) {
                totalSize += fileSize;
                formatSizes.put(extension, formatSizes.getOrDefault(extension, 0L) + fileSize);
            }
        }

        // Construir string de estadísticas
        StringBuilder stats = new StringBuilder();
        stats.append("Distribución por formato:\n");

        formatCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> {
                    String format = entry.getKey();
                    int count = entry.getValue();
                    long size = formatSizes.getOrDefault(format, 0L);
                    double percentage = (count * 100.0) / imageFiles.size();

                    stats.append(String.format("   %-6s: %3d archivos (%.1f%%) - %s\n",
                            format.toUpperCase(), count, percentage, formatFileSize(size)));
                });

        stats.append(String.format("\nTamaño total: %s en %d archivos",
                formatFileSize(totalSize), imageFiles.size()));

        return stats.toString();
    }

    /**
     * PHASE 2: Obtiene estadísticas detalladas de archivos por formato.
     */
    public static Map<String, FormatStats> getDetailedFormatStatistics(List<String> imageFiles) {
        Map<String, FormatStats> detailedStats = new HashMap<>();

        if (imageFiles == null || imageFiles.isEmpty()) {
            return detailedStats;
        }

        for (String filePath : imageFiles) {
            String extension = getFileExtension(new File(filePath).getName()).toLowerCase();
            long fileSize = getFileSize(filePath);

            FormatStats stats = detailedStats.computeIfAbsent(extension, k -> new FormatStats(k));
            stats.addFile(fileSize);
        }

        return detailedStats;
    }

    /**
     * PHASE 2: Clase para almacenar estadísticas detalladas de formato.
     */
    public static class FormatStats {
        private final String format;
        private int fileCount;
        private long totalSize;
        private long minSize = Long.MAX_VALUE;
        private long maxSize = 0;

        public FormatStats(String format) {
            this.format = format;
        }

        public void addFile(long size) {
            fileCount++;
            if (size > 0) {
                totalSize += size;
                minSize = Math.min(minSize, size);
                maxSize = Math.max(maxSize, size);
            }
        }

        public String getFormat() { return format; }
        public int getFileCount() { return fileCount; }
        public long getTotalSize() { return totalSize; }
        public long getMinSize() { return minSize == Long.MAX_VALUE ? 0 : minSize; }
        public long getMaxSize() { return maxSize; }
        public double getAverageSize() {
            return fileCount > 0 ? (double) totalSize / fileCount : 0;
        }

        @Override
        public String toString() {
            return String.format("%s: %d archivos, Total: %s, Promedio: %s, Rango: %s - %s",
                    format.toUpperCase(),
                    fileCount,
                    formatFileSize(totalSize),
                    formatFileSize((long) getAverageSize()),
                    formatFileSize(getMinSize()),
                    formatFileSize(getMaxSize()));
        }
    }

    /**
     * PHASE 2: Filtra archivos por formato específico.
     */
    public static List<String> filterFilesByFormat(List<String> imageFiles, String formats) {
        if (imageFiles == null || imageFiles.isEmpty() || formats == null || formats.trim().isEmpty()) {
            return new ArrayList<>(imageFiles);
        }

        String[] formatArray = formats.toLowerCase().split(",");
        return imageFiles.stream()
                .filter(path -> {
                    String ext = getFileExtension(new File(path).getName()).toLowerCase();
                    for (String format : formatArray) {
                        if (ext.equals(format.trim())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * PHASE 2: Verifica si un archivo existe y puede ser sobrescrito.
     */
    public static boolean canOverwriteFile(String filePath, boolean overwriteEnabled) {
        File file = new File(filePath);

        if (!file.exists()) {
            return true; // No existe, se puede crear
        }

        if (overwriteEnabled) {
            return file.canWrite(); // Existe pero se permite sobrescribir
        }

        return false; // Existe y no se permite sobrescribir
    }

    /**
     * PHASE 2: Obtiene información básica de un archivo.
     */
    public static String getFileInfo(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "El archivo no existe";
            }

            StringBuilder info = new StringBuilder();
            info.append("Archivo: ").append(file.getName()).append("\n");
            info.append("Ruta: ").append(file.getAbsolutePath()).append("\n");
            info.append("Tamaño: ").append(formatFileSize(file.length())).append("\n");
            info.append("Formato: ").append(getFileExtension(file.getName()).toUpperCase()).append("\n");
            info.append("Modificado: ").append(new java.util.Date(file.lastModified())).append("\n");
            info.append("Lectura: ").append(file.canRead() ? "Sí" : "No").append("\n");
            info.append("Escritura: ").append(file.canWrite() ? "Sí" : "No").append("\n");

            return info.toString();

        } catch (Exception e) {
            return "Error al obtener información del archivo: " + e.getMessage();
        }
    }

    /**
     * PHASE 2: Validación completa de parámetros de conversión.
     */
    public static ValidationResult validateConversionParams(String inputFolder, String outputFolder,
                                                            String outputFormat, boolean recursive) {
        ValidationResult result = new ValidationResult();

        // Validar carpeta de entrada
        if (!isValidFolder(inputFolder)) {
            result.addError("Carpeta de entrada inválida: " + inputFolder);
        }

        // Validar formato de salida
        if (!isOutputFormatSupported(outputFormat)) {
            result.addError("Formato de salida no soportado: " + outputFormat);
        }

        // Contar archivos disponibles
        List<String> files = findImageFiles(inputFolder, recursive);
        result.setFileCount(files.size());

        if (files.isEmpty()) {
            result.addWarning("No se encontraron archivos de imagen en: " + inputFolder);
        }

        // Validar que se puede crear carpeta de salida
        try {
            if (!ensureFolderExists(outputFolder)) {
                result.addError("No se puede crear carpeta de salida: " + outputFolder);
            }
        } catch (Exception e) {
            result.addError("Error validando carpeta de salida: " + e.getMessage());
        }

        return result;
    }

    /**
     * PHASE 2: Clase para resultados de validación.
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private int fileCount = 0;

        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        public void setFileCount(int count) { fileCount = count; }

        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        public int getFileCount() { return fileCount; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (hasErrors()) {
                sb.append("Errores:\n");
                errors.forEach(e -> sb.append("  ❌ ").append(e).append("\n"));
            }
            if (hasWarnings()) {
                sb.append("Advertencias:\n");
                warnings.forEach(w -> sb.append("  ⚠️  ").append(w).append("\n"));
            }
            sb.append("Archivos encontrados: ").append(fileCount);
            return sb.toString();
        }
    }
}
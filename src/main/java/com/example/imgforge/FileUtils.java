package com.example.imgforge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilidades para manejo de archivos e imágenes.
 * Incluye búsqueda de archivos, validación de rutas y operaciones batch.
 */
public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    // Extensiones de imagen soportadas
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "bmp", "gif"
    );

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

        String[] units = {"B", "KB", "MB", "GB"};
        double size = bytes;
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * Cuenta el número de archivos de imagen en una carpeta.
     */
    public static int countImageFiles(String folderPath, boolean recursive) {
        return findImageFiles(folderPath, recursive).size();
    }
}
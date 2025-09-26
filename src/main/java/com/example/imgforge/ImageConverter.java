package com.example.imgforge;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Clase principal para la conversión de imágenes entre diferentes formatos.
 * Soporta PNG, JPG/JPEG con opciones de calidad configurables.
 */
public class ImageConverter {

    private static final Logger logger = Logger.getLogger(ImageConverter.class.getName());

    // Formatos soportados
    public enum SupportedFormat {
        PNG("png"),
        JPG("jpg"),
        JPEG("jpeg");

        private final String extension;

        SupportedFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }

        public static SupportedFormat fromString(String format) {
            for (SupportedFormat sf : values()) {
                if (sf.extension.equalsIgnoreCase(format)) {
                    return sf;
                }
            }
            throw new IllegalArgumentException("Formato no soportado: " + format);
        }
    }

    /**
     * Convierte una imagen de un formato a otro.
     *
     * @param inputPath Ruta del archivo de entrada
     * @param outputPath Ruta del archivo de salida
     * @param targetFormat Formato objetivo (png, jpg, jpeg)
     * @return true si la conversión fue exitosa, false en caso contrario
     */
    public boolean convertImage(String inputPath, String outputPath, String targetFormat) {
        return convertImage(inputPath, outputPath, targetFormat, 0.85f);
    }

    /**
     * Convierte una imagen de un formato a otro con calidad específica.
     *
     * @param inputPath Ruta del archivo de entrada
     * @param outputPath Ruta del archivo de salida
     * @param targetFormat Formato objetivo (png, jpg, jpeg)
     * @param quality Calidad de la imagen (0.0 a 1.0, solo aplica para JPG)
     * @return true si la conversión fue exitosa, false en caso contrario
     */
    public boolean convertImage(String inputPath, String outputPath, String targetFormat, float quality) {
        try {
            // Validar entrada
            File inputFile = new File(inputPath);
            if (!inputFile.exists() || !inputFile.isFile()) {
                logger.severe("Archivo de entrada no existe: " + inputPath);
                return false;
            }

            // Validar formato objetivo
            SupportedFormat format;
            try {
                format = SupportedFormat.fromString(targetFormat.toLowerCase());
            } catch (IllegalArgumentException e) {
                logger.severe("Formato no soportado: " + targetFormat);
                return false;
            }

            // Leer imagen de entrada
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                logger.severe("No se pudo leer la imagen: " + inputPath);
                return false;
            }

            // Crear directorio de salida si no existe
            File outputFile = new File(outputPath);
            File outputDir = outputFile.getParentFile();
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Convertir y guardar
            boolean success = saveImage(image, outputFile, format, quality);

            if (success) {
                logger.info("Conversión exitosa: " + inputPath + " -> " + outputPath);
            } else {
                logger.severe("Error al guardar la imagen: " + outputPath);
            }

            return success;

        } catch (IOException e) {
            logger.severe("Error de E/S durante la conversión: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.severe("Error inesperado durante la conversión: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda una BufferedImage en el formato especificado.
     */
    private boolean saveImage(BufferedImage image, File outputFile, SupportedFormat format, float quality)
            throws IOException {

        String formatName = format.getExtension();

        // Para JPG, usar configuración de calidad específica
        if (format == SupportedFormat.JPG || format == SupportedFormat.JPEG) {
            return saveJpegWithQuality(image, outputFile, quality);
        }

        // Para PNG y otros formatos, usar método estándar
        return ImageIO.write(image, formatName, outputFile);
    }

    /**
     * Guarda imagen como JPEG con calidad específica.
     */
    private boolean saveJpegWithQuality(BufferedImage image, File outputFile, float quality)
            throws IOException {

        // Validar rango de calidad
        if (quality < 0.0f) quality = 0.0f;
        if (quality > 1.0f) quality = 1.0f;

        // Obtener ImageWriter para JPEG
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No hay ImageWriter disponible para JPEG");
        }

        ImageWriter writer = writers.next();

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);

            // Configurar parámetros de calidad
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            // Escribir imagen
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            return true;

        } finally {
            writer.dispose();
        }
    }

    /**
     * Verifica si un archivo es una imagen soportada.
     */
    public boolean isSupportedImageFile(String filePath) {
        try {
            String extension = getFileExtension(filePath).toLowerCase();
            SupportedFormat.fromString(extension);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Obtiene la extensión de un archivo.
     */
    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(lastDot + 1);
    }

    /**
     * Obtiene información básica de una imagen.
     */
    public String getImageInfo(String imagePath) {
        try {
            File file = new File(imagePath);
            BufferedImage image = ImageIO.read(file);

            if (image == null) {
                return "No se pudo leer la imagen";
            }

            return String.format("Dimensiones: %dx%d, Tipo: %s, Tamaño: %.2f KB",
                    image.getWidth(),
                    image.getHeight(),
                    getFileExtension(imagePath).toUpperCase(),
                    file.length() / 1024.0);

        } catch (IOException e) {
            return "Error al leer información de la imagen: " + e.getMessage();
        }
    }
}
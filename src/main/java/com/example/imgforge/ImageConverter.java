package com.example.imgforge;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Clase principal para la conversión de imágenes entre diferentes formatos.
 * PHASE 2: Extended format support - TIFF, BMP, GIF, WBMP with advanced features
 */
public class ImageConverter {

    private static final Logger logger = Logger.getLogger(ImageConverter.class.getName());

    // Formatos soportados (PHASE 2: Extended)
    public enum SupportedFormat {
        PNG("png", "image/png", true, false, "Portable Network Graphics"),
        JPG("jpg", "image/jpeg", false, true, "JPEG"),
        JPEG("jpeg", "image/jpeg", false, true, "JPEG"),
        BMP("bmp", "image/bmp", false, false, "Windows Bitmap"),
        GIF("gif", "image/gif", true, false, "Graphics Interchange Format"),
        TIFF("tiff", "image/tiff", true, true, "Tagged Image File Format"),
        TIF("tif", "image/tiff", true, true, "Tagged Image File Format"),
        WBMP("wbmp", "image/vnd.wap.wbmp", false, false, "Wireless Bitmap");

        private final String extension;
        private final String mimeType;
        private final boolean supportsTransparency;
        private final boolean supportsQuality;
        private final String description;

        SupportedFormat(String extension, String mimeType, boolean supportsTransparency,
                        boolean supportsQuality, String description) {
            this.extension = extension;
            this.mimeType = mimeType;
            this.supportsTransparency = supportsTransparency;
            this.supportsQuality = supportsQuality;
            this.description = description;
        }

        public String getExtension() { return extension; }
        public String getMimeType() { return mimeType; }
        public boolean supportsTransparency() { return supportsTransparency; }
        public boolean supportsQuality() { return supportsQuality; }
        public String getDescription() { return description; }

        public static SupportedFormat fromString(String format) {
            for (SupportedFormat sf : values()) {
                if (sf.extension.equalsIgnoreCase(format)) {
                    return sf;
                }
            }
            throw new IllegalArgumentException("Formato no soportado: " + format);
        }

        /**
         * PHASE 2: Verifica si el formato soporta compresión con pérdida.
         */
        public boolean isLossyFormat() {
            return this == JPG || this == JPEG;
        }

        /**
         * PHASE 2: Verifica si es un formato de alta calidad.
         */
        public boolean isHighQualityFormat() {
            return this == PNG || this == TIFF || this == TIF || this == BMP;
        }

        /**
         * PHASE 2: Verifica si requiere conversión especial de color.
         */
        public boolean requiresColorConversion() {
            return this == GIF || this == WBMP;
        }
    }

    // Configuración de conversión
    public static class ConversionConfig {
        private float quality = 0.85f;
        private int pngCompression = 6;
        private boolean preserveQuality = false;
        private boolean maintainAspectRatio = true;
        private Color backgroundColor = Color.WHITE;
        private boolean optimizeForSize = false;
        private boolean preserveMetadata = false;

        // Getters y setters
        public float getQuality() { return quality; }
        public void setQuality(float quality) { this.quality = Math.max(0.0f, Math.min(1.0f, quality)); }

        public int getPngCompression() { return pngCompression; }
        public void setPngCompression(int pngCompression) {
            this.pngCompression = Math.max(0, Math.min(9, pngCompression));
        }

        public boolean isPreserveQuality() { return preserveQuality; }
        public void setPreserveQuality(boolean preserveQuality) { this.preserveQuality = preserveQuality; }

        public boolean isMaintainAspectRatio() { return maintainAspectRatio; }
        public void setMaintainAspectRatio(boolean maintainAspectRatio) { this.maintainAspectRatio = maintainAspectRatio; }

        public Color getBackgroundColor() { return backgroundColor; }
        public void setBackgroundColor(Color backgroundColor) { this.backgroundColor = backgroundColor; }

        public boolean isOptimizeForSize() { return optimizeForSize; }
        public void setOptimizeForSize(boolean optimizeForSize) { this.optimizeForSize = optimizeForSize; }

        public boolean isPreserveMetadata() { return preserveMetadata; }
        public void setPreserveMetadata(boolean preserveMetadata) { this.preserveMetadata = preserveMetadata; }
    }

    // Información detallada de imagen
    public static class ImageInfo {
        private final String fileName;
        private final SupportedFormat format;
        private final int width;
        private final int height;
        private final String colorType;
        private final boolean hasTransparency;
        private final long fileSize;
        private final String compression;

        public ImageInfo(String fileName, SupportedFormat format, int width, int height,
                         String colorType, boolean hasTransparency, long fileSize, String compression) {
            this.fileName = fileName;
            this.format = format;
            this.width = width;
            this.height = height;
            this.colorType = colorType;
            this.hasTransparency = hasTransparency;
            this.fileSize = fileSize;
            this.compression = compression;
        }

        // Getters
        public String getFileName() { return fileName; }
        public SupportedFormat getFormat() { return format; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public String getColorType() { return colorType; }
        public boolean hasTransparency() { return hasTransparency; }
        public long getFileSize() { return fileSize; }
        public String getCompression() { return compression; }
        public long getPixelCount() { return (long) width * height; }
        public double getAspectRatio() { return height != 0 ? (double) width / height : 0; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Archivo: ").append(fileName).append("\n");
            sb.append("Formato: ").append(format.getDescription()).append(" (").append(format.getMimeType()).append(")\n");
            sb.append("Dimensiones: ").append(width).append(" x ").append(height).append(" píxeles\n");
            sb.append("Píxeles totales: ").append(String.format("%,d", getPixelCount())).append("\n");
            sb.append("Relación de aspecto: ").append(String.format("%.2f:1", getAspectRatio())).append("\n");
            sb.append("Tipo de color: ").append(colorType).append("\n");
            sb.append("Transparencia: ").append(hasTransparency ? "Sí" : "No").append("\n");
            sb.append("Tamaño archivo: ").append(FileUtils.formatFileSize(fileSize)).append("\n");
            if (compression != null && !compression.isEmpty()) {
                sb.append("Compresión: ").append(compression).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Convierte una imagen de un formato a otro (método simple para compatibilidad).
     */
    public boolean convertImage(String inputPath, String outputPath, String targetFormat) {
        return convertImage(inputPath, outputPath, targetFormat, 0.85f);
    }

    /**
     * Convierte una imagen de un formato a otro con calidad específica.
     */
    public boolean convertImage(String inputPath, String outputPath, String targetFormat, float quality) {
        return convertImage(inputPath, outputPath, targetFormat, quality, 6, false);
    }

    /**
     * Convierte una imagen con opciones avanzadas de compresión.
     */
    public boolean convertImage(String inputPath, String outputPath, String targetFormat,
                                float quality, int pngCompression, boolean preserveQuality) {
        ConversionConfig config = new ConversionConfig();
        config.setQuality(quality);
        config.setPngCompression(pngCompression);
        config.setPreserveQuality(preserveQuality);

        return convertImage(inputPath, outputPath, targetFormat, config);
    }

    /**
     * PHASE 2: Conversión con configuración completa.
     */
    public boolean convertImage(String inputPath, String outputPath, String targetFormat,
                                ConversionConfig config) {
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

            // Leer imagen de entrada con información detallada
            BufferedImage image = readImageWithValidation(inputFile);
            if (image == null) {
                logger.severe("No se pudo leer la imagen: " + inputPath);
                return false;
            }

            // Crear directorio de salida si no existe
            File outputFile = new File(outputPath);
            if (!FileUtils.ensureDirectoryStructure(outputPath)) {
                logger.severe("No se pudo crear la estructura de directorios para: " + outputPath);
                return false;
            }

            // PHASE 2: Conversión con manejo específico por formato
            boolean success = performAdvancedConversion(image, outputFile, format, config, inputPath);

            if (success) {
                logger.info(String.format("Conversión exitosa: %s -> %s [%s]",
                        inputPath, outputPath, format.getMimeType()));
            } else {
                logger.severe("Error al guardar la imagen: " + outputPath);
            }

            return success;

        } catch (IOException e) {
            logger.severe("Error de E/S durante la conversión: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.severe("Error inesperado durante la conversión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * PHASE 2: Lee imagen con validación robusta.
     */
    private BufferedImage readImageWithValidation(File inputFile) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(inputFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            if (!readers.hasNext()) {
                logger.warning("No se encontró lector para: " + inputFile.getName());
                return ImageIO.read(inputFile); // Fallback
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * PHASE 2: Conversión avanzada con manejo específico por formato.
     */
    private boolean performAdvancedConversion(BufferedImage image, File outputFile,
                                              SupportedFormat format, ConversionConfig config,
                                              String originalPath) throws IOException {

        // Verificar que el formato esté disponible en el sistema
        if (!isFormatSupportedBySystem(format.getExtension())) {
            logger.severe("Formato " + format.getExtension().toUpperCase() +
                    " no está disponible en este sistema");
            return false;
        }

        // Preparar imagen según el formato destino
        BufferedImage processedImage = prepareImageForFormat(image, format, config);

        // Aplicar optimizaciones si se solicita
        if (config.isOptimizeForSize()) {
            processedImage = optimizeImageForSize(processedImage, format);
        }

        // Guardar con método específico del formato
        try {
            switch (format) {
                case JPG:
                case JPEG:
                    return saveJpegImage(processedImage, outputFile, config);

                case PNG:
                    return savePngImage(processedImage, outputFile, config);

                case BMP:
                    return saveBmpImage(processedImage, outputFile, config);

                case GIF:
                    return saveGifImage(processedImage, outputFile, config);

                case TIFF:
                case TIF:
                    return saveTiffImage(processedImage, outputFile, config);

                case WBMP:
                    return saveWbmpImage(processedImage, outputFile, config);

                default:
                    return ImageIO.write(processedImage, format.getExtension(), outputFile);
            }
        } catch (Exception e) {
            logger.severe("Error guardando en formato " + format.getExtension().toUpperCase() +
                    ": " + e.getMessage());
            return false;
        }
    }

    /**
     * PHASE 2: Prepara imagen según el formato destino.
     */
    private BufferedImage prepareImageForFormat(BufferedImage image, SupportedFormat format,
                                                ConversionConfig config) {

        // Manejar transparencia para formatos que no la soportan
        if (!format.supportsTransparency() && hasTransparency(image)) {
            image = removeTransparency(image, config.getBackgroundColor());
            logger.info("Transparencia removida para formato: " + format.getExtension());
        }

        // Conversiones específicas por formato
        if (format.requiresColorConversion()) {
            image = performColorConversion(image, format);
        }

        return image;
    }

    /**
     * PHASE 2: Optimiza imagen para tamaño menor.
     */
    private BufferedImage optimizeImageForSize(BufferedImage image, SupportedFormat format) {
        // Para formatos con pérdida, usar configuraciones que reduzcan tamaño
        if (format.isLossyFormat()) {
            // Ya se maneja en la configuración de calidad
            return image;
        }

        // Para formatos sin pérdida, optimizar tipo de imagen
        int targetType = getOptimalImageType(image, format);
        if (image.getType() != targetType) {
            BufferedImage optimized = new BufferedImage(
                    image.getWidth(), image.getHeight(), targetType);

            Graphics2D g2d = optimized.createGraphics();
            configureGraphicsQuality(g2d, false); // Velocidad sobre calidad para optimización
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();

            return optimized;
        }

        return image;
    }

    /**
     * PHASE 2: Configura Graphics2D para calidad óptima.
     */
    private void configureGraphicsQuality(Graphics2D g2d, boolean highQuality) {
        if (highQuality) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        } else {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
    }

    /**
     * PHASE 2: Obtiene el tipo de imagen óptimo para un formato.
     */
    private int getOptimalImageType(BufferedImage image, SupportedFormat format) {
        switch (format) {
            case JPG:
            case JPEG:
            case BMP:
                return BufferedImage.TYPE_INT_RGB;
            case PNG:
                return hasTransparency(image) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            case GIF:
                return BufferedImage.TYPE_BYTE_INDEXED;
            case WBMP:
                return BufferedImage.TYPE_BYTE_GRAY;
            case TIFF:
            case TIF:
                return image.getType(); // TIFF soporta múltiples tipos
            default:
                return image.getType();
        }
    }

    /**
     * PHASE 2: Realiza conversiones de color específicas.
     */
    private BufferedImage performColorConversion(BufferedImage image, SupportedFormat format) {
        switch (format) {
            case GIF:
                return convertToIndexedColor(image);
            case WBMP:
                return convertToGrayscale(image);
            default:
                return image;
        }
    }

    /**
     * PHASE 2: Convierte imagen a color indexado (para GIF).
     */
    private BufferedImage convertToIndexedColor(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            return image; // Ya es indexado
        }

        // Crear imagen indexada con paleta optimizada
        BufferedImage indexedImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);

        Graphics2D g2d = indexedImage.createGraphics();
        configureGraphicsQuality(g2d, true);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return indexedImage;
    }

    /**
     * PHASE 2: Convierte imagen a escala de grises.
     */
    private BufferedImage convertToGrayscale(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return image; // Ya es escala de grises
        }

        BufferedImage grayImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g2d = grayImage.createGraphics();
        configureGraphicsQuality(g2d, true);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return grayImage;
    }

    /**
     * PHASE 2: Verifica si una imagen tiene transparencia.
     */
    private boolean hasTransparency(BufferedImage image) {
        return image.getColorModel().hasAlpha();
    }

    /**
     * PHASE 2: Remueve transparencia de una imagen con color de fondo configurable.
     */
    private BufferedImage removeTransparency(BufferedImage image, Color backgroundColor) {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = newImage.createGraphics();
        configureGraphicsQuality(g2d, true);
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return newImage;
    }

    /**
     * PHASE 2: Guarda imagen como JPEG con configuración avanzada.
     */
    private boolean saveJpegImage(BufferedImage image, File outputFile, ConversionConfig config)
            throws IOException {

        float quality = config.isPreserveQuality() ? 1.0f : config.getQuality();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No hay ImageWriter disponible para JPEG");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);

                // Configurar algoritmo de compresión si es posible
                String[] compressionTypes = param.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    param.setCompressionType(compressionTypes[0]);
                }
            }

            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            return true;

        } finally {
            writer.dispose();
        }
    }

    /**
     * PHASE 2: Guarda imagen como PNG con configuración avanzada.
     */
    private boolean savePngImage(BufferedImage image, File outputFile, ConversionConfig config)
            throws IOException {

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No hay ImageWriter disponible para PNG");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                // PNG usa nivel de compresión inverso (0 = sin compresión, 9 = máxima)
                float compression = 1.0f - (config.getPngCompression() / 9.0f);
                param.setCompressionQuality(compression);
            }

            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            return true;

        } finally {
            writer.dispose();
        }
    }

    /**
     * PHASE 2: Guarda imagen como BMP.
     */
    private boolean saveBmpImage(BufferedImage image, File outputFile, ConversionConfig config)
            throws IOException {

        // BMP no soporta transparencia, asegurar tipo correcto
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            image = removeTransparency(image, config.getBackgroundColor());
        }

        return ImageIO.write(image, "bmp", outputFile);
    }

    /**
     * PHASE 2: Guarda imagen como GIF con optimización de colores.
     */
    private boolean saveGifImage(BufferedImage image, File outputFile, ConversionConfig config)
            throws IOException {

        // Convertir a imagen indexada si es necesario
        if (image.getType() != BufferedImage.TYPE_BYTE_INDEXED) {
            image = convertToIndexedColor(image);
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!writers.hasNext()) {
            return ImageIO.write(image, "gif", outputFile);
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), null);
            return true;
        } finally {
            writer.dispose();
        }
    }

    /**
     * PHASE 2: Guarda imagen como TIFF con opciones avanzadas.
     */
    private boolean saveTiffImage(BufferedImage image, File outputFile, ConversionConfig config)
            throws IOException {

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
        if (!writers.hasNext()) {
            // Fallback a método estándar
            return ImageIO.write(image, "tiff", outputFile);
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

                String[] compressionTypes = param.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    // Usar LZW si está disponible, sino el primero disponible
                    String compressionType = compressionTypes[0];
                    for (String type : compressionTypes) {
                        if ("LZW".equalsIgnoreCase(type)) {
                            compressionType = type;
                            break;
                        }
                    }
                    param.setCompressionType(compressionType);

                    if (!config.isPreserveQuality()) {
                        param.setCompressionQuality(config.getQuality());
                    }
                }
            }

            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            return true;

        } finally {
            writer.dispose();
        }
    }

    /**
     * PHASE 2: Guarda imagen como WBMP (Wireless Bitmap).
     */
    private boolean saveWbmpImage(BufferedImage image, File outputFile, ConversionConfig config)
            throws IOException {

        // WBMP requiere imagen en escala de grises de 1 bit (blanco y negro)
        BufferedImage binaryImage = convertToBinary(image);

        try {
            return ImageIO.write(binaryImage, "wbmp", outputFile);
        } catch (Exception e) {
            logger.warning("Error escribiendo WBMP con ImageIO, intentando método alternativo: " + e.getMessage());

            // Método alternativo: convertir a escala de grises primero
            BufferedImage grayImage = convertToGrayscale(image);
            return ImageIO.write(grayImage, "wbmp", outputFile);
        }
    }

    /**
     * PHASE 2: Convierte imagen a binario (blanco y negro) para WBMP.
     */
    private BufferedImage convertToBinary(BufferedImage image) {
        // Primero convertir a escala de grises
        BufferedImage grayImage = convertToGrayscale(image);

        // Luego convertir a binario usando threshold
        BufferedImage binaryImage = new BufferedImage(
                grayImage.getWidth(), grayImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D g2d = binaryImage.createGraphics();
        g2d.drawImage(grayImage, 0, 0, null);
        g2d.dispose();

        return binaryImage;
    }

    /**
     * PHASE 2: Obtiene información completa y detallada de una imagen.
     */
    public ImageInfo getCompleteImageInfo(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                return null;
            }

            String extension = FileUtils.getFileExtension(imagePath).toLowerCase();
            SupportedFormat format = SupportedFormat.fromString(extension);

            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null;
            }

            String colorType = getDetailedColorTypeDescription(image);
            boolean hasTransparency = hasTransparency(image);
            String compression = detectCompressionInfo(imagePath, format);

            return new ImageInfo(
                    file.getName(),
                    format,
                    image.getWidth(),
                    image.getHeight(),
                    colorType,
                    hasTransparency,
                    file.length(),
                    compression
            );

        } catch (Exception e) {
            logger.warning("Error al obtener información de imagen " + imagePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * PHASE 2: Obtiene descripción detallada del tipo de color.
     */
    private String getDetailedColorTypeDescription(BufferedImage image) {
        ColorModel colorModel = image.getColorModel();
        String baseType = getColorTypeDescription(image);

        StringBuilder description = new StringBuilder(baseType);

        // Información adicional sobre el modelo de color
        if (colorModel instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) colorModel;
            description.append(String.format(" (%d colores)", icm.getMapSize()));
        }

        description.append(String.format(" - %d bits por píxel", colorModel.getPixelSize()));

        if (colorModel.hasAlpha()) {
            description.append(", canal alfa");
        }

        return description.toString();
    }

    /**
     * PHASE 2: Detecta información de compresión.
     */
    private String detectCompressionInfo(String imagePath, SupportedFormat format) {
        switch (format) {
            case JPG:
            case JPEG:
                return "JPEG (con pérdida)";
            case PNG:
                return "Deflate (sin pérdida)";
            case GIF:
                return "LZW (sin pérdida)";
            case TIFF:
            case TIF:
                return "Variable (LZW/None/JPEG)";
            case BMP:
                return "Sin compresión";
            case WBMP:
                return "Sin compresión";
            default:
                return "Desconocida";
        }
    }

    /**
     * PHASE 2: Verifica si un archivo es una imagen soportada.
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
     * PHASE 2: Obtiene todos los formatos soportados con sus características.
     */
    public String getSupportedFormatsInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Formatos soportados:\n\n");

        for (SupportedFormat format : SupportedFormat.values()) {
            info.append(String.format("%-6s - %s\n",
                    format.getExtension().toUpperCase(),
                    format.getDescription()));
            info.append(String.format("         MIME: %s\n", format.getMimeType()));
            info.append(String.format("         Transparencia: %s\n",
                    format.supportsTransparency() ? "Sí" : "No"));
            info.append(String.format("         Control de calidad: %s\n",
                    format.supportsQuality() ? "Sí" : "No"));
            info.append(String.format("         Tipo: %s\n\n",
                    format.isLossyFormat() ? "Con pérdida" : "Sin pérdida"));
        }

        return info.toString();
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
     * Obtiene descripción del tipo de color de una imagen (método original para compatibilidad).
     */
    private String getColorTypeDescription(BufferedImage image) {
        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB (24-bit)";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB (32-bit con transparencia)";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "Escala de grises (8-bit)";
            case BufferedImage.TYPE_BYTE_INDEXED:
                return "Colores indexados";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "BGR (24-bit)";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "ABGR (32-bit con transparencia)";
            case BufferedImage.TYPE_USHORT_GRAY:
                return "Escala de grises (16-bit)";
            case BufferedImage.TYPE_USHORT_565_RGB:
                return "RGB (16-bit, 5-6-5)";
            case BufferedImage.TYPE_USHORT_555_RGB:
                return "RGB (15-bit, 5-5-5)";
            case BufferedImage.TYPE_BYTE_BINARY:
                return "Binario (1-bit)";
            case BufferedImage.TYPE_INT_BGR:
                return "BGR (24-bit entero)";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return "ABGR premultiplicado (32-bit)";
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "ARGB premultiplicado (32-bit)";
            default:
                return "Tipo personalizado (" + image.getType() + ")";
        }
    }

    /**
     * PHASE 2: Valida que una imagen pueda ser convertida al formato especificado.
     */
    public ValidationResult validateImageConversion(String inputPath, String outputFormat) {
        ValidationResult result = new ValidationResult();

        try {
            // Verificar archivo de entrada
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                result.addError("Archivo no existe: " + inputPath);
                return result;
            }

            // Verificar formato de salida
            SupportedFormat targetFormat;
            try {
                targetFormat = SupportedFormat.fromString(outputFormat);
            } catch (IllegalArgumentException e) {
                result.addError("Formato de salida no soportado: " + outputFormat);
                return result;
            }

            // Obtener información de la imagen
            ImageInfo info = getCompleteImageInfo(inputPath);
            if (info == null) {
                result.addError("No se pudo leer la imagen: " + inputPath);
                return result;
            }

            // Validaciones específicas
            if (info.hasTransparency() && !targetFormat.supportsTransparency()) {
                result.addWarning("El formato " + targetFormat.getExtension().toUpperCase() +
                        " no soporta transparencia. Se aplicará fondo blanco.");
            }

            if (info.getFormat().isHighQualityFormat() && targetFormat.isLossyFormat()) {
                result.addWarning("Conversión de formato sin pérdida a formato con pérdida. " +
                        "Puede haber degradación de calidad.");
            }

            if (info.getPixelCount() > 50_000_000) { // 50 megapíxeles
                result.addWarning("Imagen muy grande (" +
                        String.format("%,d", info.getPixelCount()) + " píxeles). " +
                        "La conversión puede ser lenta.");
            }

            result.setImageInfo(info);

        } catch (Exception e) {
            result.addError("Error validando conversión: " + e.getMessage());
        }

        return result;
    }

    /**
     * PHASE 2: Clase para resultados de validación de conversión.
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        private ImageInfo imageInfo;

        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        public void setImageInfo(ImageInfo info) { imageInfo = info; }

        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public java.util.List<String> getErrors() { return new java.util.ArrayList<>(errors); }
        public java.util.List<String> getWarnings() { return new java.util.ArrayList<>(warnings); }
        public ImageInfo getImageInfo() { return imageInfo; }

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
            if (imageInfo != null) {
                sb.append("Información de imagen:\n");
                sb.append(imageInfo.toString());
            }
            return sb.toString();
        }
    }

    /**
     * PHASE 2: Realiza conversión batch de múltiples imágenes.
     */
    public BatchConversionResult convertImagesBatch(java.util.List<String> inputPaths,
                                                    String outputFolder, String targetFormat,
                                                    ConversionConfig config) {
        BatchConversionResult result = new BatchConversionResult();

        try {
            SupportedFormat format = SupportedFormat.fromString(targetFormat);

            for (String inputPath : inputPaths) {
                String outputPath = generateBatchOutputPath(inputPath, outputFolder, targetFormat);

                long startTime = System.currentTimeMillis();
                boolean success = convertImage(inputPath, outputPath, targetFormat, config);
                long duration = System.currentTimeMillis() - startTime;

                if (success) {
                    result.addSuccess(inputPath, outputPath, duration);
                } else {
                    result.addFailure(inputPath, "Error en conversión");
                }
            }

        } catch (Exception e) {
            logger.severe("Error en conversión batch: " + e.getMessage());
        }

        return result;
    }

    /**
     * PHASE 2: Genera ruta de salida para conversión batch.
     */
    private String generateBatchOutputPath(String inputPath, String outputFolder, String targetFormat) {
        return FileUtils.generateOutputPath(inputPath, outputFolder, targetFormat);
    }

    /**
     * PHASE 2: Clase para resultados de conversión batch.
     */
    public static class BatchConversionResult {
        private final java.util.List<ConversionRecord> successes = new java.util.ArrayList<>();
        private final java.util.List<ConversionRecord> failures = new java.util.ArrayList<>();

        public static class ConversionRecord {
            private final String inputPath;
            private final String outputPath;
            private final long duration;
            private final String error;

            public ConversionRecord(String inputPath, String outputPath, long duration, String error) {
                this.inputPath = inputPath;
                this.outputPath = outputPath;
                this.duration = duration;
                this.error = error;
            }

            public String getInputPath() { return inputPath; }
            public String getOutputPath() { return outputPath; }
            public long getDuration() { return duration; }
            public String getError() { return error; }
            public boolean isSuccess() { return error == null; }
        }

        public void addSuccess(String inputPath, String outputPath, long duration) {
            successes.add(new ConversionRecord(inputPath, outputPath, duration, null));
        }

        public void addFailure(String inputPath, String error) {
            failures.add(new ConversionRecord(inputPath, null, 0, error));
        }

        public int getSuccessCount() { return successes.size(); }
        public int getFailureCount() { return failures.size(); }
        public int getTotalCount() { return successes.size() + failures.size(); }
        public double getSuccessRate() {
            return getTotalCount() > 0 ? (double) getSuccessCount() / getTotalCount() * 100 : 0;
        }
        public long getTotalDuration() {
            return successes.stream().mapToLong(ConversionRecord::getDuration).sum();
        }

        public java.util.List<ConversionRecord> getSuccesses() {
            return new java.util.ArrayList<>(successes);
        }
        public java.util.List<ConversionRecord> getFailures() {
            return new java.util.ArrayList<>(failures);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Resultado conversión batch:\n");
            sb.append(String.format("  Total: %d archivos\n", getTotalCount()));
            sb.append(String.format("  Exitosos: %d (%.1f%%)\n", getSuccessCount(), getSuccessRate()));
            sb.append(String.format("  Fallidos: %d\n", getFailureCount()));
            sb.append(String.format("  Tiempo total: %.2f segundos\n", getTotalDuration() / 1000.0));

            if (getSuccessCount() > 0) {
                double avgTime = getTotalDuration() / (double) getSuccessCount();
                sb.append(String.format("  Tiempo promedio: %.2f ms por imagen\n", avgTime));
            }

            return sb.toString();
        }
    }

    /**
     * Obtiene información básica de una imagen (compatibilidad con versión anterior).
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

    /**
     * PHASE 2: Obtiene información detallada de una imagen como texto formateado.
     */
    public String getDetailedImageInfo(String imagePath) {
        ImageInfo info = getCompleteImageInfo(imagePath);
        return info != null ? info.toString() : "No se pudo obtener información de la imagen";
    }

    /**
     * PHASE 2: Verifica si el sistema soporta un formato específico.
     */
    public boolean isFormatSupportedBySystem(String format) {
        try {
            SupportedFormat.fromString(format);

            // Verificar disponibilidad específica para WBMP
            if ("wbmp".equalsIgnoreCase(format)) {
                // WBMP puede no estar disponible en todos los sistemas
                String[] writerFormats = ImageIO.getWriterFormatNames();
                for (String writerFormat : writerFormats) {
                    if ("wbmp".equalsIgnoreCase(writerFormat)) {
                        return true;
                    }
                }
                logger.warning("WBMP writer no disponible en este sistema");
                return false;
            }

            // Para otros formatos, verificar readers/writers
            return ImageIO.getImageReadersByFormatName(format).hasNext() &&
                    ImageIO.getImageWritersByFormatName(format).hasNext();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * PHASE 2: Obtiene estadísticas de uso de memoria para una imagen.
     */
    public String getMemoryUsageInfo(String imagePath) {
        try {
            ImageInfo info = getCompleteImageInfo(imagePath);
            if (info == null) {
                return "No se pudo analizar la imagen";
            }

            long pixelCount = info.getPixelCount();
            long estimatedRAM = pixelCount * 4; // 4 bytes por píxel (ARGB)
            long compressionRatio = info.getFileSize() > 0 ? estimatedRAM / info.getFileSize() : 0;

            StringBuilder sb = new StringBuilder();
            sb.append("Uso de memoria estimado:\n");
            sb.append(String.format("  Píxeles: %,d\n", pixelCount));
            sb.append(String.format("  RAM necesaria: %s\n", FileUtils.formatFileSize(estimatedRAM)));
            sb.append(String.format("  Tamaño en disco: %s\n", FileUtils.formatFileSize(info.getFileSize())));
            sb.append(String.format("  Ratio compresión: %d:1\n", compressionRatio));

            return sb.toString();

        } catch (Exception e) {
            return "Error calculando uso de memoria: " + e.getMessage();
        }
    }
}
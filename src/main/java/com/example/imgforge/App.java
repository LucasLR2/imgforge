package com.example.imgforge;

import picocli.CommandLine;

/**
 * Clase principal de ImgForge.
 * Punto de entrada de la aplicación que inicializa el CLI handler.
 */
public class App {

    public static void main(String[] args) {
        // Banner de bienvenida
        printBanner();

        // Si no hay argumentos, mostrar ayuda
        if (args.length == 0) {
            System.out.println("💡 Usa --help para ver todas las opciones disponibles");
            System.out.println();

            // Mostrar ejemplo básico
            System.out.println("📋 Ejemplo de uso:");
            System.out.println("   java -jar imgforge.jar -i ./fotos -o ./convertidas -f jpg");
            System.out.println();

            args = new String[]{"--help"};
        }

        // Ejecutar CLI handler
        int exitCode = new CommandLine(new CLIHandler()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Muestra el banner de bienvenida de ImgForge.
     */
    private static void printBanner() {
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║            🔨 ImgForge v1.0           ║");
        System.out.println("║      Conversor batch de imágenes      ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println();
    }
}
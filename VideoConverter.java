import java.io.File;
import java.io.IOException;

public class VideoConverter {

    public static void convertToMp4(String inputPath, String outputPath) throws IOException {
        System.out.println("I'M CONVERTING");
        
        // Commande FFmpeg pour la conversion
        String command = "ffmpeg -i \"" + inputPath + "\" -c:v libx264 -c:a aac -strict experimental \"" + outputPath + "\"";

        // Exécuter la commande de conversion
        Process process = new ProcessBuilder(command.split(" "))
                .inheritIO()
                .start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException("Conversion interrompue.");
        } finally {
            process.onExit();
        }

        // Vérifier si le fichier a bien été créé
        File mp4File = new File(outputPath);
        if (!mp4File.exists()) {
            throw new IOException("Erreur lors de la création du fichier MP4.");
        }
    }
}

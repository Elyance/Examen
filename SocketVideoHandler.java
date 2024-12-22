import java.io.*;
import java.util.Arrays;

public class SocketVideoHandler {

    private static final String[] SUPPORTED_VIDEO_FORMATS = {
        ".mp4"
    };

    public static boolean isSupportedVideoFormat(File videoFile) {
        String fileName = videoFile.getName().toLowerCase();
        return Arrays.stream(SUPPORTED_VIDEO_FORMATS).anyMatch(fileName::endsWith);   //condition de verification
    }

    public static void streamVideo(BufferedReader in, OutputStream out, String videoDirectory, String videoName) {
        File videoFile = new File(videoDirectory, videoName);
        if (!videoFile.exists() || !videoFile.isFile()) {
            sendNotFound(out);
            return;
        }

        try (FileInputStream videoInputStream = new FileInputStream(videoFile)) {
            // Analyse de l'en-tête HTTP pour le Range
            int startByte = 0;
            int endByte = (int) videoFile.length() - 1;

            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Range:")) {
                    try {
                        String rangeValue = line.split(":")[1].trim();
                        String[] ranges = rangeValue.split("=")[1].split("-");
                        startByte = Integer.parseInt(ranges[0]);
                        if (ranges.length > 1 && !ranges[1].isEmpty()) {
                            endByte = Integer.parseInt(ranges[1]);
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        sendNotFound(out);
                        return;
                    }
                }
            }

            // Longueur des données à envoyer
            int lengthToSend = endByte - startByte + 1;

            // En-têtes HTTP
            out.write("HTTP/1.1 206 Partial Content\r\n".getBytes());
            out.write(("Content-Range: bytes " + startByte + "-" + endByte + "/" + videoFile.length() + "\r\n").getBytes());
            out.write("Content-Type: video/mp4\r\n".getBytes());
            out.write("Cache-Control: no-cache\r\n".getBytes());
            out.write("Connection: close\r\n\r\n".getBytes());

            // Streaming des données vidéo
            videoInputStream.skip(startByte);
            byte[] buffer = new byte[8192];
            int bytesRead;
            int bytesRemaining = lengthToSend;

            while ((bytesRead = videoInputStream.read(buffer, 0, Math.min(buffer.length, bytesRemaining))) != -1) {
                out.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
                if (bytesRemaining <= 0) {
                    break;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            out.flush();
        } catch (IOException e) {
            System.out.println("mahasosotra");
            sendNotFound(out);
        }
    }

    public static File convertToMp4(File videoFile) throws IOException {
        String lectureDirectory = "./lecture";
        File responseFile = new File(lectureDirectory, videoFile.getName().replaceFirst("[.][^.]+$", "") + ".mp4");
        System.out.println("conversion de la video");
        if (responseFile.exists()) {
            return responseFile;
        }
        if (!new File(lectureDirectory).exists()) {
            new File(lectureDirectory).mkdirs();
        }
        VideoConverter.convertToMp4(videoFile.getAbsolutePath(), responseFile.getAbsolutePath());
        System.out.println("video convertit");
        if (!responseFile.exists()) {
            throw new IOException("Erreur lors de la conversion de la vidéo.");
        }
        return responseFile;
    }

    private static void sendNotFound(OutputStream out) {
        try {
            out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
            out.write("Content-Type: text/plain\r\n".getBytes());
            out.write("Connection: close\r\n\r\n".getBytes());
            out.write("Fichier introuvable.\r\n".getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

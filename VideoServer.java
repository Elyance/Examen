import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.Properties;

public class VideoServer {

    private static String VIDEO_DIRECTORY;
    private static int PORT;
    private static String LISTEN_IP;
    private static String LECTURE_DIRECTORY;

    public static void main(String[] args) {
        loadConfig();

        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(LISTEN_IP))) {
            System.out.println("Serveur de streaming démarré sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config.conf")) {
            properties.load(input);

            PORT = Integer.parseInt(properties.getProperty("port", "8080"));
            VIDEO_DIRECTORY = properties.getProperty("video_directory", "./videos");
            LECTURE_DIRECTORY = properties.getProperty("lecture_directory", "./lecture");
            LISTEN_IP = properties.getProperty("listen_ip", "0.0.0.0");
            properties.getProperty("static_directory", "./static");
        } catch (IOException ex) {
            System.err.println("Erreur lors du chargement du fichier de configuration.");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void handleClient(Socket clientSocket) throws SQLException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                return;
            }

            String requestedResource = requestLine.split(" ")[1];
            String searchQuery = null;

            if (requestedResource.contains("?")) {
                String[] parts = requestedResource.split("\\?", 2);
                requestedResource = parts[0];
                String queryString = parts[1];

                for (String param : queryString.split("&")) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2 && keyValue[0].equals("search")) {
                        searchQuery = URLDecoder.decode(keyValue[1], "UTF-8");
                    }
                }
            }

            if (requestedResource.startsWith("/static/")) {
                System.out.println("css okey");
                HttpHandler.serveStaticFile(out, requestedResource.substring(8));
            }
            else if (requestedResource.equals("/")) {
                System.out.println("racine okey");
                HttpHandler.sendVideoList(out, searchQuery, HttpHandler.getVideoFiles(VIDEO_DIRECTORY),"info");
            }
            else if (requestedResource.startsWith("/lecture/")) {
                System.out.println("tsy zay fa ...");
                String videoName = URLDecoder.decode(requestedResource.substring(9), "UTF-8");
                File videoFile = new File(VIDEO_DIRECTORY, videoName);
                if (videoFile.exists() && videoFile.isFile()) {
                    if (SocketVideoHandler.isSupportedVideoFormat(videoFile)) {
                        System.out.println("supported video");
                        SocketVideoHandler.streamVideo(in, out, VIDEO_DIRECTORY, videoName);
                    } else {
                        System.out.println("nonsupported video");
                        File convertedVideoFile = SocketVideoHandler.convertToMp4(videoFile);
                        SocketVideoHandler.streamVideo(in, out,LECTURE_DIRECTORY, convertedVideoFile.getName());
                    }
                } else {
                    HttpHandler.sendNotFound(out);
                }
            } else if (requestedResource.startsWith("/playlist/")) {
                String playlistId = URLDecoder.decode(requestedResource.substring(10), "UTF-8");
                HttpHandler.sendVideoList(out, searchQuery, HttpHandler.getVideoInPLaylist(VIDEO_DIRECTORY, Integer.valueOf(playlistId).intValue()),"playlist");
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                HttpHandler.sendNotFound(clientSocket.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

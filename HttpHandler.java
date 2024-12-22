import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HttpHandler {

    private static final String[] SUPPORTED_VIDEO_FORMATS = {
        ".mp4", ".avi", ".mkv", ".mov", ".flv", ".wmv", ".webm", ".mpeg", ".3gp"
    };

    public static void serveStaticFile(OutputStream out, String fileName) throws IOException {
        String STATIC_DIRECTORY = "./static"; // Vous pouvez passer cela en paramètre ou via une configuration
        File staticFile = new File(STATIC_DIRECTORY, fileName);

        if (staticFile.exists() && staticFile.isFile()) {
            String contentType = Files.probeContentType(staticFile.toPath());

            String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + (contentType != null ? contentType : "application/octet-stream") + "\r\n" +
                    "Content-Length: " + staticFile.length() + "\r\n" +
                    "Connection: close\r\n\r\n";
            out.write(responseHeaders.getBytes());

            try (FileInputStream fis = new FileInputStream(staticFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

        } else {
            sendNotFound(out);
        }
    }

    public static void sendNotFound(OutputStream out) throws IOException {
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Erreur 404</title>
                    <link rel='stylesheet' href='/static/deco.css'>
                </head>
                <body>
                    <header>
                        <div class='logo'>
                            <img src='/static/logo.png' alt='Logo'>
                            <h1>Streaming Vidéo</h1>
                        </div>
                    </header>
                    <main>
                        <div class='error-page'>
                            <h2>404 - Fichier non trouvé</h2>
                            <p>Le fichier demandé est introuvable. Veuillez vérifier l'URL ou retourner à la <a href='/'>page d'accueil</a>.</p>
                        </div>
                    </main>
                    <footer>
                        <div class='contact'>
                            <h3>Contactez-nous</h3>
                            <p>Email: contact@streaming.com</p>
                            <p>Téléphone: +261 34 12 34 56</p>
                        </div>
                    </footer>
                </body>
                </html>
                """;

        String responseHeaders = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + html.length() + "\r\n\r\n";
        out.write(responseHeaders.getBytes());
        out.write(html.getBytes());
    }

    public static List<File> getVideoFiles(String directoryPath) {
        File directory = new File(directoryPath);

        // Vérifie si le chemin donné est un dossier
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Le chemin spécifié n'est pas un dossier valide : " + directoryPath);
        }

        // Filtrer les fichiers vidéo
        File[] videoFiles = directory.listFiles((dir, name) -> {
            for (String format : SUPPORTED_VIDEO_FORMATS) {
                if (name.toLowerCase().endsWith(format)) {
                    return true;
                }
            }
            return false;
        });

        // Convertir le tableau en liste
        return videoFiles != null ? Arrays.asList(videoFiles) : new ArrayList<>();
    }

    public static List<File> getVideoInPLaylist(String directoryPath,int idPlaylist) {
        File directory = new File(directoryPath);
        // Vérifie si le chemin donné est un dossier
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Le chemin spécifié n'est pas un dossier valide : " + directoryPath);
        }

        // Filtrer les fichiers vidéo
        File[] videoFiles = directory.listFiles((dir, name) -> {
            for (String format : SUPPORTED_VIDEO_FORMATS) {
                try {
                    for (String nomVideo : new PlaylistManager().listerVideos(idPlaylist)) {
                        if (name.toLowerCase().endsWith(format) && name.toLowerCase().equals(nomVideo.toLowerCase())) {
                            return true;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
        return videoFiles != null ? Arrays.asList(videoFiles) : new ArrayList<>();
    }

    public static void sendVideoList(OutputStream out, String searchQuery, List<File> videoFiles,String info) throws IOException, SQLException {
        // Filtrer les vidéos selon la recherche
        List<File> filteredVideos = videoFiles.stream()
                .filter(video -> searchQuery == null || video.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());
    
        // Générer la liste HTML avec actions limitées
        String videoListHtml = getStringBuilderListe(filteredVideos,info);

        String playlist=getStringBuilderPlaylists(new PlaylistManager().listerPlaylists());
    
        // Construire la page HTML complète
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html lang='en'>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>Liste des Vidéos</title>")
                .append("<link rel='stylesheet' href='/static/deco.css'>")
                .append("<link rel='stylesheet' href='/static/deco.css'>")
                .append("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css\">")
                .append("<script src='/static/javaScript.js'></script>")
                .append("</head>")
                .append("<body>")
                .append("<header>")
                .append("<div class='logo'>")
                .append("<img src='/static/logo.jpg' alt='Logo'>")
                .append("<h1>Streaming Vidéo</h1>")
                .append("</div>")
                .append("<form method='GET' action='/' class='search-bar'>")
                .append("<input type='text' name='search' placeholder='Rechercher une vidéo...' value='"
                        + (searchQuery != null ? searchQuery : "") + "'>")
                .append("<button type='submit'>Rechercher</button>")
                .append("</form>")
                .append("</header>")
                .append("<main>")
                .append(playlist)
                .append("<div id=\"addPlaylistForm\" class=\"popup\">"
                    + "<div class=\"popup-content\">"
                          +"<span class=\"close-button\" onclick=\"closeAddPlaylistForm()\">&times;</span>"
                            + "<h2>Ajouter une Nouvelle Playlist</h2>"
                            +"<form method=\"POST\" action=\"/addPlaylist\">"
                                +"<label for=\"playlistName\">Nom de la Playlist</label>"
                                +"<input type=\"text\" id=\"playlistName\" name=\"playlistName\" placeholder=\"Entrez le nom de la playlist\" required>"+
                                 "<button type=\"submit\" class=\"btn-submit\">Ajouter</button>"
                            +"</form>"
                        +"</div>"
                        +"</div>")
                .append("<h2>Liste des vidéos disponibles</h2>")
                .append(videoListHtml) // Insérer la liste HTML générée ici
                .append("</main>")
                .append("<footer>")
                .append("<div class='contact'>")
                .append("<h3>Contactez-nous</h3>")
                .append("<p>Email: contact@streaming.com</p>")
                .append("<p>Téléphone: +261 34 12 34 56</p>")
                .append("</div>")
                .append("</footer>")
                .append("</body>")
                .append("</html>");
    
        // Envoyer la réponse HTTP avec le HTML
        String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + html.length() + "\r\n\r\n";
        out.write(responseHeaders.getBytes());
        out.write(html.toString().getBytes());
    }

    public static String getStringBuilderPlaylists(List<Playlist> playlists) throws UnsupportedEncodingException {
        StringBuilder html = new StringBuilder();
        
        // Début de la section de playlists
        html.append("<section class='playlists'>")
            .append("<h3>Playlists Disponibles</h3>")
            .append("<div class='carousel'>");
        
        if (playlists.isEmpty()) {
            html.append("<div class='playlist-item'>Aucune playlist disponible</div>");
        } else {
            // Ajouter les playlists dans le carrousel
            for (Playlist playlist : playlists) {
                html.append("<div class='playlist-item'>")
                    .append("<a href='/playlist/").append(URLEncoder.encode(String.valueOf(playlist.getIdPlaylist()), "UTF-8")).append("'>")
                    .append(playlist.getNomPlaylist())
                    .append("</a>")
                    .append("</div>");
            }
        }
    
        // Ajouter un div pour l'ajout d'une nouvelle playlist avec seulement l'image
        html.append("<div class='playlist-item add-playlist'>")
            .append("<img src='/static/addPlaylist.png' alt='Ajouter Playlist' onclick=\"showAddPlaylistForm()\">")  // Juste l'image
            .append("</div>");
    
        html.append("</div>") // Fermeture du carrousel
            .append("</section>");
        
        return html.toString();
    }    

    public static String getStringBuilderListe(List<File> videoFiles,String info) {
        StringBuilder html = new StringBuilder();

        html.append("<ul class='video-list'>");

        if (videoFiles != null && !videoFiles.isEmpty()) {
            for (File video : videoFiles) {
                try {
                    html.append("<li>")
                        .append("<div class='video-item'>")
                        .append("<a href=\"/lecture/")
                        .append(URLEncoder.encode(video.getName(), "UTF-8"))
                        .append("\">")
                        .append("<i class='fas fa-play'></i> ")
                        .append(video.getName())
                        .append("</a>")
                        .append("<div class='actions'>")
                        .append("<button onclick=\"addToPlaylist('")
                        .append(URLEncoder.encode(video.getName(), "UTF-8"))
                        .append("')\" title='Ajouter à une playlist'><i class='fas fa-list'></i></button>")
                        .append("<button onclick=\"addToFavorites('")
                        .append(URLEncoder.encode(video.getName(), "UTF-8"))
                        .append("')\" title='Ajouter aux favoris'><i class='fas fa-heart'></i></button>");
                    
                        if(info == "playlist") {
                            html.append("<button onclick=\"deleteVideo('")
                            .append(URLEncoder.encode(video.getName(), "UTF-8"))
                            .append("')\" title='Supprimer'><i class='fas fa-trash'></i></button>");
                        }
                        
                        html.append("</div>")
                        .append("</div>")
                        .append("</li>");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else {
            html.append("<li>Aucune vidéo disponible</li>");
        }

        html.append("</ul>");
        return html.toString();
    }



}

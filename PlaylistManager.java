import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {


    public Connection getConnection() throws SQLException {
        return new MyConnexion("root", "").connectSql("streaming");
    }

    // Ajouter une nouvelle playlist
    public int ajouterPlaylist(String nom) throws SQLException {
        String sql = "INSERT INTO playlist (nom) VALUES (?)";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nom);
            stmt.executeUpdate();

            // Récupérer l'id généré pour la playlist
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println("l'insertion etst reussie");
                    return rs.getInt(1); // Retourne l'id de la nouvelle playlist
                }
            }
        }
        System.out.println("insertion echouee");
        return -1; // Erreur si aucun id n'a été généré
    }

    // Supprimer une playlist
    public boolean supprimerPlaylist(int idPlaylist) throws SQLException {
        String sql = "DELETE FROM playlist WHERE idPlaylist = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idPlaylist);
            return stmt.executeUpdate() > 0; // Retourne true si une ligne a été supprimée
        }
    }

    // Ajouter une vidéo à une playlist
    public boolean ajouterVideo(int idPlaylist, String fichier) throws SQLException {
        String sql = "INSERT INTO playlistVideo (idPlaylist, fichier) VALUES (?, ?)";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idPlaylist);
            stmt.setString(2, fichier);
            return stmt.executeUpdate() > 0; // Retourne true si l'insertion a réussi
        }
    }

    // Supprimer une vidéo d'une playlist
    public boolean supprimerVideo(int idPlaylist, String fichier) throws SQLException {
        String sql = "DELETE FROM playlistVideo WHERE idPlaylist = ? AND fichier = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idPlaylist);
            stmt.setString(2, fichier);
            return stmt.executeUpdate() > 0; // Retourne true si une ligne a été supprimée
        }
    }

    // Récupérer toutes les playlists
    public List<Playlist> listerPlaylists() throws SQLException {
        List<Playlist> playlists = new ArrayList<Playlist>();
        String sql = "SELECT * FROM playlist";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                playlists.add(new Playlist(rs.getInt("idPlaylist"), rs.getString("nom")));
            }
        }
        return playlists;
    }

    // Récupérer les vidéos d'une playlist
    public List<String> listerVideos(int idPlaylist) throws SQLException {
        List<String> videos = new ArrayList<>();
        String sql = "SELECT fichier FROM playlistVideo WHERE idPlaylist = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idPlaylist);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    videos.add(rs.getString("fichier"));
                }
            }
        }
        return videos;
    }

    // Vérifier si une vidéo existe dans une playlist
    public boolean videoExiste(int idPlaylist, String fichier) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM playlistVideo WHERE idPlaylist = ? AND fichier = ?";
        try (PreparedStatement stmt = this.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idPlaylist);
            stmt.setString(2, fichier);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0; // Retourne true si la vidéo existe
                }
            }
        }
        return false;
    }
}

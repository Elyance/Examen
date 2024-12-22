public class Playlist {
    int idPlaylist;
    String nomPlaylist;

    public int getIdPlaylist() {
        return idPlaylist;
    }
    public void setIdPlaylist(int idPlaylist) {
        this.idPlaylist = idPlaylist;
    }
    public String getNomPlaylist() {
        return nomPlaylist;
    }
    public void setNomPlaylist(String nomPlaylist) {
        this.nomPlaylist = nomPlaylist;
    }

    public Playlist(int id, String nom) {
        this.setIdPlaylist(id);
        this.setNomPlaylist(nom);
    }

    
}

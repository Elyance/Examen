create database streaming;
use streaming;

-- Table playlist
CREATE TABLE playlist (
    idPlaylist INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL
);

-- Table playlistVideo
CREATE TABLE playlistVideo (
    idPlaylist_video INT AUTO_INCREMENT PRIMARY KEY,
    idPlaylist INT NOT NULL,
    fichier VARCHAR(255) NOT NULL,
    FOREIGN KEY (idPlaylist) REFERENCES playlist(idPlaylist) ON DELETE CASCADE
);


INSERT INTO playlist (nom) VALUES
('Favorits');

INSERT INTO playlistVideo (idPlaylist, fichier) VALUES
(1, 'video1.mp4'),
(1, 'video2.mp4');



SELECT * FROM playlist;


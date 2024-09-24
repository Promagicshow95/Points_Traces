package gtfs;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Horaire_Arret {

    public static void main(String[] args) {
        try {
            // Initialisation du répertoire GTFS
            File gtfsDirectory = new File("src/main/resources/tisseo_gtfs_v2");
            if (!gtfsDirectory.exists() || !gtfsDirectory.isDirectory()) {
                System.err.println("Le répertoire GTFS spécifié est invalide.");
                return;
            }

            // Lire les fichiers GTFS
            GtfsDaoImpl store = loadGtfsData(gtfsDirectory);

            // Utiliser le stop_id pour l'arrêt spécifique
            String stopId = "stop_point:SP_1249"; // Exemple d'arrêt avec stop_id

            // Afficher les heures de départs/arrivées pour cet arrêt
            displayTimetableForStop(store, stopId);

        } catch (Exception e) {
            System.err.println("Erreur lors du traitement des fichiers GTFS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Charger les données GTFS depuis le répertoire spécifié
    private static GtfsDaoImpl loadGtfsData(File gtfsDirectory) throws Exception {
        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(gtfsDirectory);

        // Stocker les entités GTFS
        GtfsDaoImpl store = new GtfsDaoImpl();
        reader.setEntityStore(store);

        // Lecture des fichiers GTFS
        reader.run();
        return store;
    }

    // Méthode pour convertir les secondes en HH:MM:SS
    private static String convertSecondsToHHMMSS(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    // Afficher les horaires d'arrivée et de départ pour un arrêt donné en utilisant stop_id
    private static void displayTimetableForStop(GtfsDaoImpl store, String stopId) {
        // Rechercher les StopTimes associés à l'arrêt donné en utilisant stop_id
        List<StopTime> stopTimes = store.getAllStopTimes().stream()
                .filter(stopTime -> stopTime.getStop().getId().getId().equalsIgnoreCase(stopId)) // Filtrer par stop_id
                .distinct() // Supprimer les doublons
                .sorted((st1, st2) -> Integer.compare(st1.getArrivalTime(), st2.getArrivalTime())) // Trier par heure d'arrivée
                .collect(Collectors.toList());

        if (stopTimes.isEmpty()) {
            System.out.println("Aucun horaire trouvé pour l'arrêt avec stop_id : " + stopId);
            return;
        }

        // Afficher les heures d'arrivée et de départ pour cet arrêt
        System.out.println("Horaires pour l'arrêt avec stop_id : " + stopId);
        for (StopTime stopTime : stopTimes) {
            // Cast StopLocation to Stop
            StopLocation stopLocation = stopTime.getStop();
            if (stopLocation instanceof Stop) {
                Stop stop = (Stop) stopLocation;
                String arrivalTime = convertSecondsToHHMMSS(stopTime.getArrivalTime());
                String departureTime = convertSecondsToHHMMSS(stopTime.getDepartureTime());
                
                // Afficher les horaires sans doublons
                System.out.println("  Arrivée : " + arrivalTime + " | Départ : " + departureTime);
            } else {
                System.out.println("L'arrêt avec stop_id " + stopId + " n'est pas un Stop valide.");
            }
        }
    }
}


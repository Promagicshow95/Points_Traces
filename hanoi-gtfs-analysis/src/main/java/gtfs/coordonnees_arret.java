package gtfs;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class coordonnees_arret {

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

            // Afficher les trajets avec les arrêts, leurs coordonnées et horaires
            displayTripsWithStopsAndTimes(store);

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

    // Méthode pour convertir les secondes en format HH:mm:ss
    private static String convertSecondsToHHMMSS(int seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    // Afficher les trajets avec les arrêts, coordonnées et horaires
    private static void displayTripsWithStopsAndTimes(GtfsDaoImpl store) {
        System.out.println("Trajets, Arrêts, Coordonnées, et Horaires:");

        // Parcourir tous les trajets
        for (Trip trip : store.getAllTrips()) {
            System.out.println("\nTrip ID: " + trip.getId());

            // Filtrer les StopTimes pour ce trip
            List<StopTime> stopTimes = store.getAllStopTimes().stream()
                    .filter(stopTime -> stopTime.getTrip().equals(trip))
                    .collect(Collectors.toList());

            // Parcourir les arrêts de ce trip
            for (StopTime stopTime : stopTimes) {
                // Obtenir l'objet Stop depuis StopLocation
                if (stopTime.getStop() instanceof Stop) {
                    Stop stop = (Stop) stopTime.getStop();

                    // Afficher les informations de l'arrêt
                    System.out.println("  Arrêt: " + stop.getName());
                    System.out.println("    Latitude: " + stop.getLat());
                    System.out.println("    Longitude: " + stop.getLon());

                    // Convertir et afficher les horaires d'arrivée et de départ en format HH:mm:ss
                    String arrivalTime = convertSecondsToHHMMSS(stopTime.getArrivalTime());
                    String departureTime = convertSecondsToHHMMSS(stopTime.getDepartureTime());
                    System.out.println("    Heure d'arrivée: " + arrivalTime);
                    System.out.println("    Heure de départ: " + departureTime);
                }
            }
        }
    }
}

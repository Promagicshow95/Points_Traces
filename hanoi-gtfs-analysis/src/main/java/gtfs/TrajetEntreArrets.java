package gtfs;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrajetEntreArrets {

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

            // Les stop_id des deux arrêts
            String stopA = "stop_point:SP_1831"; // Exemple d'ID pour l'arrêt A (point de départ)
            String stopB = "stop_point:SP_3411"; // Exemple d'ID pour l'arrêt B (point d'arrivée)

            // Trouver et afficher le trajet entre ces deux arrêts
            findTripBetweenStops(store, stopA, stopB);

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

    // Trouver le trajet entre deux arrêts
    private static void findTripBetweenStops(GtfsDaoImpl store, String stopA, String stopB) {
        // Trouver tous les trips qui passent par l'arrêt A
        List<Trip> tripsWithStopA = store.getAllStopTimes().stream()
                .filter(stopTime -> stopTime.getStop().getId().getId().equals(stopA))
                .map(StopTime::getTrip)
                .distinct()
                .collect(Collectors.toList());

        // Trouver tous les trips qui passent par l'arrêt B
        List<Trip> tripsWithStopB = store.getAllStopTimes().stream()
                .filter(stopTime -> stopTime.getStop().getId().getId().equals(stopB))
                .map(StopTime::getTrip)
                .distinct()
                .collect(Collectors.toList());

        // Trouver les trips qui passent à la fois par A et B
        List<Trip> commonTrips = tripsWithStopA.stream()
                .filter(tripsWithStopB::contains)
                .collect(Collectors.toList());

        // Parcourir les trips communs et vérifier l'ordre des arrêts (A doit apparaître avant B)
        for (Trip trip : commonTrips) {
            List<StopTime> stopTimes = store.getAllStopTimes().stream()
                    .filter(stopTime -> stopTime.getTrip().equals(trip))
                    .sorted((st1, st2) -> Integer.compare(st1.getStopSequence(), st2.getStopSequence())) // Trier par séquence d'arrêt
                    .collect(Collectors.toList());

            // Trouver l'index de stopA et stopB
            Optional<StopTime> stopTimeA = stopTimes.stream()
                    .filter(stopTime -> stopTime.getStop().getId().getId().equals(stopA))
                    .findFirst();

            Optional<StopTime> stopTimeB = stopTimes.stream()
                    .filter(stopTime -> stopTime.getStop().getId().getId().equals(stopB))
                    .findFirst();

            // Si les deux arrêts sont trouvés dans le même trip
            if (stopTimeA.isPresent() && stopTimeB.isPresent()) {
                int indexA = stopTimeA.get().getStopSequence();
                int indexB = stopTimeB.get().getStopSequence();

                // Vérifier que l'arrêt A vient avant l'arrêt B
                if (indexA < indexB) {
                    System.out.println("Trajet trouvé entre les arrêts " + stopA + " et " + stopB + " dans le trip: " + trip.getId());
                    return;
                }
            }
        }

        System.out.println("Aucun trajet trouvé entre les arrêts " + stopA + " et " + stopB);
    }
}

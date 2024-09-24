package gtfs;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopLocation;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Liste_Arret {

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

            // Afficher les points de tracé entre deux arrêts spécifiques dans un trajet
            displayShapePointsBetweenStops(store, "1965630", "Guyenne-Berry", "Aéroport");

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

    // Afficher les points de tracé (lat/lon) entre deux arrêts spécifiques
    private static void displayShapePointsBetweenStops(GtfsDaoImpl store, String tripId, String startStopName, String endStopName) {
        System.out.println("Points de tracé entre les arrêts " + startStopName + " et " + endStopName + " pour le trajet " + tripId + ":");

        // Trouver le trajet (Trip) par son ID
        Trip trip = store.getAllTrips().stream()
                .filter(t -> t.getId().getId().equals(tripId))
                .findFirst()
                .orElse(null);

        if (trip == null) {
            System.out.println("Trip non trouvé avec l'ID " + tripId);
            return;
        }

        // Récupérer les StopTimes associés à ce trip
        List<StopTime> stopTimes = store.getAllStopTimes().stream()
                .filter(stopTime -> stopTime.getTrip().equals(trip))
                .sorted((st1, st2) -> Integer.compare(st1.getStopSequence(), st2.getStopSequence())) // Assurer que les arrêts sont dans l'ordre
                .collect(Collectors.toList());

        // Trouver les StopTimes correspondant aux arrêts de départ et d'arrivée
        StopTime startStopTime = stopTimes.stream()
                .filter(stopTime -> stopTime.getStop().getName().equalsIgnoreCase(startStopName))
                .findFirst()
                .orElse(null);

        StopTime endStopTime = stopTimes.stream()
                .filter(stopTime -> stopTime.getStop().getName().equalsIgnoreCase(endStopName))
                .findFirst()
                .orElse(null);

        if (startStopTime == null || endStopTime == null) {
            System.out.println("Un ou plusieurs arrêts spécifiés n'ont pas été trouvés dans ce trajet.");
            return;
        }

        // Récupérer le shape ID associé au trajet
        String shapeId = trip.getShapeId().getId();

        // Récupérer les points de tracé associés à ce shape ID et filtrer ceux qui sont entre les deux arrêts
        List<ShapePoint> shapePoints = store.getAllShapePoints().stream()
                .filter(shapePoint -> shapePoint.getShapeId().getId().equals(shapeId))
                .filter(shapePoint -> shapePoint.getSequence() >= startStopTime.getStopSequence() && shapePoint.getSequence() <= endStopTime.getStopSequence())
                .collect(Collectors.toList());

        // Afficher les points de tracé (lat/lon) avec les arrêts correspondants
        for (ShapePoint point : shapePoints) {
            // Chercher s'il y a un arrêt à cette séquence
            StopTime correspondingStopTime = stopTimes.stream()
                    .filter(stopTime -> stopTime.getStopSequence() == point.getSequence())
                    .findFirst()
                    .orElse(null);

            if (correspondingStopTime != null) {
                // Vérifier si StopLocation est une instance de Stop
                StopLocation stopLocation = correspondingStopTime.getStop();
                if (stopLocation instanceof Stop) {
                    Stop stop = (Stop) stopLocation;
                    // Afficher les informations d'arrêt avec les points de tracé
                    System.out.println("  Lat: " + point.getLat() + ", Lon: " + point.getLon() +
                                       ", Sequence: " + point.getSequence() + 
                                       ", Arrêt: " + stop.getName());
                } else {
                    // Si l'objet n'est pas un arrêt classique
                    System.out.println("  Lat: " + point.getLat() + ", Lon: " + point.getLon() +
                                       ", Sequence: " + point.getSequence() +
                                       ", Arrêt: inconnu (non Stop)");
                }
            } else {
                // Afficher les points de tracé sans arrêt
                System.out.println("  Lat: " + point.getLat() + ", Lon: " + point.getLon() + ", Sequence: " + point.getSequence());
            }
        }
    }
}


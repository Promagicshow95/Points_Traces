package gtfs;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class LesPointsEntreArret {

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

            // ID du trajet pour lequel nous voulons afficher les points de tracé
            String tripId = "1965630"; // Exemple d'ID de trajet

            // Afficher les points de tracé pour le trip donné
            displayShapePointsForTrip(store, tripId);

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

    // Afficher les points de tracé pour un trip donné
    private static void displayShapePointsForTrip(GtfsDaoImpl store, String tripId) {
        // Trouver le trajet (Trip) par son ID
        Trip trip = store.getAllTrips().stream()
                .filter(t -> t.getId().getId().equals(tripId))
                .findFirst()
                .orElse(null);

        if (trip == null) {
            System.out.println("Trip non trouvé avec l'ID " + tripId);
            return;
        }

        // Récupérer le shape ID associé au trajet
        String shapeId = trip.getShapeId().getId();

        // Récupérer les points de tracé associés à ce shape ID
        List<ShapePoint> shapePoints = store.getAllShapePoints().stream()
                .filter(shapePoint -> shapePoint.getShapeId().getId().equals(shapeId))
                .sorted((sp1, sp2) -> Integer.compare(sp1.getSequence(), sp2.getSequence())) // Trier par séquence
                .collect(Collectors.toList());

        // Afficher les points de tracé (lat/lon)
        System.out.println("Points de tracé pour le trajet " + tripId + " (Shape ID: " + shapeId + "):");
        for (ShapePoint point : shapePoints) {
            System.out.println("  Lat: " + point.getLat() + ", Lon: " + point.getLon() + ", Sequence: " + point.getSequence());
        }
    }
}

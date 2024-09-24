package gtfs;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.model.*;

import java.io.File;
import java.util.Collection;

public class GTFSAnalyzer {

    public static void main(String[] args) {
        try {
            // Initialiser GtfsReader pour lire les fichiers GTFS
            File gtfsDirectory = new File("src/main/resources/tisseo_gtfs_v2");
            GtfsReader reader = new GtfsReader();
            reader.setInputLocation(gtfsDirectory);

            // Utiliser GtfsDaoImpl pour stocker les entités lues
            GtfsDaoImpl store = new GtfsDaoImpl();
            reader.setEntityStore(store);

            // Lire les fichiers GTFS
            reader.run();

            // Accéder aux entités GTFS depuis GtfsDaoImpl (lecture seule)
            Collection<Agency> agencies = store.getAllAgencies();
            Collection<Route> routes = store.getAllRoutes();
            Collection<Stop> stops = store.getAllStops();
            Collection<Trip> trips = store.getAllTrips();

            // Afficher les agences
            System.out.println("Agences:");
            for (Agency agency : agencies) {
                System.out.println("Agency ID: " + agency.getId() + ", Name: " + agency.getName());
            }

            // Afficher les routes
            System.out.println("\nRoutes:");
            for (Route route : routes) {
                System.out.println("Route ID: " + route.getId() + ", Route Name: " + route.getLongName());
            }

            // Afficher les arrêts
            System.out.println("\nArrêts:");
            for (Stop stop : stops) {
                System.out.println("Stop ID: " + stop.getId() + ", Stop Name: " + stop.getName());
            }

            // Afficher les trajets
            System.out.println("\nTrajets:");
            for (Trip trip : trips) {
                System.out.println("Trip ID: " + trip.getId() + ", Route ID: " + trip.getRoute().getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

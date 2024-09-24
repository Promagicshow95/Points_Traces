package gtfs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.GtfsReader;

import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.List;

public class GTFSAnalyser_vers1 {

    public static void main(String[] args) {
        try {
            // Chemin du répertoire contenant les fichiers GTFS
            File gtfsDirectory = new File("src/main/resources/hanoi_gtfs_pm");  // Modifie le chemin selon ton emplacement
            GtfsReader reader = new GtfsReader();
            reader.setInputLocation(gtfsDirectory);

            // Utiliser GtfsDaoImpl pour stocker les entités lues
            GtfsDaoImpl dao = new GtfsDaoImpl();
            reader.setEntityStore(dao);

            // Lire les fichiers GTFS
            reader.run();

            // Accéder aux entités GTFS
            Collection<Agency> agencies = dao.getAllAgencies();
            Collection<Route> routes = dao.getAllRoutes();
            Collection<Stop> stops = dao.getAllStops();
            Collection<Trip> trips = dao.getAllTrips();
            Collection<StopTime> stopTimes = dao.getAllStopTimes();

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

            // Afficher les trajets et leurs arrêts
            System.out.println("\nTrajets et arrêts:");
            for (Trip trip : trips) {
                System.out.println("Trip ID: " + trip.getId() + ", Route ID: " + trip.getRoute().getId());

                // Trouver les arrêts pour ce trajet
                List<StopTime> tripStopTimes = getStopTimesForTrip(stopTimes, trip);
                for (StopTime stopTime : tripStopTimes) {
                	Stop stop = (Stop) stopTime.getStop();
                    int arrivalTimeInSeconds = stopTime.getArrivalTime();
                    int departureTimeInSeconds = stopTime.getDepartureTime();
                    System.out.println(" - Stop: " + stop.getName() + " Arrival: " + formatTime(arrivalTimeInSeconds) + " Departure: " + formatTime(departureTimeInSeconds));
                }

                // Calculer la durée du trajet
                if (!tripStopTimes.isEmpty()) {
                    StopTime firstStop = tripStopTimes.get(0);
                    StopTime lastStop = tripStopTimes.get(tripStopTimes.size() - 1);
                    int duration = lastStop.getArrivalTime() - firstStop.getDepartureTime();
                    System.out.println("  -> Durée du trajet: " + duration + " secondes");
                }
            }

            // Générer un graphique des trajets par ligne
            generateRouteTripChart(dao);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Générer un graphique des trajets par ligne
     */
    private static void generateRouteTripChart(GtfsDaoImpl dao) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Collection<Route> routes = dao.getAllRoutes();
        Collection<Trip> trips = dao.getAllTrips();

        for (Route route : routes) {
            // Utiliser l'ID de la route si le nom court est null
            String routeName = (route.getShortName() != null) ? route.getShortName() : "Route ID: " + route.getId();
            
            // Filtrer les trajets qui appartiennent à cette route
            long tripCount = trips.stream().filter(trip -> trip.getRoute().equals(route)).count();
            
            // Ajouter la valeur au dataset
            dataset.addValue(tripCount, "Number of Trips", routeName);
        }

        // Créer le graphique à barres
        JFreeChart barChart = ChartFactory.createBarChart(
                "Number of Trips per Route",
                "Route",
                "Number of Trips",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Afficher le graphique dans une fenêtre
        ChartPanel chartPanel = new ChartPanel(barChart);
        JFrame frame = new JFrame("Route Trip Analysis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }



    /**
     * Convertir les secondes en format hh:mm:ss
     */
    private static String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Filtrer les StopTimes pour un trajet spécifique
     */
    private static List<StopTime> getStopTimesForTrip(Collection<StopTime> stopTimes, Trip trip) {
        return stopTimes.stream().filter(st -> st.getTrip().equals(trip)).toList();
    }
}

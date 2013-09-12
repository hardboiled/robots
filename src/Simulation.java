/**
 * User: daniel
 * Date: 9/8/13
 */
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Simulation {
    private final static int ROBOT1_ID = 5937;
    private final static int ROBOT2_ID = 6043;
    public static void main(String[] args) {
        if (args.length > 0) redirectOutput(args[0]);
        final ArrayList<TunnelPoint> tps = readTunnelPoints();
        final LinkedList<RoutePoint> rps1 = readRoutePoints(ROBOT1_ID);
        final LinkedList<RoutePoint> rps2 = readRoutePoints(ROBOT2_ID);

        Dispatcher d = new Dispatcher(new ArrayList<Robot>(){{
            add(new Robot(ROBOT1_ID, tps));
            add(new Robot(ROBOT2_ID, tps));
        }}, new HashMap<Integer, LinkedList<RoutePoint>>() {{
            put(ROBOT1_ID, rps1);
            put(ROBOT2_ID, rps2);
        }});
        d.startDispatching();
    }
    private static ArrayList<TunnelPoint> readTunnelPoints() {
        ArrayList<TunnelPoint> tps = new ArrayList<TunnelPoint>();
        TunnelPoint tp;
        try {
            CsvSimulationReader csvTp = new CsvSimulationReader("res/tube.csv");
            while ((tp = csvTp.readTunnelPoint()) != null) {
                tps.add(tp);
            }
            csvTp.close();
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return tps;
    }
    private static LinkedList<RoutePoint> readRoutePoints(Integer id) {
        RoutePoint rp;
        LinkedList<RoutePoint> rps = new LinkedList<RoutePoint>();
        try{
            final CsvSimulationReader csvRp = new CsvSimulationReader("res/" +id +".csv");
            while ((rp = csvRp.readRoutePoint()) != null) {
                rps.add(rp);
            }
            csvRp.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return rps;
    }
    private static void redirectOutput(String filename) {
        try {
            System.setOut(new PrintStream(filename));
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}

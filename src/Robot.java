import java.lang.Runnable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

enum ETrafficConditions {
    Light,
    Moderate,
    Heavy,
    MaxLength //add all other traffic values before this.
}

public class Robot implements Runnable {
    private final double MAX_METERS = 350.0d;
    private final String ARBITRARY_SPEED = "30 km/hr.";
    private RoutePoint _curPt;
    private int _id;
    private ArrayList<RoutePoint> _rps;
    private List<TunnelPoint> _tps;
    private boolean _isShutdown;
    private LinkedBlockingQueue<RoutePoint> _rpQueue;
    private LinkedBlockingQueue<String> _msgQueue;

    public Robot(int id, List<TunnelPoint> tps){
        _id = id;
        _tps = tps;
        _isShutdown = false;
        _rpQueue= new LinkedBlockingQueue<RoutePoint>(10);
        _msgQueue = new LinkedBlockingQueue<String>(10);
        _rps = new ArrayList<RoutePoint>();
    }
    public int getId() { return _id;}
    public LinkedBlockingQueue<String> getMessageQueue() { return _msgQueue; }
    public LinkedBlockingQueue<RoutePoint> getPointsQueue() { return _rpQueue; }
    public void run(){
        while (isRunning()) {
            if(advanceToNextPoint()) {
                List<TunnelPoint> trafficPoints = getTrafficPoints();
                if (trafficPoints != null) {
                    for(TunnelPoint tp : trafficPoints) {
                        ETrafficConditions condition = generateRandomTrafficCondition();
                        System.out.println(_id + " " + _curPt.date + ":" + tp.description + " is experiencing " +
                                            condition + " traffic at speed " + ARBITRARY_SPEED);
                    }
                }
            } else {
                getMorePoints();
            }
            respondToDispatchMsg();
        }
        System.out.println(_id + " exiting.");
    }
    public boolean isRunning() {
        return !_isShutdown;
    }
    private void getMorePoints() {
        try{
            while(_rps.size() < 10) {
                RoutePoint rp = _rpQueue.poll();
                if (rp != null) {
                    _rps.add(rp);
                } else {
                    break;
                }
            }
        } catch (Exception ex) {}
    }
    private void respondToDispatchMsg() {
        try {
            String msg;
            if((msg = _msgQueue.poll()) != null) {
                if(msg.contentEquals("SHUTDOWN")) {
                    _isShutdown = true;
                    System.out.println(_id + " received SHUTDOWN message.");
                }
            }
        } catch (Exception ex){}
    }
    private ETrafficConditions generateRandomTrafficCondition() {
        int scale = (ETrafficConditions.MaxLength.ordinal() - 1);
        return ETrafficConditions.values()[(int)Math.round(Math.random() * scale)];
    }
    private ArrayList<TunnelPoint> getTrafficPoints() {
        ArrayList<TunnelPoint> tps = new ArrayList<TunnelPoint>();
        synchronized(_tps) {
            for (TunnelPoint tp : _tps) {
                double meters = distanceInMeters(_curPt.lat, _curPt.lon, tp.lat, tp.lon);
                if (meters <= MAX_METERS) {
                    tps.add(tp);
                }
            }
        }
        return (tps.size() > 0) ? tps : null;
    }
    private boolean advanceToNextPoint() {
        if (_rps.size() > 0) {
            try {
                Thread.sleep(3); //simulate delay for moving
            } catch (Exception ex) {}
            _curPt = _rps.remove(0);
            return true;
        }
        return false;
    }
    private static double distanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        //http://stackoverflow.com/questions/120283/working-with-latitude-longitude-values-in-java#answer-9822531
        double a = 6378137, b = 6356752.314245, f = 1 / 298.257223563; // WGS-84 ellipsoid params
        double L = Math.toRadians(lon2 - lon1);
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double sinLambda, cosLambda, sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
        double lambda = L, lambdaP, iterLimit = 100;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0)
                return 0; // co-incident points
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM))
                cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (§6)
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha
                    * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0)
            return Double.NaN; // formula failed to converge

        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B
                * sinSigma
                * (cos2SigmaM + B
                / 4
                * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
                * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        double dist = b * A * (sigma - deltaSigma);

        return dist;
    }

}
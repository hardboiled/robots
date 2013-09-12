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
    private final int ARBITRARY_SPEED = 80;
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
                                            condition + " traffic at speed " + getRandomSpeed(condition));
                    }
                } else {
                    System.out.println(_id + " no tube found near current point: " + _curPt.lat + " " + _curPt.lon);
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
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
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
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    private ETrafficConditions generateRandomTrafficCondition() {
        int scale = (ETrafficConditions.MaxLength.ordinal() - 1);
        return ETrafficConditions.values()[(int)Math.round(Math.random() * scale)];
    }
    private String getRandomSpeed(ETrafficConditions type) {
        int scale = type.ordinal() + 1;
        return String.format("%.2f km/hr", (ARBITRARY_SPEED * Math.random() / scale));
    }
    private ArrayList<TunnelPoint> getTrafficPoints() {
        ArrayList<TunnelPoint> tps = new ArrayList<TunnelPoint>();
        synchronized(_tps) {
            for (TunnelPoint tp : _tps) {
                double meters = Calculate.distanceInMeters(_curPt.lat, _curPt.lon, tp.lat, tp.lon);
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
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            _curPt = _rps.remove(0);
            return true;
        }
        return false;
    }
}

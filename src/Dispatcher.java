import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Dispatcher {
    private List<Robot> _robots;
    private HashMap<Integer, LinkedList<RoutePoint>> _rpMap;

    Dispatcher(List<Robot> list, HashMap<Integer, LinkedList<RoutePoint>> rpMap) {
        _robots = list;
        _rpMap = rpMap;
        for (Robot r : _robots) {
            new Thread(r, "" + r.getId()).start();
        }
    }
    public void startDispatching() {
        while(robotsAreRunning()) {
            for(Robot r : _robots) {
                if(!r.isRunning()) continue;
                LinkedBlockingQueue<RoutePoint> rpQ = r.getPointsQueue();
                int cap = rpQ.remainingCapacity();
                boolean sendShutdown = false;
                while(cap-- > 0 && rpQ.remainingCapacity() > 0) {
                    try {
                        RoutePoint rp = _rpMap.get(r.getId()).remove();
                        if (isBeforeShutdownTime(rp.date.toString())) {
                            rpQ.put(rp);
                        } else {
                            sendShutdown = true;
                            break;
                        }
                    } catch (Exception ex) {}
                }
                if (sendShutdown) {
                    while (!rpQ.isEmpty()); //wait until done with existing points
                    sendShutdown(r);
                }
            }
        }
    }
    private boolean robotsAreRunning() {
       boolean areRobotsRunning = false;
       for(Robot r : _robots) {
           if (r.isRunning()) {
               areRobotsRunning = true;
               break;
           }
       }
       return areRobotsRunning;
    }
    private boolean isBeforeShutdownTime(String dateStr) {
        return dateStr.matches("^.+(0[01234567]:\\d{2}|08:0\\d):\\d{2}.+");
    }
    private void sendShutdown(Robot r) {
        LinkedBlockingQueue<String> q = r.getMessageQueue();
        try {
            q.put("SHUTDOWN");
        } catch(Exception ex) {}
    }
}

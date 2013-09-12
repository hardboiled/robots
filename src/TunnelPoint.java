/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 9/8/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
import java.util.Comparator;

public class TunnelPoint implements Comparable<TunnelPoint> {
    public String description;
    public double lat;
    public double lon;
    public TunnelPoint(String params[]){
        this.description = params[0];
        this.lat = Double.parseDouble(params[1]);
        this.lon = Double.parseDouble(params[2]);
    }
    public int compareTo(TunnelPoint tp) {
        return (this.lat > tp.lat) ? 1 : -1;
    }
    public static Comparator<TunnelPoint> TunnelPointComparator
            = new Comparator<TunnelPoint>() {

        public int compare(TunnelPoint tp1, TunnelPoint tp2) {
            return tp1.compareTo(tp2);
        }

    };
}

/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 9/8/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
import java.util.Comparator;

public class TunnelPoint {
    public String description;
    public double lat;
    public double lon;
    public TunnelPoint(String params[]){
        this.description = params[0];
        this.lat = Double.parseDouble(params[1]);
        this.lon = Double.parseDouble(params[2]);
    }
}

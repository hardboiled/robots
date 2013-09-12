/**
 * Created with IntelliJ IDEA.
 * User: daniel
 * Date: 9/8/13
 * Time: 6:04 PM
 * To change this template use File | Settings | File Templates.
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RoutePoint {
    public int driverId;
    public double lat;
    public double lon;
    public Date date;
    public RoutePoint(String[] params) throws ParseException {
        this.driverId = Integer.parseInt(params[0]);
        this.lat = Double.parseDouble(params[1]);
        this.lon = Double.parseDouble(params[2]);
        this.date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(params[3]);
    }
}

import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;

public class CsvSimulationReader extends BufferedReader {
    public CsvSimulationReader(String path) throws FileNotFoundException {
        super(new InputStreamReader(new FileInputStream(path), Charset.forName("UTF-8")));
    }
    public RoutePoint readRoutePoint() throws IOException, ParseException {
        String[] params = this.readAndSplitLine();
        if (params != null && params.length > 3) {
            return new RoutePoint(params);
        }
        return null;
    }
    public TunnelPoint readTunnelPoint() throws IOException {
        String[] params = this.readAndSplitLine();
        if (params != null && params.length > 2) {
            return new TunnelPoint(params);
        }
        return null;
    }
    private String[] readAndSplitLine() throws IOException {
        String line = this.readLine();
        String[] params = (line != null && !line.isEmpty()) ?
            line.split(",") : null;
        if (params != null) {
            for (int i = 0; i < params.length; ++i) {
                params[i] = params[i].replace("\"", "");
            }
        }
        return params;
    }
}

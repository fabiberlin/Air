package baensch.de.airlocator;
import java.io.InputStream;

/**
 * Created by fabi on 05.03.17.
 */

public class HttpResultHelper {
    private int statusCode;
    private InputStream response;

    public HttpResultHelper() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public InputStream getResponse() {
        return response;
    }

    public void setResponse(InputStream response) {
        this.response = response;
    }
}

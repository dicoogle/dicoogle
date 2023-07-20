package pt.ua.dicoogle.server.web.servlets.mlprovider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.core.mlprovider.DatastoreRequest;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DatastoreServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DatastoreServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String dataString = IOUtils.toString(req.getReader());
        if (dataString == null) {
            resp.sendError(404, "Empty POST body");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        DatastoreRequest datasetRequest;
        try {
            datasetRequest = mapper.readValue(dataString, DatastoreRequest.class);
            PluginController.getInstance().datastore(datasetRequest);
        } catch (Exception e) {
            log.error("Error parsing json string", e);
            ResponseUtil.sendError(resp, Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Malformed request");
        }
    }
}

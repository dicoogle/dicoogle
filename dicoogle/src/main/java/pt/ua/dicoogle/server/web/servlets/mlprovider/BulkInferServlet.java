package pt.ua.dicoogle.server.web.servlets.mlprovider;

import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.server.web.utils.ResponseUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BulkInferServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(BulkInferServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponseUtil.sendError(resp, Status.SERVER_ERROR_NOT_IMPLEMENTED.getCode(), "Endpoint not implemented");
    }
}

package pt.ua.dicoogle.server.web.servlets.mlprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.data.Status;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.mlprovider.MLModelTrainInfo;
import pt.ua.dicoogle.sdk.mlprovider.MLProviderInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This servlet returns a detailed list of metrics described in @see pt.ua.dicoogle.sdk.mlprovider.MLModelTrainInfo.
 * Usually it should only be available when a model has already been trained at least once.
 * Model is identified by the provider and model ID.
 */
public class ModelinfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        String provider = request.getParameter("provider");
        String modelID = request.getParameter("modelID");

        if(provider == null || provider.isEmpty()){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Provider was not specified");
            return;
        }

        if(modelID == null || modelID.isEmpty()){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Model was not specified");
            return;
        }

        MLProviderInterface mlPlugin = PluginController.getInstance().getMachineLearningProviderByName(provider, true);
        if(mlPlugin == null){
            response.sendError(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Provider not found");
            return;
        }

        MLModelTrainInfo info = mlPlugin.modelInfo(modelID);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, info);
        out.close();
        out.flush();
    }

}

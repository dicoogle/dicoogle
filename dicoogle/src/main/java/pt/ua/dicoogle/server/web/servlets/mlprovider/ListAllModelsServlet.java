package pt.ua.dicoogle.server.web.servlets.mlprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.sdk.mlprovider.MLProvider;
import pt.ua.dicoogle.sdk.mlprovider.MLProviderInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This servlet lists all models from all providers.
 * Optionally, if a provider is specified, it will only list models from that provider.
 */
public class ListAllModelsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        String provider = request.getParameter("provider");
        List<MLProvider> providersResponse = new ArrayList<>();

        if(provider != null && !provider.isEmpty()){
            MLProviderInterface mlPlugin = PluginController.getInstance().getMachineLearningProviderByName(provider, true);
            MLProvider p = new MLProvider(mlPlugin.getName());
            p.setModels(mlPlugin.listModels());
            providersResponse.add(p);
        } else {
            Iterable<MLProviderInterface> providers = PluginController.getInstance().getMLPlugins(true);
            providers.forEach((mlPlugin) -> {
                MLProvider p = new MLProvider(mlPlugin.getName());
                p.setModels(mlPlugin.listModels());
                providersResponse.add(p);
            });
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, providersResponse);
        out.close();
        out.flush();
    }

}

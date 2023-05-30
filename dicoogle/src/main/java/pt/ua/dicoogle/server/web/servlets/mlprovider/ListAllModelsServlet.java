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

public class ListAllModelsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        Iterable<MLProviderInterface> providers = PluginController.getInstance().getMLPlugins(true);

        List<MLProvider> providersResponse = new ArrayList<>();

        providers.forEach((mlPlugin) -> {
            MLProvider provider = new MLProvider(mlPlugin.getName());
            provider.setModels(mlPlugin.listModels());
            providersResponse.add(provider);
        });

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, providersResponse);
        out.close();
        out.flush();
    }

}

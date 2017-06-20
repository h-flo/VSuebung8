package de.othr.vs.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.othr.vs.data.entity.Student;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.ws.rs.ext.RuntimeDelegate;
import org.glassfish.jersey.server.ResourceConfig;

public class ServerREST {
    
    private final static Config cfg = new Config();
    private static HazelcastInstance instance;
    public static Map<Integer, Student> mapCustomers;
   
    
    public static void main(String[] args) throws IllegalArgumentException, IOException, URISyntaxException {
        
        // JAX-RS bzw. Jersey konfigurieren...
        ResourceConfig config = new ResourceConfig();
        //config.register(PruefungsleistungResource.class);
        // ... indem lediglich Klasse(n) registriert werden, die @Path-Annotationen haben (hier nur eine)
        config.register(StudentResource.class);
        instance = Hazelcast.newHazelcastInstance(cfg);
        mapCustomers = instance.getMap("students");

        // Webserver für Port 8080 generieren (Teil des JDK!)
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // Handler erzeugen (Handler = Objekt, das Request bekommt und Response erzeugt)
        HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(config, HttpHandler.class);
        // Webserver den Handler (oben) einem bestimmten Kontext zuordnen
        // Der Handler ist jetzt zuständig für alle Requests, die mit http://localhost:8080/webresources beginnen
        server.createContext("/webresources", handler);
        // Webserver starten...
        server.start();

        // Dialog anzeigen (blockierend)
        JOptionPane.showMessageDialog(null, "Server stoppen...");
        server.stop(0);
    }
}

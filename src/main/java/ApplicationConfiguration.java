import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class ApplicationConfiguration extends Configuration {

    @JsonProperty
    private ServerConfiguration server;

    public ServerConfiguration getServer() {
        return server;
    }
}
class ServerConfiguration {

    @JsonProperty
    private ConnectorConfiguration applicationConnectors;

    @JsonProperty
    private ConnectorConfiguration adminConnectors;

    public ConnectorConfiguration getApplicationConnectors() {
        return applicationConnectors;
    }

    public ConnectorConfiguration getAdminConnectors() {
        return adminConnectors;
    }
}
class ConnectorConfiguration {

    @JsonProperty
    private String type;

    @JsonProperty
    private int port;

    public String getType() {
        return type;
    }

    public int getPort() {
        return port;
    }
}

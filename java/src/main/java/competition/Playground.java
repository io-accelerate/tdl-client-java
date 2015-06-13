package competition;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class Playground {

    public static void main(String[] args) throws IOException {
        JMXServiceURL url =
                new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:2011/jmxrmi");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
    }
}

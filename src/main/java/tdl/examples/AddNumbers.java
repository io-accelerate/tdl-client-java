package tdl.examples;

import tdl.client.Client;
import tdl.client.abstractions.ImplementationMap;

/**
 * Created by julianghionoiu on 11/06/2015.
 */
public class AddNumbers {

    public static void main(String[] args) throws Exception {
        Client client = new Client("localhost", 21616, "test");

        ImplementationMap implementations = new ImplementationMap();
        implementations.register("sum", params -> {
            Integer x = Integer.parseInt(params[0]);
            Integer y = Integer.parseInt(params[1]);
            return x + y;
        });

        client.goLiveWith(implementations);
    }
}

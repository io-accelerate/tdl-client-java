package competition.example;

import competition.client.Client;

/**
 * Created by julianghionoiu on 11/06/2015.
 */
public class AddNumbersExample {

    public static void main(String[] args) throws Exception {
        Client client = new Client("tcp://localhost:21616", "your_user");

        client.goLiveWith(params -> {
            Integer x = Integer.parseInt(params[0]);
            Integer y = Integer.parseInt(params[1]);
            return x + y;
        });
    }
}

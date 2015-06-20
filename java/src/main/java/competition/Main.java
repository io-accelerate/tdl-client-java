package competition;

/**
 * Created by julianghionoiu on 11/06/2015.
 */
public class Main {


    public static void main(String[] args) throws Exception {
        Client client = new Client("tcp://localhost:21616", "jgh");

        client.goLiveWith(serializedParams -> {
            String[] params = serializedParams.split(", ");
            Integer x = Integer.parseInt(params[0]);
            Integer y = Integer.parseInt(params[1]);
            return x + y;
        });
    }
}

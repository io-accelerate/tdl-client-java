package competition;

/**
 * Created by julianghionoiu on 11/06/2015.
 */
public class Main {


    public static void main(String[] args) throws Exception {
        new Client("tcp://localhost:21616", "jgh").goLiveWith((String serializedParam) -> {
            Integer param = Integer.parseInt(serializedParam);
            return param + 1;
        });

    }
}

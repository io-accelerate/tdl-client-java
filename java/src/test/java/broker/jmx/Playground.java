package broker.jmx;

import java.util.List;

/**
 * Created by julianghionoiu on 13/06/2015.
 */
public class Playground {

    public static void main(String[] args) throws Exception {
        RemoteJmxBroker broker = RemoteJmxBroker.connect("localhost", 20011, "TEST.BROKER");

        RemoteJmxQueue queue = broker.addQueue("test.queue");
        queue.sendTextMessage("ole!");
        System.out.println(queue.getSize());

        List<String> messageContents = queue.getMessageContents();
        for (String messageContent : messageContents) {
            System.out.println("> "+messageContent);
        }

        queue.purge();
        System.out.println(queue.getSize());

        broker.removeQueue("test.queue");
    }

}

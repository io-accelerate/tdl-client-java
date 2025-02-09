package acceptance.queue;

import io.accelerate.client.queue.ImplementationRunner;

public class QuietImplementationRunner implements ImplementationRunner {
    @Override
    public void run() {
        // do nothing
    }
}

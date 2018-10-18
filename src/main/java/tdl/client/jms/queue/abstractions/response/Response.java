package tdl.client.jms.queue.abstractions.response;

import audit.Auditable;

/**
 * Created by julianghionoiu on 17/01/2016.
 */
public interface Response extends Auditable {
    String getId();

    Object getResult();
}

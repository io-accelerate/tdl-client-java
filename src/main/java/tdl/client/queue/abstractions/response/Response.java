package tdl.client.queue.abstractions.response;

import tdl.client.audit.Auditable;

/**
 * Created by julianghionoiu on 17/01/2016.
 */
public interface Response extends Auditable {
    String getId();

    Object getResult();
}

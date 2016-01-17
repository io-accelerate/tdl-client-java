package tdl.client.abstractions.response;

import tdl.client.actions.ClientAction;
import tdl.client.audit.Auditable;

/**
 * Created by julianghionoiu on 17/01/2016.
 */
public interface Response extends Auditable {
    String getId();

    Object getResult();

    ClientAction getClientAction();
}

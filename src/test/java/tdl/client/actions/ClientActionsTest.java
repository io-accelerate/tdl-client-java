package tdl.client.actions;

import org.junit.Test;
import tdl.client.ClientSteps;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;

/**
 * Created by julianghionoiu on 31/01/2016.
 */
public class ClientActionsTest {

    @Test
    public void should_satisfy_contract_for_utility_class() {
        assertUtilityClassWellDefined(ClientActions.class);
    }


}
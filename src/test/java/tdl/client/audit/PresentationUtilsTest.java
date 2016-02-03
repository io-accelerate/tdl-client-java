package tdl.client.audit;

import org.junit.Test;
import tdl.client.actions.ClientActions;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;

/**
 * Created by julianghionoiu on 03/02/2016.
 */
public class PresentationUtilsTest {

    @Test
    public void should_satisfy_contract_for_utility_class() {
        assertUtilityClassWellDefined(PresentationUtils.class);
    }


}
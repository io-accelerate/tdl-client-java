package io.accelerate.client.queue.abstractions;

import java.util.List;

/**
 * Created by julianghionoiu on 20/06/2015.
 */ //Obs: serializedParam can become Request and the returned object could be called Response
@FunctionalInterface
public interface UserImplementation {
    Object process(List<ParamAccessor> params);
}

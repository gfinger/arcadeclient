package org.makkiato.arcadeclient.data.repository;

import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import static org.makkiato.arcadeclient.data.repository.ArcadeclientQueryMethod.*;

public class ArcadeclientParameterAccessor extends ParametersParameterAccessor {
    /**
     * Creates a new {@link ParametersParameterAccessor}.
     *
     * @param parameters must not be {@literal null}.
     * @param values     must not be {@literal null}.
     */
    public ArcadeclientParameterAccessor(Parameters<ArcadeclientParameters, ArcadeclientParameter> parameters, Object[] values) {
        super(parameters, values);
    }

    @Override
    public Parameters<ArcadeclientParameters, ArcadeclientParameter> getParameters() {
        return (Parameters<ArcadeclientParameters, ArcadeclientParameter>) super.getParameters();
    }

    @Override
    public Object[] getValues() {
        return super.getValues();
    }
}

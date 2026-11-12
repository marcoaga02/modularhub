package com.marcoaga02.modularhub.config;

import com.marcoaga02.modularhub.core.spi.IdentityProviderService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class BaseITWithMockIdentity extends BaseIT {

    @MockitoBean
    protected IdentityProviderService identityProvider;
}

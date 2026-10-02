package com.marcoaga02.modularhub.config;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"mock-identity", "test"})
@Import(MockIdentityConfig.class)
public abstract class BaseITWithMockIdentity extends BaseIT {}

package com.marcoaga02.modularhub.core.spi;

import com.marcoaga02.modularhub.core.api.dto.AccountProfileDTO;

public interface AccountDetailsProvider {

    AccountProfileDTO getDetails(String identityId);

}

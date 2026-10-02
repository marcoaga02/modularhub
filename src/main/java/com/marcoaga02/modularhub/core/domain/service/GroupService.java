package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    private final IdentityService identityService;

    public GroupService(IdentityService identityService) {
        this.identityService = identityService;
    }

    public List<IdentityGroupDTO> getAllGroups() {
        return this.identityService.getGroups();
    }
}

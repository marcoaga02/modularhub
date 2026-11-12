package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.openapi.GroupApi;
import com.marcoaga02.modularhub.core.domain.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GroupController implements GroupApi {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public ResponseEntity<List<IdentityGroupDTO>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }
}

package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private GroupService groupService;

    @Test
    void getAllGroups_shouldReturnGroupsFromIdentityService() {
        List<IdentityGroupDTO> groups = List.of(
                new IdentityGroupDTO().id("group-1").name("first"),
                new IdentityGroupDTO().id("group-2").name("second"));
        when(identityService.getGroups()).thenReturn(groups);

        List<IdentityGroupDTO> result = groupService.getAllGroups();

        assertThat(result).isSameAs(groups);
        verify(identityService).getGroups();
    }

    @Test
    void getAllGroups_shouldReturnEmptyList_whenNoGroupExists() {
        when(identityService.getGroups()).thenReturn(List.of());

        assertThat(groupService.getAllGroups()).isEmpty();

        verify(identityService).getGroups();
    }
}

package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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
    void getAllGroups_shouldReturnEmptyList_whenNoGroupIsPresent() {
        when(identityService.getGroups()).thenReturn(Collections.emptyList());

        List<IdentityGroupDTO> result = groupService.getAllGroups();

        assertThat(result).isEmpty();

        verify(identityService).getGroups();
    }

    @Test
    void getAllGroups_shouldReturnGroup_whenOneGroupIsPresent() {
        IdentityGroupDTO groupDto = createIdentityGroupDTO("group-id1", "first group", "first group description");

        when(identityService.getGroups()).thenReturn(List.of(groupDto));

        List<IdentityGroupDTO> result = groupService.getAllGroups();

        assertThat(result)
                .hasSize(1)
                .containsExactly(groupDto);

        verify(identityService).getGroups();
    }

    @Test
    void getAllGroups_shouldAllReturnGroups_whenMultipleGroupsArePresent() {
        IdentityGroupDTO firstGroupDto = createIdentityGroupDTO("group-id1", "first group", "first group description");
        IdentityGroupDTO secondGroupDto = createIdentityGroupDTO("group-id2", "second group",  "second group description");


        when(identityService.getGroups()).thenReturn(List.of(firstGroupDto, secondGroupDto));

        List<IdentityGroupDTO> result = groupService.getAllGroups();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(firstGroupDto, secondGroupDto);

        verify(identityService).getGroups();
    }

    private IdentityGroupDTO createIdentityGroupDTO(
            String id,
            String name,
            String description
    ) {
        IdentityGroupDTO groupDTO = new IdentityGroupDTO();
        groupDTO.setId(id);
        groupDTO.setName(name);
        groupDTO.setDescription(description);

        return groupDTO;
    }

}
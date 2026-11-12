package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.PluginDTO;
import com.marcoaga02.modularhub.core.domain.service.PluginRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PluginRegistryController.class)
class PluginRegistryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PluginRegistryService pluginRegistryService;

    @Test
    void getAuthorizedPlugins_shouldReturnOkWithPlugins_whenUserIsAuthorized() throws Exception {
        PluginDTO plugin = new PluginDTO()
                .path("user-management")
                .name("plugin.name")
                .description("plugin.description")
                .icon("pi pi-users");
        when(pluginRegistryService.getAuthorizedPlugins()).thenReturn(List.of(plugin));

        mockMvc.perform(get("/plugins"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].path").value("user-management"))
                .andExpect(jsonPath("$[0].name").value("plugin.name"))
                .andExpect(jsonPath("$[0].description").value("plugin.description"))
                .andExpect(jsonPath("$[0].icon").value("pi pi-users"));
    }

    @Test
    void getAuthorizedPlugins_shouldReturnOkWithEmptyList_whenNoPluginIsAuthorized() throws Exception {
        when(pluginRegistryService.getAuthorizedPlugins()).thenReturn(List.of());

        mockMvc.perform(get("/plugins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

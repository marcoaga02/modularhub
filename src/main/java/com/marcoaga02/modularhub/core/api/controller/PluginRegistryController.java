package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.PluginDTO;
import com.marcoaga02.modularhub.core.api.openapi.PluginApi;
import com.marcoaga02.modularhub.core.domain.service.PluginRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PluginRegistryController implements PluginApi {

    private final PluginRegistryService pluginRegistryService;

    public PluginRegistryController(PluginRegistryService pluginRegistryService) {
        this.pluginRegistryService = pluginRegistryService;
    }

    @Override
    public ResponseEntity<List<PluginDTO>> getAuthorizedPlugins() {
        return ResponseEntity.ok(pluginRegistryService.getAuthorizedPlugins());
    }

}

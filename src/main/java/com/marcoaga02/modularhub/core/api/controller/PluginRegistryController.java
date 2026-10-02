package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.PluginDTO;
import com.marcoaga02.modularhub.core.domain.service.PluginRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/plugins")
public class PluginRegistryController {

    private final PluginRegistryService  pluginRegistryService;

    public PluginRegistryController(PluginRegistryService pluginRegistryService) {
        this.pluginRegistryService = pluginRegistryService;
    }

    @GetMapping
    public ResponseEntity<List<PluginDTO>> getAuthorizedPlugins() {
        return ResponseEntity.ok(pluginRegistryService.getAuthorizedPlugins());
    }

}

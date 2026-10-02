package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.ModulePlugin;
import com.marcoaga02.modularhub.core.api.dto.PluginDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PluginRegistryService {

    private final AccountService  accountService;
    private final List<ModulePlugin> plugins;

    public PluginRegistryService(AccountService accountService, List<ModulePlugin> plugins) {
        this.accountService = accountService;
        this.plugins = plugins;
    }

    public List<PluginDTO> getAuthorizedPlugins() {
        Set<String> userRoles = accountService.getRoles();

        return plugins.stream()
                .filter(p -> userRoles.containsAll(p.getRequiredRoles()))
                .map(p -> new PluginDTO(
                        p.getPath(),
                        p.getName(),
                        p.getDescription(),
                        p.getIcon()
                ))
                .toList();
    }

}

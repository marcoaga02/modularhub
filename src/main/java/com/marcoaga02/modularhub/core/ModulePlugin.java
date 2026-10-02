package com.marcoaga02.modularhub.core;

import java.util.List;

public interface ModulePlugin {

    String getPath();

    String getName();

    String getDescription();

    String getIcon();

    List<String> getRequiredRoles();

}

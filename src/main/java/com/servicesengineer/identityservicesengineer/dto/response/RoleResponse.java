package com.servicesengineer.identityservicesengineer.dto.response;

import lombok.Builder;

import java.util.Set;
@Builder
public class RoleResponse {
    private String name;
    private String description;
    private Set<PermissionResponse> permissions;

    public RoleResponse() {
    }

    public RoleResponse(String name, String description, Set<PermissionResponse> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = permissions;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<PermissionResponse> getPermissions() {
        return permissions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPermissions(Set<PermissionResponse> permissions) {
        this.permissions = permissions;
    }
}

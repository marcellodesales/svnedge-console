package com.collabnet.svnedge.teamforge;

public class TeamForgeConfig {
    private String baseUrl;
    
    public TeamForgeConfig(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getBaseUrl() {
	return this.baseUrl;
    }

    public String getWebAppUrl() {
	return getBaseUrl() + "/sf";
    }
}
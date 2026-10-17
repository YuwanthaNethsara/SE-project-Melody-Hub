package com.example.project1.security;

import com.example.project1.storage.FileStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves uploaded IMAGES (products, advertisements, artists, albums) as static files.
 * Music files are deliberately NOT mapped here: they are only available through
 * GET /api/digital-music/{id}/download, which requires a login.
 */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final FileStorageService storage;

    public UploadResourceConfig(FileStorageService storage) {
        this.storage = storage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (String folder : FileStorageService.IMAGE_FOLDERS) {
            String location = storage.getRoot().resolve(folder).toUri().toString();
            registry.addResourceHandler("/uploads/" + folder + "/**")
                    .addResourceLocations(location.endsWith("/") ? location : location + "/")
                    .setCachePeriod(3600);
        }
    }
}

package vn.iotstar.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.location:E:/upload}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/category/**")
                .addResourceLocations(
                        directoryLocation(uploadRoot.resolve("category")));

        registry.addResourceHandler("/product/**")
                .addResourceLocations(
                        directoryLocation(uploadRoot.resolve("product")));
    }

    private String directoryLocation(Path path) {
        String location = path.toUri().toString();
        if (location.endsWith("/")) {
            return location;
        }
        return location + "/";
    }
}

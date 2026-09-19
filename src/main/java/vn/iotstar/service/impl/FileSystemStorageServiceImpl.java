package vn.iotstar.service.impl;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.config.StorageProperties;
import vn.iotstar.exception.StorageException;
import vn.iotstar.service.IStorageService;

@Service
public class FileSystemStorageServiceImpl implements IStorageService {
    private final Path rootLocation;

    public FileSystemStorageServiceImpl(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation())
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String getSorageFilename(MultipartFile file, String id) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        return "p" + id + "." + ext;
    }

    @Override
    public void store(MultipartFile file, String storeFilename) {
        try {
            if (file == null || file.isEmpty()) {
                throw new StorageException("Failed to store empty file");
            }

            Path destinationFile = this.rootLocation
                    .resolve(Paths.get(storeFilename))
                    .normalize()
                    .toAbsolutePath();

            if (!destinationFile.startsWith(this.rootLocation)) {
                throw new StorageException("Cannot store file outside current directory");
            }

            Files.createDirectories(destinationFile.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StorageException("Failed to store file: " + storeFilename, ex);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            throw new StorageException("Can not read file: " + filename);
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StorageException("Could not read file: " + filename, ex);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public void delete(String storeFilename) throws Exception {
        if (storeFilename == null || storeFilename.isBlank()) {
            return;
        }

        Path destinationFile = rootLocation
                .resolve(Paths.get(storeFilename))
                .normalize()
                .toAbsolutePath();

        if (!destinationFile.startsWith(rootLocation)) {
            throw new StorageException("Cannot delete file outside current directory");
        }

        Files.deleteIfExists(destinationFile);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("category"));
            Files.createDirectories(rootLocation.resolve("product"));
            System.out.println(rootLocation.toString());
        } catch (Exception ex) {
            throw new StorageException("Could not create upload directory", ex);
        }
    }
}

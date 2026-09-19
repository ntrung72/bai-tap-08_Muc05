package vn.iotstar.controller.api;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.service.IStorageService;

@RestController
@RequestMapping("/api/uploads")
public class UploadApiController {
    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "category",
            "product");

    private final IStorageService storageService;

    public UploadApiController(IStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/{folder}")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable String folder,
            @RequestParam("file") MultipartFile file) {
        if (!ALLOWED_FOLDERS.contains(folder)) {
            return error("Thư mục tải ảnh không hợp lệ.");
        }

        if (file == null || file.isEmpty()) {
            return error("Vui lòng chọn ảnh cần tải lên.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return error("Tệp tải lên phải là hình ảnh.");
        }

        String filename = storageService.getSorageFilename(
                file,
                UUID.randomUUID().toString());
        String storeFilename = folder + "/" + filename;
        storageService.store(file, storeFilename);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "path", storeFilename));
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", message));
    }
}

package com.codex.intellilearn.service;

import com.codex.intellilearn.dto.common.CommonErrorResponse;
import com.codex.intellilearn.dto.common.CommonResponse;
import com.codex.intellilearn.repo.SubTopicRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final SubTopicRepo subTopicRepo;
    private final String UPLOAD_DIR = "D:/Projects/IntelliLearn/IntelliLearn Backend/src/main/resources/uploads/";
    @Value("${fastapi.url}")
    private String fastApiUrl;

    public CommonResponse<String> convertVideoToAudio(MultipartFile videoFile) {
        try {
            String videoFilePath = saveUploadedFile(videoFile);
            String audioUrl = callFastApiToConvertVideoToAudio(videoFilePath);

            if (audioUrl != null && !audioUrl.isEmpty()) {
                String audioFilePath = downloadAudio(audioUrl);
                String transcribeFilePath = getTranscript(audioFilePath);
                return new CommonResponse<>("Video converted to audio successfully.");
            } else {
                return new CommonResponse<>(new CommonErrorResponse(
                    new Date(),
                    "conversion_failed",
                    "Video conversion failed",
                    "Error converting video to audio."
                ));
            }
        } catch (Exception e) {
            return new CommonResponse<>(new CommonErrorResponse(
                new Date(),
                "internal_server_error",
                "Internal server error occurred",
                e.getMessage()
            ));
        }
    }

    private String getTranscript(String audioFilePath) {
        return null;
    }

    private String saveUploadedFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + ".mp4";
        Path uploadPath = Paths.get(UPLOAD_DIR + "/video");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return filePath.toString();
    }

    private String callFastApiToConvertVideoToAudio(String videoFilePath) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(videoFilePath));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return null;
        }
    }

    private String downloadAudio(String audioUrl) {
        audioUrl = audioUrl.replaceAll("\"", "");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(audioUrl, byte[].class);
        Path path = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                String fileName = UUID.randomUUID() + ".mp3";
                path = Paths.get(UPLOAD_DIR + "/audio", fileName);
                Files.write(path, Objects.requireNonNull(response.getBody()));
            } catch (IOException ignored) {

            }
        }
        return path == null ? "null" : path.toString();
    }

    public Resource loadFileAsResource(String fileName) {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
        Resource resource = null;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("File not found: " + fileName);
        }
    }
}

package com.codex.intellilearn.controller;

import com.codex.intellilearn.dto.common.CommonResponse;
import com.codex.intellilearn.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        CommonResponse<String> commonResponse = videoService.convertVideoToAudio(file, 1, "Class");
        if (commonResponse.getHasException()) {
            return ResponseEntity.internalServerError().body(commonResponse.getErrorResponse());
        }
        if (!commonResponse.getIsSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(commonResponse.getErrorResponse());
        }
        return ResponseEntity.ok().body(commonResponse.getResult());
    }
}

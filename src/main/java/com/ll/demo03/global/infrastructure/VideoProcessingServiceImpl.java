package com.ll.demo03.global.infrastructure;

import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.port.VideoProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class VideoProcessingServiceImpl implements VideoProcessingService {

    @Override
    public MultipartFile extractLatestFrameFromVideo(String videoUrl) {
        log.info("Starting latest frame extraction from video - URL: {}", videoUrl);
        
        Path tempVideoFile = null;
        Path frameImageFile = null;
        
        try {
            // Step 1: Download video from URL
            log.info("Step 1: Downloading video from URL...");
            tempVideoFile = downloadVideo(videoUrl);
            log.info("✅ Video download successful: {} bytes", Files.size(tempVideoFile));
            
            // Step 2: Extract last frame
            log.info("Step 2: Extracting latest frame...");
            frameImageFile = extractLatestFrame(tempVideoFile);
            log.info("✅ Latest frame extraction successful: {} bytes", Files.size(frameImageFile));
            
            // Step 3: Convert to MultipartFile
            log.info("Step 3: Converting to MultipartFile...");
            byte[] imageBytes = Files.readAllBytes(frameImageFile);
            MultipartFile multipartFile = new CustomMultipartFile(
                "latest_frame.jpg",
                "latest_frame.jpg", 
                "image/jpeg",
                imageBytes
            );
            log.info("✅ MultipartFile created successfully: {} bytes", multipartFile.getSize());
            
            return multipartFile;
            
        } catch (IOException e) {
            log.error("❌ IO Error during video processing: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (InterruptedException e) {
            log.error("❌ FFmpeg process interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("❌ Unexpected error during video processing: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            // Always clean up temporary files
            cleanupTempFiles(tempVideoFile, frameImageFile);
        }
    }

    @Override
    public MultipartFile extractFrameFromVideo(String videoUrl, double captureTimeSeconds) {
        log.info("Starting video frame extraction - URL: {}, captureTime: {}s", videoUrl, captureTimeSeconds);
        
        Path tempVideoFile = null;
        Path frameImageFile = null;
        
        try {
            // Step 1: Download video from URL
            log.info("Step 1: Downloading video from URL...");
            tempVideoFile = downloadVideo(videoUrl);
            log.info("✅ Video download successful: {} bytes", Files.size(tempVideoFile));
            
            // Step 2: Extract frame at specified time
            log.info("Step 2: Extracting frame at {}s...", captureTimeSeconds);
            frameImageFile = extractFrame(tempVideoFile, captureTimeSeconds);
            log.info("✅ Frame extraction successful: {} bytes", Files.size(frameImageFile));
            
            // Step 3: Convert to MultipartFile
            log.info("Step 3: Converting to MultipartFile...");
            byte[] imageBytes = Files.readAllBytes(frameImageFile);
            MultipartFile multipartFile = new CustomMultipartFile(
                "frame.jpg",
                "frame.jpg", 
                "image/jpeg",
                imageBytes
            );
            log.info("✅ MultipartFile created successfully: {} bytes", multipartFile.getSize());
            
            return multipartFile;
            
        } catch (IOException e) {
            log.error("❌ IO Error during video processing: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (InterruptedException e) {
            log.error("❌ FFmpeg process interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("❌ Unexpected error during video processing: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            // Always clean up temporary files
            cleanupTempFiles(tempVideoFile, frameImageFile);
        }
    }
    
    private Path downloadVideo(String videoUrl) throws IOException {
        log.info("🔄 Downloading video from URL: {}", videoUrl);
        
        try {
            URL url = new URL(videoUrl);
            Path tempFile = Files.createTempFile("video_" + UUID.randomUUID(), ".mp4");
            log.info("📁 Created temp file: {}", tempFile);
            
            try (InputStream inputStream = url.openStream()) {
                long bytesDownloaded = Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                log.info("✅ Video downloaded successfully: {} bytes to {}", bytesDownloaded, tempFile);
                return tempFile;
            }
        } catch (IOException e) {
            log.error("❌ Failed to download video from URL: {} - Error: {}", videoUrl, e.getMessage());
            throw e;
        }
    }
    
    private Path extractLatestFrame(Path videoFile) throws IOException, InterruptedException {
        log.info("🎬 Extracting latest frame from video: {}", videoFile);
        
        // Find FFmpeg executable
        String ffmpegPath = findFFmpeg();
        log.info("✅ Using FFmpeg at: {}", ffmpegPath);
        
        Path outputImageFile = Files.createTempFile("latest_frame_" + UUID.randomUUID(), ".jpg");
        log.info("📁 Created output file: {}", outputImageFile);
        
        // Build FFmpeg command to extract last frame
        String[] command = {
            ffmpegPath,
            "-i", videoFile.toString(),
            "-vf", "select='isnan(next_selected_t)'", // Selects the very last frame
            "-vframes", "1",
            "-y", // Overwrite output file
            "-q:v", "2", // High quality
            "-update", "1", // Update single output file
            outputImageFile.toString()
        };
        
        log.info("🔧 FFmpeg command: {}", String.join(" ", command));
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        // Read process output for debugging
        StringBuilder ffmpegOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ffmpegOutput.append(line).append("\n");
                log.debug("FFmpeg: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        log.info("FFmpeg exit code: {}", exitCode);
        
        if (exitCode != 0) {
            log.error("❌ FFmpeg failed with exit code: {}", exitCode);
            log.error("FFmpeg output:\n{}", ffmpegOutput.toString());
            
            // Try alternative method to get last frame
            return extractLastFrameAlternative(videoFile, ffmpegPath);
        }
        
        // Verify output file exists and has content
        if (!Files.exists(outputImageFile) || Files.size(outputImageFile) == 0) {
            log.error("❌ Latest frame extraction failed - output file is empty or doesn't exist");
            // Try alternative method
            return extractLastFrameAlternative(videoFile, ffmpegPath);
        }
        
        log.info("✅ Latest frame extracted successfully: {} bytes", Files.size(outputImageFile));
        return outputImageFile;
    }
    
    private Path extractLastFrameAlternative(Path videoFile, String ffmpegPath) throws IOException, InterruptedException {
        log.info("🔄 Trying alternative method to extract last frame");
        
        Path outputImageFile = Files.createTempFile("last_frame_alt_" + UUID.randomUUID(), ".jpg");
        
        // Alternative: seek to end and extract frame
        String[] command = {
            ffmpegPath,
            "-sseof", "-0.1", // Seek to 1 second before end
            "-i", videoFile.toString(),
            "-vframes", "1",
            "-y", // Overwrite output file
            "-q:v", "2", // High quality
            outputImageFile.toString()
        };
        
        log.info("🔧 Alternative FFmpeg command: {}", String.join(" ", command));
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        // Read process output
        StringBuilder ffmpegOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ffmpegOutput.append(line).append("\n");
                log.debug("FFmpeg Alt: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        log.info("Alternative FFmpeg exit code: {}", exitCode);
        
        if (exitCode != 0) {
            log.error("❌ Alternative FFmpeg failed with exit code: {}", exitCode);
            log.error("Alternative FFmpeg output:\n{}", ffmpegOutput.toString());
            throw new RuntimeException("Failed to extract last frame with both methods. Output: " + ffmpegOutput.toString());
        }
        
        // Verify output file
        if (!Files.exists(outputImageFile) || Files.size(outputImageFile) == 0) {
            log.error("❌ Alternative method failed - output file is empty or doesn't exist");
            throw new RuntimeException("Alternative method failed - no output file generated");
        }
        
        log.info("✅ Last frame extracted with alternative method: {} bytes", Files.size(outputImageFile));
        return outputImageFile;
    }
    
    private Path extractFrame(Path videoFile, double captureTimeSeconds) throws IOException, InterruptedException {
        log.info("🎬 Extracting frame at {}s from video: {}", captureTimeSeconds, videoFile);
        
        // Find FFmpeg executable
        String ffmpegPath = findFFmpeg();
        log.info("✅ Using FFmpeg at: {}", ffmpegPath);
        
        Path outputImageFile = Files.createTempFile("frame_" + UUID.randomUUID(), ".jpg");
        log.info("📁 Created output file: {}", outputImageFile);
        
        // Build FFmpeg command
        String[] command = {
            ffmpegPath,
            "-i", videoFile.toString(),
            "-ss", String.valueOf(captureTimeSeconds),
            "-vframes", "1",
            "-y", // Overwrite output file
            "-q:v", "2", // High quality
            outputImageFile.toString()
        };
        
        log.info("🔧 FFmpeg command: {}", String.join(" ", command));
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        // Read process output for debugging
        StringBuilder ffmpegOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ffmpegOutput.append(line).append("\n");
                log.debug("FFmpeg: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        log.info("FFmpeg exit code: {}", exitCode);
        
        if (exitCode != 0) {
            log.error("❌ FFmpeg failed with exit code: {}", exitCode);
            log.error("FFmpeg output:\n{}", ffmpegOutput.toString());
            throw new RuntimeException("FFmpeg failed with exit code: " + exitCode + "\nOutput: " + ffmpegOutput.toString());
        }
        
        // Verify output file exists and has content
        if (!Files.exists(outputImageFile) || Files.size(outputImageFile) == 0) {
            log.error("❌ Frame extraction failed - output file is empty or doesn't exist");
            throw new RuntimeException("Frame extraction failed - no output file generated");
        }
        
        log.info("✅ Frame extracted successfully: {} bytes", Files.size(outputImageFile));
        return outputImageFile;
    }
    
    private String findFFmpeg() {
        // Common FFmpeg locations
        String[] possiblePaths = {
            "ffmpeg", // In PATH
            "/usr/bin/ffmpeg", // Standard Linux location
            "/usr/local/bin/ffmpeg", // Common Linux location
            "/opt/homebrew/bin/ffmpeg", // Homebrew on Apple Silicon
            "/usr/local/homebrew/bin/ffmpeg", // Homebrew on Intel Mac
            "/opt/local/bin/ffmpeg" // MacPorts
        };
        
        for (String path : possiblePaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(path, "-version");
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log.info("Found FFmpeg at: {}", path);
                    return path;
                }
            } catch (Exception e) {
                log.debug("FFmpeg not found at: {} - {}", path, e.getMessage());
            }
        }
        
        throw new RuntimeException("FFmpeg not found in any common locations. Please install FFmpeg or add it to PATH.");
    }
    
    private void cleanupTempFiles(Path... files) {
        for (Path file : files) {
            try {
                if (file != null && Files.exists(file)) {
                    Files.delete(file);
                    log.debug("Deleted temp file: {}", file);
                }
            } catch (IOException e) {
                log.warn("Failed to delete temp file: {}", file, e);
            }
        }
    }
}
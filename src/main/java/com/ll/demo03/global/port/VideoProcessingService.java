package com.ll.demo03.global.port;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface VideoProcessingService {
    /**
     * Downloads video from URL and extracts frame at specified time
     * @param videoUrl URL of the video to download
     * @param captureTimeSeconds Time in seconds to capture frame
     * @return MultipartFile containing the extracted frame as image
     */
    MultipartFile extractFrameFromVideo(String videoUrl, double captureTimeSeconds);
    
    /**
     * Downloads video from URL and extracts the latest (last) frame
     * @param videoUrl URL of the video to download
     * @return MultipartFile containing the last frame as image
     */
    MultipartFile extractLatestFrameFromVideo(String videoUrl);
    
    /**
     * Downloads multiple videos and concatenates them into one video
     * @param videoUrls List of video URLs to concatenate
     * @param quality Quality setting ("high", "medium", "low")
     * @return Path to the concatenated video file
     */
    MultipartFile concatenateVideos(List<String> videoUrls, String quality);
}
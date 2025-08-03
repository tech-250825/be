package com.ll.demo03.global.controller;

import com.ll.demo03.global.domain.ResolutionProfile;
import com.ll.demo03.global.dto.GlobalResponse;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/resolutions")
public class ResolutionController {

    @GetMapping
    public GlobalResponse<List<ResolutionOption>> getAvailableResolutions() {
        List<ResolutionOption> resolutions = Arrays.stream(ResolutionProfile.values())
                .map(profile -> new ResolutionOption(
                        profile.name(),
                        profile.getWidth() + "×" + profile.getHeight(),
                        profile.getBaseCreditCost(),
                        profile.getWidth(),
                        profile.getHeight()
                ))
                .toList();
        
        return GlobalResponse.success(resolutions);
    }

    @Getter
    public static class ResolutionOption {
        private final String name;
        private final String displayName;
        private final int baseCreditCost;
        private final int width;
        private final int height;

        public ResolutionOption(String name, String displayName, int baseCreditCost, int width, int height) {
            this.name = name;
            this.displayName = displayName;
            this.baseCreditCost = baseCreditCost;
            this.width = width;
            this.height = height;
        }
    }
}
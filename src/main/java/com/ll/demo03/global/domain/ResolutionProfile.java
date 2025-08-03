package com.ll.demo03.global.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ll.demo03.global.config.ResolutionProfileDeserializer;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import lombok.Getter;

@Getter
@JsonDeserialize(using = ResolutionProfileDeserializer.class)
public enum ResolutionProfile {
    RATIO_1_1_SD(480, 480, 5),
    RATIO_1_1_HD(720, 720, 10),
    RATIO_16_9_SD(854, 480, 5),
    RATIO_16_9_HD(1280, 720, 10),
    RATIO_9_16_SD(480, 854, 5),
    RATIO_9_16_HD(720, 1280, 10),
    I2V_SD(0, 0, 5),
    I2V_HD(0, 0, 10);

    private final int width;
    private final int height;
    private final int baseCreditCost;

    ResolutionProfile(int width, int height, int baseCreditCost) {
        this.width = width;
        this.height = height;
        this.baseCreditCost = baseCreditCost;
    }

    public static ResolutionProfile from(String ratio, boolean isHD) {
        String key = "RATIO_" + ratio.replace(":", "_") + (isHD ? "_HD" : "_SD");
        try {
            return ResolutionProfile.valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    public static ResolutionProfile fromDimensions(int width, int height) {
        for (ResolutionProfile profile : values()) {
            if (profile.width == width && profile.height == height) {
                return profile;
            }
        }
        throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
    }
}


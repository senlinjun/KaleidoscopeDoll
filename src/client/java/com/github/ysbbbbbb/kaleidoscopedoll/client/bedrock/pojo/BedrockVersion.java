package com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.pojo;

public enum BedrockVersion {
    /**
     * 旧版本基岩版模型，仅限 1.10.0
     */
    LEGACY("1.10.0"),
    /**
     * 新版本基岩版模型，往后的 1.14.0，1.16.0 1.21.0 通通用此版本读取
     */
    NEW("1.12.0");

    private final String minVersion;

    BedrockVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public static boolean isNewVersion(BedrockModelPOJO bedrockModel) {
        return compareVersions(bedrockModel.getFormatVersion(), NEW.minVersion) >= 0;
    }

    public static boolean isLegacyVersion(BedrockModelPOJO bedrockModel) {
        return compareVersions(bedrockModel.getFormatVersion(), LEGACY.minVersion) >= 0
                && compareVersions(bedrockModel.getFormatVersion(), NEW.minVersion) < 0;
    }

    public static BedrockVersion getVersion(BedrockModelPOJO pojo) {
        if (isNewVersion(pojo)) {
            return NEW;
        } else if (isLegacyVersion(pojo)) {
            return LEGACY;
        }
        throw new IllegalArgumentException("Invalid version for model: " + pojo);
    }

    private static int compareVersions(String a, String b) {
        String[] partsA = a.split("\\.");
        String[] partsB = b.split("\\.");
        int length = Math.max(partsA.length, partsB.length);
        for (int i = 0; i < length; i++) {
            int partA = i < partsA.length ? parseIntSafe(partsA[i]) : 0;
            int partB = i < partsB.length ? parseIntSafe(partsB[i]) : 0;
            if (partA != partB) {
                return Integer.compare(partA, partB);
            }
        }
        return 0;
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

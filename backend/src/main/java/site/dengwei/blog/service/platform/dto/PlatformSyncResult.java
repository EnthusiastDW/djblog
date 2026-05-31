package site.dengwei.blog.service.platform.dto;

import lombok.Data;

@Data
public class PlatformSyncResult {
    private String platformCode;
    private boolean success;
    private String externalUrl;
    private String errorMessage;

    public static PlatformSyncResult success(String platformCode, String externalUrl) {
        PlatformSyncResult result = new PlatformSyncResult();
        result.setPlatformCode(platformCode);
        result.setSuccess(true);
        result.setExternalUrl(externalUrl);
        return result;
    }

    public static PlatformSyncResult failed(String platformCode, String errorMessage) {
        PlatformSyncResult result = new PlatformSyncResult();
        result.setPlatformCode(platformCode);
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}

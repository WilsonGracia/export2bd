package com.export2bd.dto;

import java.util.List;

public class UploadResultDto {

    private int processed;
    private int succeeded;
    private int failed;
    private List<ImportFailureDto> failures;

    public int getProcessed() { return processed; }
    public void setProcessed(int processed) { this.processed = processed; }

    public int getSucceeded() { return succeeded; }
    public void setSucceeded(int succeeded) { this.succeeded = succeeded; }

    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }

    public List<ImportFailureDto> getFailures() { return failures; }
    public void setFailures(List<ImportFailureDto> failures) { this.failures = failures; }
}
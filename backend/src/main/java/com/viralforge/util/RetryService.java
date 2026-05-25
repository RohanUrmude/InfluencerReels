package com.viralforge.util;

import com.viralforge.exception.AIServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RetryService {
    public interface Callable<T> {
        T call() throws Exception;
    }

    public <T> T executeWithRetry(Callable<T> callable, int maxRetries, long delayMs, String operationName) throws AIServiceException {
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                attempt++;
                log.info("Executing operation '{}' (attempt {}/{})", operationName, attempt, maxRetries);
                return callable.call();
            } catch (Exception e) {
                if (attempt >= maxRetries) {
                    log.error("Operation '{}' failed after {} attempts", operationName, maxRetries, e);
                    throw new AIServiceException("Operation failed after " + maxRetries + " retries: " + e.getMessage(), e);
                }

                long backoffDelay = delayMs * (long) Math.pow(2, attempt - 1);
                log.warn("Operation '{}' attempt {} failed. Retrying in {}ms. Error: {}",
                    operationName, attempt, backoffDelay, e.getMessage());

                try {
                    Thread.sleep(backoffDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AIServiceException("Retry interrupted", ie);
                }
            }
        }

        throw new AIServiceException("Operation failed after all retries");
    }
}

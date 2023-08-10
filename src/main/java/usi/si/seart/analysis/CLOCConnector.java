package usi.si.seart.analysis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import usi.si.seart.exception.StaticCodeAnalysisException;
import usi.si.seart.exception.TerminalExecutionException;
import usi.si.seart.io.ExternalProcess;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Component responsible for performing static code analysis through CLOC.
 */
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CLOCConnector {

    ConversionService conversionService;

    /**
     * Performs static code analysis using CLOC.
     *
     * @param path The path to the directory to analyze.
     * @return A JSON object containing the analysis results.
     * @throws StaticCodeAnalysisException if an error occurs during analysis.
     */
    @SuppressWarnings("ConstantConditions")
    public JsonObject analyze(Path path) throws StaticCodeAnalysisException {
        try {
            ExternalProcess process = new ExternalProcess(path, "cloc", "--json", "--quiet", ".");
            ExternalProcess.Result result = process.execute(5, TimeUnit.MINUTES);
            result.ifFailedThrow(() -> new StaticCodeAnalysisException(result.getStdErr()));
            JsonElement element = conversionService.convert(result.getStdOut(), JsonElement.class);
            return element.isJsonNull() ? new JsonObject() : element.getAsJsonObject();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new StaticCodeAnalysisException("Interrupted while analyzing: " + path, ex);
        } catch (TimeoutException ex) {
            throw new StaticCodeAnalysisException("Timed out while analyzing: " + path, ex);
        } catch (TerminalExecutionException ex) {
            throw new StaticCodeAnalysisException("Failed for: " + path, ex);
        }
    }
}

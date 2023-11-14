package ch.usi.si.seart.analysis;

import ch.usi.si.seart.exception.StaticCodeAnalysisException;
import ch.usi.si.seart.exception.TerminalExecutionException;
import ch.usi.si.seart.io.ExternalProcess;
import ch.usi.si.seart.stereotype.Connector;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Component responsible for performing static code analysis through CLOC.
 */
@Connector(command = "cloc")
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CLOCConnector {

    ConversionService conversionService;

    @Value("${app.cloc.analysis-timeout-duration}")
    Duration duration;

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
            ExternalProcess.Result result = process.execute(duration.toMillis());
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

package ch.usi.si.seart.controller;

import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/l")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(
        name = "language",
        description = "Endpoints used for retrieving information regarding platform-supported languages."
)
public class LanguageController {

    LanguageService languageService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the names of all currently supported languages")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<?> getLanguages(
            @RequestParam(required = false, defaultValue = "")
            @Parameter(description = "The search term value", in = ParameterIn.QUERY)
            String name,
            @Parameter(description = "The search pagination settings", in = ParameterIn.QUERY)
            Pageable pageable
    ) {
        Page<Language> page = ObjectUtils.isEmpty(name)
                ? languageService.getAllMined(pageable)
                : languageService.getMinedByNameContains(name, pageable);
        return ResponseEntity.ok(page.map(Language::getName));
    }
}

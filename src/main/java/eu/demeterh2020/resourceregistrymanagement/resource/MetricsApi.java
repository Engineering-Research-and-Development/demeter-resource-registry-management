package eu.demeterh2020.resourceregistrymanagement.resource;

import eu.demeterh2020.resourceregistrymanagement.domain.Metrics;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.JsonResponse;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.MetricsDataDto;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.MetricsDto;
import eu.demeterh2020.resourceregistrymanagement.domain.dto.UserResourceMetricsDto;
import eu.demeterh2020.resourceregistrymanagement.exception.BadRequestException;
import eu.demeterh2020.resourceregistrymanagement.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/metrics", produces = {MediaType.APPLICATION_JSON_VALUE})
public class MetricsApi {

    private final static Logger log = LoggerFactory.getLogger(DehResourceApi.class);

    @Autowired
    private MetricsService metricsService;


    @Operation(summary = "Store new metrics of DEH Resource consumption")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource registered",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public JsonResponse saveMetricsData(@Valid @RequestBody List<MetricsDataDto> metricsData) {


        if (metricsData.isEmpty()){
            throw new BadRequestException("Metrics are empty");
        }
        log.info("saveMetricsData() called.");

        metricsService.save(metricsData);

        return new JsonResponse(true, "Metrics successfully stored", null, null);

    }

    @Operation(summary = "List all DEH Resources metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource registered",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserResourceMetricsDto.class)))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content)})
    @GetMapping
    public JsonResponse findAllMetrics(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
                                       @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
                                       @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
                                       @RequestParam(name = "sortingOrder", required = false, defaultValue = "ASC") Sort.Direction sortingOrder) {

        log.info("findAll() called.");

        Map<String, Object> allMetrics = metricsService.findAllMetrics();

        return new JsonResponse(true, "All metrics for users DEH Resources found.", allMetrics , null);

    }

    @Operation(summary = "Find DEH Resource metrics by RRM id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MetricsDto.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonResponse.class))}),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonResponse.class))})})
    @GetMapping(value = "/rrmId/{rrmId}")
    public JsonResponse findOneMetricByDehResourceUid(@PathVariable String rrmId, @RequestParam(name = "deh", required = false, defaultValue = "false") boolean deh) {

        log.info("findOneByDehResourceUid() called.");

        if (deh) {
            MetricsDto metricsDto = metricsService.findOneByRrmIdDeh(rrmId);
            return new JsonResponse(true, "Metrics found for DEH Resource with uid:" + rrmId + " found.",
                    metricsDto, null);
        } else {
            Metrics metricsData = metricsService.findOneByRrmId(rrmId);

            return new JsonResponse(true, "Metrics found for DEH Resource with uid:" + rrmId + " found.",
                    metricsData, null);
        }
    }

    @Operation(summary = "Find DEH Resource metrics by container id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DEH Resource found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MetricsDataDto.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonResponse.class))}),
            @ApiResponse(responseCode = "404", description = "DEH Resource not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JsonResponse.class))})})
    @GetMapping(value = "/containerId/{containerId}")
    public JsonResponse findOneMetricByContainerId(@PathVariable String containerId) {

        MetricsDataDto container = metricsService.findOneByContainerId(containerId);

        return new JsonResponse(true, "Metrics found for container with id:" + containerId + " found."
                , container, null);

    }
}

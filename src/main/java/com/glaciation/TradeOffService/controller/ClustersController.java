package com.glaciation.TradeOffService.controller;

import com.glaciation.TradeOffService.model.Node;
import com.glaciation.TradeOffService.model.Workload;
import com.glaciation.TradeOffService.service.NodeService;
import com.glaciation.TradeOffService.service.WorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/clusters")
public class ClustersController {
    private final Logger logger = LoggerFactory.getLogger(ClustersController.class);

    @Autowired
    NodeService nodeService;
    @Autowired
    WorkloadService workloadService;

    @GetMapping("/{clusterId}/workloads")
    @Operation(description = "Get all workloads' data and metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Array of workloads returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Workload.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "500", description = "An error occurred on the server side", content = @Content)
    })
    public ResponseEntity<?> getWorkloads(
            @Parameter(description = "Unique identifier for the specific cluster") @PathVariable String clusterId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm'Z'") @Parameter(description = "Date indicating the beginning of the aggregation") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm'Z'") @Parameter(description = "Date indicating the end of the aggregation") Date endTime
    ) {
        logger.info("Received request: GET /{}/workloads", clusterId);
        return this.workloadService.getWorkloads(startTime, endTime);
    }

    @GetMapping("/{clusterId}/workloads/{workloadId}")
    @Operation(description = "Get data and metadata about a specific workload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workload found and returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Workload.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Workload with provided ID not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "An error occurred on the server side", content = @Content)
    })
    public ResponseEntity<Object> getWorkload(
            @Parameter(description = "Unique identifier for the specific cluster") @PathVariable String clusterId,
            @Parameter(description = "Unique workload ID") @PathVariable String workloadId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm'Z'") @Parameter(description = "Date indicating the beginning of the aggregation") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm'Z'") @Parameter(description = "Date indicating the end of the aggregation") Date endTime
    ) {
        logger.info("Received request: GET /{}/workloads/{}", clusterId, workloadId);
        return workloadService.getWorkload(workloadId, startTime, endTime);
    }

    @GetMapping("/{clusterId}/nodes")
    @Operation(description = "Get all worker nodes' data and metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Array of worker nodes returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Workload.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "500", description = "An error occurred on the server side", content = @Content)
    })
    public ResponseEntity<?> getNodes(
            @Parameter(description = "Unique identifier for the specific cluster") @PathVariable String clusterId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm'Z'") @Parameter(description = "Date indicating the beginning of the aggregation") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm'Z'") @Parameter(description = "Date indicating the end of the aggregation") Date endTime
    ) {
        logger.info("Received request: GET /{}/nodes", clusterId);
        return this.nodeService.getNodes(startTime, endTime);
    }

    @GetMapping("/{clusterId}/nodes/{nodeId}")
    @Operation(description = "Get data and metadata about a specific worker node")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker node found and returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Node.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Worker node with provided ID not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "An error occurred on the server side", content = @Content)
    })
    public ResponseEntity<?> getNode(
            @Parameter(description = "Unique identifier for the specific cluster") @PathVariable String clusterId,
            @Parameter(description = "Unique worker node ID") @PathVariable String nodeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm'Z'") @Parameter(description = "Date indicating the beginning of the aggregation") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm'Z'") @Parameter(description = "Date indicating the end of the aggregation") Date endTime
    ) {
        logger.info("Received request: GET /{}/nodes/{}", clusterId, nodeId);
        return this.nodeService.getNode(nodeId, startTime, endTime);
    }
}

package com.glaciation.TradeOffService.controller;

import com.glaciation.TradeOffService.model.Node;
import com.glaciation.TradeOffService.model.Workload;
import com.glaciation.TradeOffService.service.StubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/stub")
public class StubController {

    @Autowired
    StubService stubService;

    @GetMapping("/workloads")
    @Operation(description = "Get data and metadata about all workloads")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workloads list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Workload.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "500", description = "An error occurred on the server side", content = @Content)
    })
    public ResponseEntity<?> getWorkloads(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "Date indicating the beginning of the aggregation") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "Date indicating the end of the aggregation") Date endTime
    ) {
        return this.stubService.getWorkloads(startTime, endTime);
    }

    @GetMapping("/workloads/{workloadId}")
    @Operation(description = "Get data and metadata about a specific workload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specific workload information", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Workload.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "500", description = "An error occurred on the server side", content = @Content)
    })
    public ResponseEntity<?> getWorkload(
            @PathVariable @Parameter(description = "Unique workload ID") String workloadId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "Date indicating the beginning of the aggregation") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "Date indicating the end of the aggregation") Date endTime
    ) {
        return this.stubService.getWorkload(workloadId, startTime, endTime);
    }

    @GetMapping("/nodes")
    @Operation(description = "Get data and metadata about all worker nodes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nodes list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Node.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "500", description = "An error occurred on the server side", content = @Content)
    })
    public ResponseEntity<?> getNodes(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "Date indicating the beginning of the aggregation") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "Date indicating the end of the aggregation") Date endTime
    ) {
        return this.stubService.getNodes(startTime, endTime);
    }

    @GetMapping("/nodes/{nodeId}")
    @Operation(description = "Get data and metadata about a specific worker node")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specific worker node information", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Node.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "500", description = "An error occurred on the server side", content = @Content)
    })
    public ResponseEntity<?> getNode(
            @PathVariable @Parameter(description = "Unique worker node ID") String nodeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "Date indicating the beginning of the aggregation") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "Date indicating the end of the aggregation") Date endTime
    ) {
        return this.stubService.getNode(nodeId, startTime, endTime);
    }

}

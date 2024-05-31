package com.glaciation.TradeOffService.controller;

import com.glaciation.TradeOffService.service.StubService;
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
    public ResponseEntity<?> getWorkloads(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return this.stubService.getWorkloads(startTime, endTime);
    }

    @GetMapping("/workloads/{workloadId}")
    public ResponseEntity<?> getWorkload(@PathVariable String workloadId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return this.stubService.getWorkload(workloadId, startTime, endTime);
    }

    @GetMapping("/nodes")
    public ResponseEntity<?> getNodes(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return this.stubService.getNodes(startTime, endTime);
    }

    @GetMapping("/nodes/{nodeId}")
    public ResponseEntity<?> getNode(@PathVariable String nodeId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return this.stubService.getNode(nodeId, startTime, endTime);
    }

}

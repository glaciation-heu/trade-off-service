package com.glaciation.TradeOffService.service;

import com.glaciation.TradeOffService.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StubService {
    private final Logger logger = LoggerFactory.getLogger(StubService.class);

    public ResponseEntity<?> getWorkloads(Date startTime, Date endTime) {
        List<Workload> workloads = new ArrayList<>();
        int records = (int) generateRandomNumber(2, 8);
        String[] podNames = {"nginx", "mysql", "kafka", "presto", "influxdb", "mongo", "redis", "sftp", "minio"};
        logger.info("Generating {} workloads for time window [{}, {}]...", records, startTime, endTime);

        for (int i = 0; i < records; i++) {
            RunInfo runInfo = new RunInfo("node-" + (int) generateRandomNumber(1, 4), startTime, endTime);
            Workload workload = new Workload(podNames[i] + "-pod", runInfo);
            workload.setResources(generateWorkloadResources());
            workloads.add(workload);
        }

        return ResponseEntity.ok(workloads);
    }

    public ResponseEntity<?> getWorkload(String workloadId, Date startTime, Date endTime) {
        RunInfo runInfo = new RunInfo("node-1", startTime, endTime);
        Workload workload = new Workload(workloadId, runInfo);
        workload.setResources(generateWorkloadResources());
        logger.info("Generated workload: {}", workload);

        return ResponseEntity.ok(workload);
    }

    public ResponseEntity<?> getNodes(Date startTime, Date endTime) {
        List<Node> nodes = new ArrayList<>();
        int records = (int) generateRandomNumber(2, 4);
        logger.info("Generating {} nodes for time window [{}, {}]...", records, startTime, endTime);

        for (int i = 0; i < records; i++) {
            Node node = new Node("node-" + (i + 1));
            node.setResources(generateNodeResources());
            nodes.add(node);
        }

        return ResponseEntity.ok(nodes);
    }

    public ResponseEntity<?> getNode(String nodeId, Date startTime, Date endTime) {
        Node node = new Node(nodeId);
        node.setResources(generateNodeResources());
        logger.info("Generated node: {}", node);

        return ResponseEntity.ok(node);
    }

    private List<WorkloadResource> generateWorkloadResources() {
        WorkloadResource cpuResource = new WorkloadResource("cpu", "cores");
        cpuResource.setAllocated(generateRandomNumber(1, 4));
        cpuResource.setDemanded(generateRandomNumber(1, 4));
        cpuResource.setUsed(generateRandomNumber(1, 4));

        WorkloadResource gpuResource = new WorkloadResource("gpu", "units");
        gpuResource.setAllocated(generateRandomNumber(1, 4));
        gpuResource.setDemanded(generateRandomNumber(1, 4));
        gpuResource.setUsed(generateRandomNumber(1, 4));

        WorkloadResource memoryResource = new WorkloadResource("memory", "GB");
        memoryResource.setAllocated(generateRandomNumber(8, 32));
        memoryResource.setDemanded(generateRandomNumber(4, 12));
        memoryResource.setUsed(generateRandomNumber(1, 4));

        WorkloadResource energyIndexResource = new WorkloadResource("energy_index", "milliwatt");
        energyIndexResource.setAllocated(generateRandomNumber(6000, 9000));
        energyIndexResource.setDemanded(generateRandomNumber(6000, 9000));
        energyIndexResource.setUsed(generateRandomNumber(6000, 9000));

        return new ArrayList<>(Arrays.asList(cpuResource, gpuResource, memoryResource, energyIndexResource));
    }

    private List<NodeResource> generateNodeResources() {
        NodeResource cpuResource = new NodeResource("cpu", "cores");
        cpuResource.setAvailable(generateRandomNumber(1, 4));
        cpuResource.setMax(generateRandomNumber(4, 8));

        NodeResource gpuResource = new NodeResource("gpu", "units");
        gpuResource.setAvailable(generateRandomNumber(1, 4));
        gpuResource.setMax(generateRandomNumber(1, 4));

        NodeResource memoryResource = new NodeResource("memory", "GB");
        memoryResource.setAvailable(generateRandomNumber(4, 12));
        memoryResource.setMax(generateRandomNumber(8, 32));

        NodeResource storageResource = new NodeResource("storage", "GB");
        storageResource.setAvailable(generateRandomNumber(4, 12));
        storageResource.setMax(generateRandomNumber(8, 32));

        NodeResource networkResource = new NodeResource("network", "Mbps");
        networkResource.setAvailable(generateRandomNumber(4, 12));
        networkResource.setMax(generateRandomNumber(8, 32));

        NodeResource energyIndexResource = new NodeResource("energy_index", "milliwatt");
        energyIndexResource.setAvailable(generateRandomNumber(6000, 9000));
        energyIndexResource.setMax(generateRandomNumber(6000, 9000));

        return new ArrayList<>(Arrays.asList(cpuResource, gpuResource, memoryResource, storageResource, networkResource, energyIndexResource));
    }

    private long generateRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextLong(max - min) + min;
    }
}

declare global {
  namespace NodeJS {
    interface ProcessEnv {
      BASE_URL: string;
      NODES_DESIRED_METRICS: string;
      WORKLOADS_DESIRED_METRICS: string;
      TEST_NODES: string;
      TEST_WORKLOADS: string;
      NODE_ID: string;
      WORKLOAD_ID: string;
    }
  }
}

export { }
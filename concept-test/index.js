// Loading env variables (.env file)
require('dotenv').config();

const axios = require('axios');
const assert = require('assert');
const logger = require('./logger');
const results = { failed: 0, passed: 0 };

// Define the target URL of the REST controller
const BASE_URL = process.env.BASE_URL;

async function testResponse(resources) {
  if (resources !== "nodes" && resources !== "workloads") {
    logger.error("❌ Configuration error: can test either 'nodes' or 'workloads'");
  }

  try {
    const resourceId = resources === "nodes" ? process.env.NODE_ID : process.env.WORKLOAD_ID;
    const url = `${BASE_URL}/${resources}/${resourceId}`;
    const EXPECTED_KEYS_STRING = resources === "nodes" ? process.env.NODES_DESIRED_METRICS : process.env.WORKLOADS_DESIRED_METRICS;

    // Preparing required parameters
    const { startTime, endTime } = generateParams();
    const params = new URLSearchParams([
      ['startTime', startTime],
      ['endTime', endTime],
    ]);
    // Perform the GET request
    logger.info(`Sending request: GET ${url}?${params.toString()}`);
    const { data } = await axios.get(url, { params });

    // Verify that all expected keys are present
    logger.info("Verifying that response body contains following keys:", EXPECTED_KEYS_STRING);
    EXPECTED_KEYS_STRING.split(",").forEach(key => {
      assert.ok(data.resources.hasOwnProperty(key), `Missing key: ${key}`);
    });

    logger.info('✅ Test passed: All expected keys are present in the response.');
    results.passed++;

  } catch (error) {
    if (error.response) {
      logger.error('❌ Test failed: Server responded with error:', error.response.status, error.response.data);
      results.failed++;
    } else {
      logger.error('❌ Test failed:', error.message || error.code);
      results.failed++;
    }
  }
}

async function test() {
  if (process.env.TEST_NODES === "true") await testResponse('nodes');
  if (process.env.TEST_WORKLOADS === "true") await testResponse('workloads');

  if (results.failed === 0) logger.success(`All tests passed successfully`);
  else logger.error(`Test results: FAILED: ${results.failed} PASSED: ${results.passed}`);
}

// Run the test
test();


function generateParams() {
  const start = new Date(), end = new Date();

  start.setHours(new Date().getHours() - 1);
  start.setMinutes(0);
  end.setHours(new Date().getHours() - 1);
  end.setMinutes(30);

  const startTime = start.toISOString().substring(0, 16) + "Z";
  const endTime = end.toISOString().substring(0, 16) + "Z";
  return { startTime, endTime };
}
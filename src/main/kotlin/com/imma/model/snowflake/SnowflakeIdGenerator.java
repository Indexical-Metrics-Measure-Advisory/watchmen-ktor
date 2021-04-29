package com.imma.model.snowflake;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SnowflakeIdGenerator {
    public synchronized SnowflakeIdWorker createWorker(long workerId, long dataCenterId, boolean workerByIp) {
        if (workerByIp && workerId == 0) {
            workerId = getWorkerIdByIp();
        }
        return new SnowflakeIdWorker(workerId, dataCenterId);
    }

    protected long getWorkerIdByIp() {
        long workerId = 0;
        byte[] address;
        try {
            address = InetAddress.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
            address = null;
        }
        // Worker id is generated by hashing of ip address which is unique in a local network
        if (address != null) {
            for (byte x : address) {
                workerId = ((workerId << 8) - Byte.MIN_VALUE + x) & SnowflakeIdWorker.MAX_WORKER_ID;
            }
        }
        return workerId;
    }
}
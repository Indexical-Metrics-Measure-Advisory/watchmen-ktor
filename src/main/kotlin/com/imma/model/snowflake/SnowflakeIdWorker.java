package com.imma.model.snowflake;

public class SnowflakeIdWorker {
	/**
	 * start timestamp on millisecond from 2015/01/01
	 */
	private final static long TWEPOCH = 1420041600000L;

	/**
	 * bits for workers
	 */
	private final static long WORKER_ID_BITS = 5L;

	/**
	 * bits for data centers
	 */
	private final static long DATA_CENTER_ID_BITS = 5L;

	public final static long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);

	public final static long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);

	/**
	 * bits for sequence
	 */
	private final static long SEQUENCE_BITS = 12L;

	private final static long WORKER_ID_SHIFT = SEQUENCE_BITS;

	private final static long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

	private final static long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

	private final static long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

	private long workerId;

	private long dataCenterId;

	private long sequence = 0L;

	private long lastTimestamp = -1L;

	public SnowflakeIdWorker(long workerId, long dataCenterId) {
		if (workerId > MAX_WORKER_ID || workerId < 0) {
			throw new IllegalArgumentException(String.format("worker id can't be greater than %d or less than 0", MAX_WORKER_ID));
		}
		if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
			throw new IllegalArgumentException(String.format("data center id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
		}
		this.workerId = workerId;
		this.dataCenterId = dataCenterId;
	}

	public SnowflakeIdWorker() {
	}

	public synchronized long nextId() {
		long timestamp = timeGen();

		if (timestamp < lastTimestamp) {
			throw new RuntimeException(
					String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
		}

		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) & SEQUENCE_MASK;
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0L;
		}

		lastTimestamp = timestamp;

		return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT)
				| (dataCenterId << DATA_CENTER_ID_SHIFT)
				| (workerId << WORKER_ID_SHIFT)
				| sequence;
	}

	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	protected long timeGen() {
		return System.currentTimeMillis();
	}
}

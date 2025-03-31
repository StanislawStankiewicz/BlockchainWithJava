package me.stahu.blockchain.core;

import java.time.Duration;

public record Block(long id, long minerId, long timestamp, String previousHash, String hash, long magicNumber, Duration generationTime) {

    @Override
    public String toString() {
        return """
                Block:
                Created by miner # %d
                Id: %d
                Timestamp: %d
                Magic number: %d
                Hash of the previous block:
                %s
                Hash of the block:
                %s
                Block was generating for %.3f seconds""".formatted(minerId, id, timestamp, magicNumber, previousHash, hash, generationTime.toMillis() / 1000.0);
    }
}

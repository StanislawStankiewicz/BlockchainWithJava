package me.stahu.blockchain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    private static final int NUM_MINERS = 20;
    private static final int NUM_BLOCKS = 5;

    public static void main(String[] args) {
        int initialDifficulty = 3;
        Blockchain blockchain = new Blockchain(initialDifficulty, NUM_BLOCKS);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_MINERS);

        for (int i = 0; i < NUM_MINERS; i++) {
            executor.execute(new Miner(blockchain, i));
        }
        while (blockchain.hasSpace()) {
            blockchain.waitForNewBlock();
        }
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Miners did not terminate in time");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

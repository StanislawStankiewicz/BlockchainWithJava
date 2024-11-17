package me.stahu.blockchain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {

    static final String DEFAULT_HASH = "0";
    private List<Block> blockchain = new ArrayList<>();
    private int difficulty;
    private volatile boolean newBlockMined = false;
    private final Object lock = new Object();
    private final int maxBlocks;
    private long lastBlockTimestamp;

    public Blockchain(int initialDifficulty, int maxBlocks) {
        this.difficulty = initialDifficulty;
        this.maxBlocks = maxBlocks;
        this.lastBlockTimestamp = System.nanoTime();
    }

    public int getDifficulty() {
        return difficulty;
    }

    public synchronized boolean addBlock(BlockSubmission blockSubmission, long minerId) {
        if (blockchain.size() >= maxBlocks) {
            return false;
        }
        if (validateNewBlock(blockSubmission)) {
            Duration generationTime = Duration.ofNanos(System.nanoTime() - lastBlockTimestamp);
            lastBlockTimestamp = System.nanoTime();
            Block block = new Block(
                    blockSubmission.id(),
                    minerId,
                    lastBlockTimestamp,
                    blockSubmission.previousHash(),
                    blockSubmission.hash(),
                    blockSubmission.magicNumber(),
                    generationTime);
            blockchain.add(block);
            System.out.println(block);
            adjustDifficulty(block.generationTime());
            newBlockMined = true;
            synchronized (lock) {
                lock.notifyAll();
            }
            return true;
        }
        return false;
    }

    public boolean hasSpace() {
        return blockchain.size() < maxBlocks;
    }

    private boolean validateNewBlock(BlockSubmission block) {
        String previousHash = blockchain.isEmpty() ? DEFAULT_HASH : blockchain.get(blockchain.size() - 1).hash();
        return previousHash.equals(block.previousHash()) && block.hash().startsWith("0".repeat(difficulty));
    }

    private void adjustDifficulty(Duration generationTime) {
        if (generationTime.toMillis() < 500) {
            difficulty++;
            System.out.println("N was increased to " + difficulty);
        } else if (generationTime.toMillis() > 3000) {
            difficulty = Math.max(0, difficulty - 1);
            System.out.println("N was decreased by 1");
        } else {
            System.out.println("N stays the same");
        }
        System.out.println();
    }

    public String getLastBlockHash() {
        return blockchain.isEmpty() ? DEFAULT_HASH : blockchain.getLast().hash();
    }

    public synchronized long getNextBlockId() {
        return blockchain.size();
    }

    public void waitForNewBlock() {
        synchronized (lock) {
            while (!newBlockMined) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
            newBlockMined = false;
        }
    }
}

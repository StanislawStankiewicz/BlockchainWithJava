package me.stahu.blockchain.miner;

import me.stahu.blockchain.core.Blockchain;
import me.stahu.blockchain.network.BlockSubmission;

import static me.stahu.blockchain.utility.Utility.calculateHash;

public class Miner implements Runnable {

    private final Blockchain blockchain;
    private final long id;

    public Miner(Blockchain blockchain, long id) {
        this.blockchain = blockchain;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (blockchain.hasSpace() && !Thread.currentThread().isInterrupted()) {
                BlockSubmission submission = mineBlock(
                        blockchain.getDifficulty(),
                        blockchain.getNextBlockId(),
                        blockchain.getLastBlockHash()
                        );
                if (blockchain.addBlock(submission, this.id)) {
                    break;
                }
            }
        } catch (Exception e) {
            // pass
        }
    }

    public BlockSubmission mineBlock(int difficulty, long id, String previousHash) {
        String prefix = "0".repeat(difficulty);
        long magicNumber;
        String hash;
        try {
            do {
                magicNumber = (long) (Math.random() * Long.MAX_VALUE);
                hash = calculateHash(id + previousHash + magicNumber);
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
            } while (!hash.startsWith(prefix));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return new BlockSubmission(id, previousHash, hash, magicNumber);
    }
}

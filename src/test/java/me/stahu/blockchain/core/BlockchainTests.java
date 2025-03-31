package me.stahu.blockchain.core;

import me.stahu.blockchain.network.BlockSubmission;
import me.stahu.blockchain.utility.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainTests {

    private Blockchain blockchain;

    @BeforeEach
    void setUp() {
        int initialDifficulty = 2;
        int maxBlocks = 5;
        blockchain = new Blockchain(initialDifficulty, maxBlocks);
    }

    @Test
    void testAddValidBlock() {
        BlockSubmission validBlock = generateValidBlockSubmission();
        boolean result = blockchain.addBlock(validBlock, 1L);
        assertTrue(result, "Adding a valid block should return true");
        assertEquals(1, blockchain.getNextBlockId(), "Blockchain should have one block");
    }

    @Test
    void testAddInvalidBlock_WrongDifficulty() {
        BlockSubmission invalidBlock = new BlockSubmission(
                blockchain.getNextBlockId(),
                blockchain.getLastBlockHash(),
                "InvalidHash",
                12345L
        );
        boolean result = blockchain.addBlock(invalidBlock, 1L);
        assertFalse(result, "Adding a block that doesn't meet difficulty should return false");
        assertEquals(0, blockchain.getNextBlockId(), "Blockchain should still be empty");
    }

    @Test
    void testDifficultyIncrease() {
        BlockSubmission block = generateValidBlockSubmission();
        blockchain.addBlock(block, 1L);
        assertEquals(3, blockchain.getDifficulty(), "Difficulty should increase by 1");
    }

    @Test
    void testHasSpace() {
        assertTrue(blockchain.hasSpace(), "Blockchain should have space initially");
        for (int i = 0; i < 5; i++) {
            BlockSubmission block = generateValidBlockSubmission();
            blockchain.addBlock(block, 1L);
        }
        assertFalse(blockchain.hasSpace(), "Blockchain should have no space after reaching max blocks");
    }

    @Test
    void testWaitForNewBlock() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                blockchain.waitForNewBlock();
            } catch (Exception e) {
                fail("Thread was interrupted unexpectedly");
            }
        });
        thread.start();

        Thread.sleep(100);

        BlockSubmission block = generateValidBlockSubmission();
        blockchain.addBlock(block, 1L);

        thread.join(1000);

        assertFalse(thread.isAlive(), "Thread should have been notified and terminated");
    }

    private BlockSubmission generateValidBlockSubmission() {
        long id = blockchain.getNextBlockId();
        String previousHash = blockchain.getLastBlockHash();
        long magicNumber = 0L;
        String hash;
        String prefix = "0".repeat(blockchain.getDifficulty());

        do {
            magicNumber++;
            String data = id + previousHash + magicNumber;
            hash = Utility.calculateHash(data);
        } while (!hash.startsWith(prefix));

        return new BlockSubmission(id, previousHash, hash, magicNumber);
    }
}

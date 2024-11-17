package me.stahu.blockchain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MinerTests {

    private Blockchain blockchainMock;
    private Miner miner;

    @BeforeEach
    void setUp() {
        blockchainMock = mock(Blockchain.class);
        miner = new Miner(blockchainMock, 1L);
    }

    @Test
    void testMineBlock_Success() {
        when(blockchainMock.getDifficulty()).thenReturn(1);
        when(blockchainMock.getNextBlockId()).thenReturn(1L);
        when(blockchainMock.getLastBlockHash()).thenReturn("0000000000000000");

        BlockSubmission submission = miner.mineBlock(1, 1L, "0000000000000000");

        assertNotNull(submission, "Block submission should not be null");
        assertTrue(submission.hash().startsWith("0"), "Hash should match the difficulty prefix");
        assertEquals(1L, submission.id(), "Block ID should match");
        assertEquals("0000000000000000", submission.previousHash(), "Previous hash should match");
    }

    @Test
    void testMineBlock_Interrupted() {
        Thread.currentThread().interrupt();

        BlockSubmission submission = miner.mineBlock(1, 1L, "0000000000000000");

        assertNull(submission, "Block submission should be null when thread is interrupted");
        assertTrue(Thread.currentThread().isInterrupted(), "Thread should remain interrupted");

        // Clear interrupt flag for other tests
        Thread.interrupted();
    }

    @Test
    void testRun_SuccessfulMining() throws InterruptedException {
        when(blockchainMock.hasSpace()).thenReturn(true);
        when(blockchainMock.getDifficulty()).thenReturn(1);
        when(blockchainMock.getNextBlockId()).thenReturn(1L);
        when(blockchainMock.getLastBlockHash()).thenReturn("0000000000000000");
        when(blockchainMock.addBlock(any(), eq(1L))).thenReturn(true);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(miner);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Executor should terminate successfully");

        verify(blockchainMock, atLeastOnce()).addBlock(any(), eq(1L));
    }

    @Test
    void testRun_ThreadInterruption() throws InterruptedException {
        when(blockchainMock.hasSpace()).thenReturn(true);
        when(blockchainMock.getDifficulty()).thenReturn(1);
        when(blockchainMock.getNextBlockId()).thenReturn(1L);
        when(blockchainMock.getLastBlockHash()).thenReturn("0000000000000000");
        when(blockchainMock.addBlock(any(), eq(1L))).thenReturn(false);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> minerFuture = executor.submit(miner);
        executor.submit(() -> {
            try {
                Thread.sleep(100);
                minerFuture.cancel(true);
            } catch (InterruptedException ignored) {
            }
        });
        executor.shutdown();
        assertTrue(executor.awaitTermination(40, TimeUnit.SECONDS), "Executor should terminate successfully");

        verify(blockchainMock, atLeastOnce()).addBlock(any(), eq(1L));
    }
}

package me.stahu.blockchain.network;

public record BlockSubmission(long id, String previousHash, String hash, long magicNumber) {
}

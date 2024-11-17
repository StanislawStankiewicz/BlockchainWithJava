package me.stahu.blockchain;

public record BlockSubmission(long id, String previousHash, String hash, long magicNumber) {
}

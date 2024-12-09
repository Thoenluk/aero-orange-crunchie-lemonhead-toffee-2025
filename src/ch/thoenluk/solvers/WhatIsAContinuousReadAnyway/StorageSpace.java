package ch.thoenluk.solvers.WhatIsAContinuousReadAnyway;

import ch.thoenluk.ut.UtMath;
import ch.thoenluk.ut.UtParsing;

import java.util.LinkedList;
import java.util.List;

class StorageSpace {
    private final int id;
    private final int startingOffset;
    private int offset;
    private long score;
    private int fileBlocks;
    private int emptySpace;

    public static List<StorageSpace> parseInputString(final String input) {
        final List<StorageSpace> result = new LinkedList<>();
        int startingOffset = 0;
        for (int i = 0; i < input.length(); i += 2) {
            final int id = i / 2;
            final int fileBlocks = UtParsing.cachedGetNumericValue(input.charAt(i));
            final int emptySpace = i < input.length() - 1
                    ? UtParsing.cachedGetNumericValue(input.charAt(i + 1))
                    : 0;
            result.add(new StorageSpace(id, startingOffset, fileBlocks, emptySpace));
            startingOffset += result.getLast().getLength();
        }
        return result;
    }

    private StorageSpace(final int id, final int startingOffset, final int fileBlocks, final int emptySpace) {
        this.id = id;
        this.startingOffset = startingOffset;
        this.offset = startingOffset;
        this.score = 0;
        this.fileBlocks = fileBlocks;
        this.emptySpace = fileBlocks + emptySpace;
        addFileBlocks(fileBlocks, id);
    }

    public int getId() {
        return id;
    }

    public long getScore() {
        return score;
    }

    private void setScore(final long score) {
        this.score = score;
    }

    private void increaseScore(final long increase) {
        setScore(UtMath.superOverflowSafeSum(getScore(), increase));
    }

    public int getFileBlocks() {
        return fileBlocks;
    }

    private void setFileBlocks(final int fileBlocks) {
        this.fileBlocks = fileBlocks;
    }

    public int removeFileBlocksAsAble(final int requestedFileBlocks) {
        final int availableFileBlocks = Math.min(requestedFileBlocks, getFileBlocks());
        final int offsetAfterRemoving = getOffset() - requestedFileBlocks;
        final long totalBlockPositions = determineTotalBlockPositions(offsetAfterRemoving, getOffset());
        setScore(getScore() - getId() * totalBlockPositions);
        setOffset(offsetAfterRemoving);
        setFileBlocks(getFileBlocks() - availableFileBlocks);
        return availableFileBlocks;
    }

    public void moveOriginalFileBlocksTo(final StorageSpace other) {
        other.addFileBlocks(getFileBlocks(), getId());
        removeOriginalFileBlocks();
    }

    public void removeOriginalFileBlocks() {
        final long totalBlockPositions = determineTotalBlockPositions(getStartingOffset(), getStartingOffset() + getFileBlocks());
        setScore(getScore() - getId() * totalBlockPositions);
    }

    public void addFileBlocks(final int fileBlocks, final int fileId) {
        final int offsetAfterAdding = getOffset() + fileBlocks;
        final long totalBlockPositions = determineTotalBlockPositions(getOffset(), offsetAfterAdding);
        increaseScore(fileId * totalBlockPositions);
        setOffset(offsetAfterAdding);
        removeEmptySpace(fileBlocks);
    }

    private long determineTotalBlockPositions(final int start, final int end) {
        return UtMath.triangularNumber(end - 1) - UtMath.triangularNumber( start - 1);
    }

    private boolean hasFileBlocks() {
        return getFileBlocks() > 0;
    }

    public boolean isEmpty() {
        return !hasFileBlocks();
    }

    public int getEmptySpace() {
        return emptySpace;
    }

    private void setEmptySpace(final int emptySpace) {
        this.emptySpace = emptySpace;
    }

    private void removeEmptySpace(final int emptySpace) {
        setEmptySpace(getEmptySpace() - emptySpace);
    }

    private boolean hasEmptySpace() {
        return getEmptySpace() > 0;
    }

    public boolean isFull() {
        return !hasEmptySpace();
    }

    private int getLength() {
        return getFileBlocks() + getEmptySpace();
    }

    private int getOffset() {
        return offset;
    }

    private void setOffset(final int offset) {
        this.offset = offset;
    }

    public boolean canFit(final StorageSpace other) {
        return this != other && getEmptySpace() >= other.getFileBlocks();
    }

    private int getStartingOffset() {
        return startingOffset;
    }
}

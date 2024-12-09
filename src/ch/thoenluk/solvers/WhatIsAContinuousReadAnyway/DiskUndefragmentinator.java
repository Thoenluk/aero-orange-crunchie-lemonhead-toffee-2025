package ch.thoenluk.solvers.WhatIsAContinuousReadAnyway;

import ch.thoenluk.ChristmasSaver;
import ch.thoenluk.Day;
import ch.thoenluk.ut.UtMath;

import java.util.LinkedList;
import java.util.List;

@Day(9)
public class DiskUndefragmentinator implements ChristmasSaver {
    @Override
    public String saveChristmas(final String input) {
        final List<StorageSpace> storageSpacesToProcess = StorageSpace.parseInputString(input);
        final List<StorageSpace> processedStorageSpaces = new LinkedList<>();
        while (!storageSpacesToProcess.isEmpty()) {
            markAllFullStorageSpacesAsProcessed(storageSpacesToProcess, processedStorageSpaces);
            final StorageSpace toFill = storageSpacesToProcess.getFirst();
            final StorageSpace toDrain = removeAllEmptyStorageSpacesFromEndAndFindFirstWithFileBlocks(storageSpacesToProcess);
            if (toFill == toDrain) {
                storageSpacesToProcess.remove(toFill);
                processedStorageSpaces.add(toFill);
            } else {
                moveAsManyFileBlocksAsPossible(toFill, toDrain);
            }
        }
        return UtMath.restOfTheLongOwl(processedStorageSpaces.stream().map(StorageSpace::getScore));
    }

    private static void moveAsManyFileBlocksAsPossible(final StorageSpace toFill, final StorageSpace toDrain) {
        final int availableSpace = toFill.getEmptySpace();
        final int availableBlocks = toDrain.removeFileBlocksAsAble(availableSpace);
        toFill.addFileBlocks(availableBlocks, toDrain.getId());
    }

    private static void markAllFullStorageSpacesAsProcessed(final List<StorageSpace> storageSpacesToProcess, final List<StorageSpace> processedStorageSpaces) {
        while (!storageSpacesToProcess.isEmpty()
                && storageSpacesToProcess.getFirst().isFull()) {
            processedStorageSpaces.add(storageSpacesToProcess.removeFirst());
        }
    }

    private StorageSpace removeAllEmptyStorageSpacesFromEndAndFindFirstWithFileBlocks(final List<StorageSpace> storageSpacesToProcess) {
        while (!storageSpacesToProcess.isEmpty() && storageSpacesToProcess.getLast().isEmpty()) {
            storageSpacesToProcess.removeLast();
        }
        return storageSpacesToProcess.getLast();
    }

    @Override
    public String saveChristmasAgain(final String input) {
        final List<StorageSpace> storageSpacesToProcess = StorageSpace.parseInputString(input);
        final List<StorageSpace> processedStorageSpaces = new LinkedList<>();
        while (!storageSpacesToProcess.isEmpty()) {
            final StorageSpace toMove = storageSpacesToProcess.removeLast();
            storageSpacesToProcess.stream()
                    .filter(space -> space.canFit(toMove))
                    .findFirst()
                    .ifPresent(toMove::moveOriginalFileBlocksTo);
            processedStorageSpaces.add(toMove);
            markAllFullStorageSpacesAsProcessed(storageSpacesToProcess, processedStorageSpaces);
        }
        return UtMath.restOfTheLongOwl(processedStorageSpaces.stream().map(StorageSpace::getScore));
    }
}

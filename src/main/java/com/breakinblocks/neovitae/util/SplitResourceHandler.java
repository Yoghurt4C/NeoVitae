package com.breakinblocks.neovitae.util;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SplitResourceHandler<T extends Resource> implements ResourceHandler<T> {

    private final ResourceHandler<T> insertInto;
    private final ResourceHandler<T> extractFrom;

    public SplitResourceHandler(ResourceHandler<T> insertInto, ResourceHandler<T> extractFrom) {
        this.insertInto = insertInto;
        this.extractFrom = extractFrom;
    }

    private boolean isInsertIndex(int index) {
        return index < insertInto.size();
    }

    private int extractIndex(int index) {
        return index - insertInto.size();
    }

    @Override
    public int size() {
        return insertInto.size() + extractFrom.size();
    }

    @Override
    public T getResource(int index) {
        return isInsertIndex(index) ? insertInto.getResource(index) : extractFrom.getResource(extractIndex(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return isInsertIndex(index) ? insertInto.getAmountAsLong(index) : extractFrom.getAmountAsLong(extractIndex(index));
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        return isInsertIndex(index) ? insertInto.getCapacityAsLong(index, resource) : extractFrom.getCapacityAsLong(extractIndex(index), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return isInsertIndex(index) && insertInto.isValid(index, resource);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        return isInsertIndex(index) ? insertInto.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        return isInsertIndex(index) ? 0 : extractFrom.extract(extractIndex(index), resource, amount, transaction);
    }
}

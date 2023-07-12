package org.makkiato.arcadeclient.data.operations;

public interface ArcadedbOperations extends BasicOperations, ConversionAwareOperations {
    TransactionalOperations transactional();
}

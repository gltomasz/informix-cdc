package com.example;

class CdcEvent {
    private final long sequenceId;
    private final int transactionId;

    public CdcEvent(long sequenceId, int transactionId) {
        this.sequenceId = sequenceId;
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "CdcEvent{" +
                "sequenceId=" + sequenceId +
                ", transactionId=" + transactionId +
                '}';
    }
}

package com.company.server;

public enum ThreadState {
    UNINITIALIZED, INITIALIZING, INITIALIZED, STOPPING, STOPPED, IRQ_FOR_STOP, RUNNING
}

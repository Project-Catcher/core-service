package com.catcher.core.domain.command;

public interface Command <T>{
    T execute();
}

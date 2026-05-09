package com.relaycore.ingester.util;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

import java.util.UUID;

public class IdGenerator {
    private static final NoArgGenerator generator = Generators.timeBasedEpochGenerator();

    public static UUID nextId() {
        return generator.generate();
    }
}

package org.obapanel.lockfactoryserver.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum LockStatus {

    ABSENT, // No lock has been found
    UNLOCKED, // Lock exists and its unlocked
    OWNER, // Lock exists, is locked by caller
    OTHER; // Lock exists, is locked by other

    public static List<LockStatus> ABSENT_OR_UNLOCKED = Collections.unmodifiableList(Arrays.asList(ABSENT, UNLOCKED));

}

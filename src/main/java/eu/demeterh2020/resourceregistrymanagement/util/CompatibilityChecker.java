package eu.demeterh2020.resourceregistrymanagement.util;

import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;

public interface CompatibilityChecker {

    public boolean checkCompatibility(DEHResource dehResource);
}

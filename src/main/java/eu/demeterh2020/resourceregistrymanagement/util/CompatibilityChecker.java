package eu.demeterh2020.resourceregistrymanagement.util;

import eu.demeterh2020.resourceregistrymanagement.domain.DehResource;

public interface CompatibilityChecker {

    public boolean checkCompatibility(DehResource dehResource);
}

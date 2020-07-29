package eu.demeterh2020.resourceregistrymanagement.util;

import eu.demeterh2020.resourceregistrymanagement.domain.DEHResource;
import org.springframework.stereotype.Service;

@Service
public class CompatibilityCheckerImpl implements CompatibilityChecker {

    //TODO Change implementation based on real CompatibilityChecker

    public boolean checkCompatibility(DEHResource dehResource){
        return true;
    }
}

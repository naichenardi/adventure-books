package com.adventurebooks.validation;

import java.util.List;

public record BookValidationResult(boolean valid, List<String> errors) {
}

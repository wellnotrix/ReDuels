package dev.veltrix.duels.util.validator;

public interface Validator<T> {
    
    boolean shouldValidate();

    boolean validate(final T validated);
}

package com.chubini.pku.dishes;

public class DishCalculationException extends RuntimeException {
    
    public DishCalculationException(String message) {
        super(message);
    }
    
    public DishCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}

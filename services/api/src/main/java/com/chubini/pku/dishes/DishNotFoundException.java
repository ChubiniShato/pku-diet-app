package com.chubini.pku.dishes;

import java.util.UUID;

public class DishNotFoundException extends RuntimeException {
    
    public DishNotFoundException(UUID dishId) {
        super("Dish not found with ID: " + dishId);
    }
    
    public DishNotFoundException(String message) {
        super(message);
    }
}

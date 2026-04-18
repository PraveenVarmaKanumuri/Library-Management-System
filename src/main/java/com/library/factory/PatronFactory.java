package com.library.factory;

import com.library.model.Patron;
import com.library.util.IdGenerator;
import com.library.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatronFactory {

    private static final Logger logger = LoggerFactory.getLogger(PatronFactory.class);

    public static Patron create(String name, String email, String phone) {
        // Validate inputs
        Validator.validateNotEmpty(name, "Name");
        Validator.validateEmail(email);
        Validator.validatePhone(phone);

        String id = IdGenerator.generatePatronId();
        logger.info("Creating new patron: {} with ID: {}", name, id);
        return new Patron(id, name, email, phone);
    }
}
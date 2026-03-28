package com.demo.constants;

import java.util.Set;

public final class HeaderConstants {

    private HeaderConstants() {}

    public static final String ENV_HEADER       = "env";
    public static final String CORRELATION_ID   = "X-Correlation-Id";

    /** Allowed environment values — must match your Spring profiles */
    public static final Set<String> ALLOWED_ENVIRONMENTS =
            Set.of("local", "dev", "qa", "prd");
}




@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MandatoryHeaderForNonProd {
    String header();
    String path();   // endpoint path where header is required
}

@Aspect
@Component
@Profile({"dev", "qa", "perf"})
public class MandatoryHeaderAspect {

    private final HttpServletRequest request;

    public MandatoryHeaderAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Before("@annotation(config)")
    public void validateHeader(MandatoryHeaderForNonProd config) {

        String requestUri = request.getRequestURI();

        // Apply ONLY for endpointA
        if (requestUri.endsWith(config.path())) {

            String headerValue = request.getHeader(config.header());

            if (headerValue == null || headerValue.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        config.header() + " is mandatory for " + config.path()
                );
            }
        }
    }
}

___
@Component
@Profile({"dev", "qa", "perf"})
public class MandatoryHeaderWebFilter implements WebFilter {

    private final Map<String, String> protectedPaths = Map.of(
            "/api/endpointA", "X-SPECIAL-HEADER"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (protectedPaths.containsKey(path)) {

            String headerName = protectedPaths.get(path);
            String headerValue = exchange.getRequest().getHeaders().getFirst(headerName);

            if (headerValue == null || headerValue.isBlank()) {
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }
}


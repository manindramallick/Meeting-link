
https://github.com/salesforce/ja3

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MandatoryHeaderForNonProd {
    String header();
    String path();   // endpoint path where header is required
}




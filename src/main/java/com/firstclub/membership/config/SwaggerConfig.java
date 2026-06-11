package com.firstclub.membership.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI membershipOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FirstClub Membership Program API")
                        .description("""
                                Backend API for FirstClub's tiered membership subscription system.
                                
                                **Features:**
                                - Browse Monthly / Quarterly / Yearly plans
                                - Subscribe with Silver / Gold / Platinum tier selection
                                - Upgrade or downgrade membership tier
                                - Checkout benefit computation (discounts + free delivery)
                                - Automatic tier promotion based on order activity
                                - Full membership history audit trail
                                
                                **H2 Console:** http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:membershipdb`, User: `sa`)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FirstClub Engineering")
                                .email("engineering@firstclub.com"))
                        .license(new License()
                                .name("Proprietary")));
    }
}

package com.btlab.fdcalculator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI/Swagger documentation
 * Access Swagger UI at: http://localhost:8081/swagger-ui.html
 * Access API Docs at: http://localhost:8081/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI fdCalculatorOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort);
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.nexabank.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setEmail("support@nexabank.com");
        contact.setName("NEXA Bank API Support");
        contact.setUrl("https://www.nexabank.com");

        License license = new License()
                .name("Proprietary")
                .url("https://www.nexabank.com/license");

        Info info = new Info()
                .title("NEXA Bank - Fixed Deposit Calculator API")
                .version("1.0.0")
                .contact(contact)
                .description("""
                    ## Overview
                    The FD Calculator API provides comprehensive fixed deposit calculation services for NEXA Bank customers.
                    
                    ## Key Features
                    - **Cumulative FD**: Calculate maturity value with compound interest
                    - **Non-Cumulative FD**: Calculate periodic payouts with configurable frequency
                    - **Dynamic Interest Rates**: Real-time rate fetching from Product & Pricing API
                    - **Category Benefits**: Support for senior citizens, gold/silver/platinum customers, and employees
                    - **Multiple Compounding Options**: Daily, Monthly, Quarterly, and Yearly compounding
                    - **APY Calculation**: Accurate Annual Percentage Yield with compounding
                    
                    ## Calculation Types
                    
                    ### Cumulative FD
                    Interest is compounded and reinvested. Customer receives the full amount at maturity.
                    - **Formula**: A = P(1 + r/n)^(nt)
                    - **Returns**: maturity_value, maturity_date, apy, effective_rate
                    
                    ### Non-Cumulative FD
                    Interest is paid out periodically (monthly/quarterly/yearly). Principal remains constant.
                    - **Formula**: Payout = P × [(1 + r/m)^n - 1]
                    - **Returns**: payout_freq, payout_amount, maturity_value (principal), apy, effective_rate
                    
                    ## Interest Rate Structure
                    Base interest rates are tenure-based:
                    - **0-12 months**: INT12M001
                    - **12-24 months**: INT24M001
                    - **24-36 months**: INT36M001
                    - **36+ months**: INT60M001
                    
                    ## Category Benefits
                    Additional interest rates based on customer categories:
                    - **SENIOR**: Senior Citizen (0.75% extra)
                    - **JR**: Junior Citizen (0.50% extra)
                    - **GOLD**: Gold Customer (0.25% extra)
                    - **SILVER**: Silver Customer (0.15% extra)
                    - **PLAT**: Platinum Customer (0.35% extra)
                    - **EMP**: Employee (1.00% extra)
                    - **DY**: Digi youth (1.25% extra)
                    
                    ## Product Rules Validation
                    Each FD is validated against product-specific rules:
                    - Minimum amount (MIN001)
                    - Maximum amount (MAX001)
                    - Maximum interest rate (MAXINT001)
                    
                    ## Example Use Cases
                    
                    ### Example 1: Senior Citizen - Cumulative FD
                    ```json
                    {
                      "principal_amount": 100000,
                      "tenure_value": 5,
                      "tenure_unit": "YEARS",
                      "interest_type": "COMPOUND",
                      "compounding_frequency": "QUARTERLY",
                      "category1_id": "SENIOR",
                      "cumulative": true,
                      "product_code": "FD001"
                    }
                    ```
                    
                    ### Example 2: Gold Customer - Non-Cumulative with Yearly Payout
                    ```json
                    {
                      "principal_amount": 50000,
                      "tenure_value": 3,
                      "tenure_unit": "YEARS",
                      "interest_type": "COMPOUND",
                      "compounding_frequency": "QUARTERLY",
                      "category1_id": "GOLD",
                      "cumulative": false,
                      "payout_freq": "YEARLY",
                      "product_code": "FD001"
                    }
                    ```
                    
                    ## Response Codes
                    - **200**: Successful calculation
                    - **400**: Invalid request parameters
                    - **500**: Server error (e.g., Product API unavailable)
                    
                    ## Rate Caching
                    Interest rates are cached and refreshed periodically to ensure optimal performance.
                    Cache refresh can be triggered via the admin endpoint.
                    
                    ## Integration
                    This API integrates with:
                    - **Product & Pricing API** (port 8080): For product rules and interest rates
                    - **MySQL Database**: For calculation history and rate caching
                    """)
                .termsOfService("https://www.nexabank.com/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}

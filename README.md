# CoreFlow ERP Frontend

React + Vite frontend starter for the CoreFlow ERP backend.

## Install
```bash
npm install
npm install axios lucide-react
npm run dev
```

Open http://localhost:5173. Start the Spring Boot backend at http://localhost:8080 first.

## Backend CORS
Add `WebConfig.java` under `src/main/java/lk/samson/coreflow/config/`:

```java
package lk.samson.coreflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

If the backend already has CORS configuration, update that configuration rather than creating conflicting rules.

## Notes
- This UI calls the current backend API paths: `/products`, `/categories`, `/uom`, `/warehouses`, `/locations`, and `/inventory/...`.
- This demo frontend assumes list endpoints return JSON arrays.
- The UI does not implement authentication. Do not expose this development build publicly with real business data.
- Confirm DTO field names against the backend before final submission.

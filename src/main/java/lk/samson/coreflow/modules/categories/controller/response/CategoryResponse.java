
package lk.samson.coreflow.modules.categories.controller.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
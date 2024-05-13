package com.github.feddericokz.gptassistant.actions.handlers;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class AssistantResponseHandlerTest {

    List<String> assistantResponse = Collections.singletonList("""
            <user-request>
            Create a service, a repository, and a controller for a "Settings" entity in Java, following the provided code examples.
            </user-request>
                        
            <imports>
            org.springframework.data.jpa.repository.JpaRepository, org.springframework.stereotype.Repository, org.springframework.stereotype.Service, org.springframework.web.bind.annotation.RequestMapping, org.springframework.web.bind.annotation.RestController, com.fg.grow_control.entity.Setting, com.fg.grow_control.service.BasicService
            </imports>
                        
            <file-creation path="com/fg/grow_control/repository/SettingRepository.java">
            package com.fg.grow_control.repository;
                        
            import com.fg.grow_control.entity.Setting;
            import org.springframework.data.jpa.repository.JpaRepository;
            import org.springframework.stereotype.Repository;
                        
            @Repository
            public interface SettingRepository extends JpaRepository<Setting, Long> {
            }
            </file-creation>
                        
            <file-creation path="com/fg/grow_control/service/SettingService.java">
            package com.fg.grow_control.service;
                        
            import com.fg.grow_control.entity.Setting;
            import com.fg.grow_control.repository.SettingRepository;
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.stereotype.Service;
                        
            @Service
            public class SettingService extends BasicService<Setting, Long, SettingRepository> {
                        
                @Autowired
                public SettingService(SettingRepository repository) {
                    super(repository);
                }
                        
                // Add any setting-specific methods here
            }
            </file-creation>
                        
            <file-creation path="com/fg/grow_control/controller/SettingController.java">
            package com.fg.grow_control.controller;
                        
            import com.fg.grow_control.entity.Setting;
            import com.fg.grow_control.repository.SettingRepository;
            import com.fg.grow_control.service.SettingService;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RestController;
                        
            @RestController
            @RequestMapping("/settings")
            public class SettingController extends BasicController<Setting, Long, SettingRepository, SettingService> {
                public SettingController(SettingService service) {
                    super(service);
                }
                        
                // Add any setting-specific endpoints here
            }
            </file-creation>
                        
            <steps>
            1. Understand the structure and format of the provided code examples, focusing on the entity, service, repository, and controller layers.
            2. Create the SettingRepository interface extending JpaRepository, enabling basic CRUD operations for the Setting entity.
            3. Develop the SettingService class extending BasicService, injecting the SettingRepository to utilize its functions and potentially add more complex business logic.
            4. Implement the SettingController REST controller, mapping basic CRUD operations and potentially more complex endpoints for the Setting entity.
            5. Use annotations such as @Service, @Repository, and @RestController to denote the components' roles within the Spring Framework.
            6. Map the SettingController to a specific route for API access, exemplified with "/settings".
            7. Add placeholders for any setting-specific methods or endpoints that may be required, providing an entry point for future expansion.
            </steps>
            """);

    private final String fileCreationTagContent = """
            package com.fg.grow_control.repository;
                        
            import com.fg.grow_control.entity.Setting;
            import org.springframework.data.jpa.repository.JpaRepository;
            import org.springframework.stereotype.Repository;
                        
            @Repository
            public interface SettingRepository extends JpaRepository<Setting, Long> {
            }
            """;

    private final String codeReplacementContent = """
            
            1. Understand the structure and format of the provided code examples, focusing on the entity, service, repository, and controller layers.
            2. Create the SettingRepository interface extending JpaRepository, enabling basic CRUD operations for the Setting entity.
            3. Develop the SettingService class extending BasicService, injecting the SettingRepository to utilize its functions and potentially add more complex business logic.
            4. Implement the SettingController REST controller, mapping basic CRUD operations and potentially more complex endpoints for the Setting entity.
            5. Use annotations such as @Service, @Repository, and @RestController to denote the components' roles within the Spring Framework.
            6. Map the SettingController to a specific route for API access, exemplified with "/settings".
            7. Add placeholders for any setting-specific methods or endpoints that may be required, providing an entry point for future expansion.
            """;

    @Test
    public void testContentExtractionFromXmlTagWithAttributes() {
        List<String> extractedContentList = AssistantResponseHandler.getXmlTagContentListFromResponse(assistantResponse, "file-creation");
        Assert.assertTrue(extractedContentList.get(0).contains(fileCreationTagContent));
        Assert.assertTrue(extractedContentList.size() > 1);

        String filePath = AssistantResponseHandler.getXmlAttribute(extractedContentList.get(0), "file-creation", "path");
        Assert.assertEquals("com/fg/grow_control/repository/SettingRepository.java", filePath);
    }

    @Test
    public void testContentExtractionFromXmlTagWithoutAttributes() {
        String extractedContent = AssistantResponseHandler.getXmlTagContentFromResponse(assistantResponse, "steps");
        Assert.assertEquals(codeReplacementContent, extractedContent);
    }

}

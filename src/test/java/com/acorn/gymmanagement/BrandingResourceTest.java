package com.acorn.gymmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class BrandingResourceTest {

    @Test
    void templatesDoNotContainThePreviousBrand() throws IOException {
        Resource[] templates = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:templates/**/*.html");

        assertThat(templates).isNotEmpty();
        for (Resource template : templates) {
            String content = template.getContentAsString(StandardCharsets.UTF_8);
            assertThat(content).as(template.getFilename())
                    .doesNotContainIgnoringCase("fitflow")
                    .doesNotContain("핏플로우");
        }
    }

    @Test
    void sharedBrandMarksUseGymrovia() throws IOException {
        for (String path : new String[] {
                "templates/auth/login.html",
                "templates/fragments/layout.html",
                "templates/trainer/fragments.html",
                "templates/member/portal-fragments.html",
                "templates/error/error.html"
        }) {
            assertThat(read(path)).as(path)
                    .contains("Gymrovia", ">G</span>")
                    .doesNotContain(">F</span>");
        }
    }

    @Test
    void errorPageAndStylesheetUseTheSameBrandSelector() throws IOException {
        assertThat(read("templates/error/error.html"))
                .contains("class=\"gymrovia-error-page\"");
        assertThat(read("static/css/pages/error/error.css"))
                .contains(".gymrovia-error-page")
                .doesNotContain(".fitflow-error-page");
    }

    private String read(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}

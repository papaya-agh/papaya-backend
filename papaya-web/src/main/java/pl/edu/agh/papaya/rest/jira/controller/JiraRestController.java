package pl.edu.agh.papaya.rest.jira.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.JiraConfigDto;
import pl.edu.agh.papaya.api.service.JiraApi;

@RestController
@RequiredArgsConstructor
public class JiraRestController implements JiraApi {

    @Value("${jira.auth.public.key}")
    private String publicKey;

    @Override
    public ResponseEntity<JiraConfigDto> getJiraKey() {
        return ResponseEntity.ok(new JiraConfigDto().key(publicKey));
    }
}

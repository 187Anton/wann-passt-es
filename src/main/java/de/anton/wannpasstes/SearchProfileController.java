package de.anton.wannpasstes;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchProfileController {

    private final SearchProfileRepository repository;

    public SearchProfileController(SearchProfileRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/profiles")
    public String listProfiles(Model model) {
        model.addAttribute(
                "profiles",
                repository.findAll(Sort.by("name", "id")));

        return "profiles";
    }
}
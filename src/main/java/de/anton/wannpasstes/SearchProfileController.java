package de.anton.wannpasstes;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.constraints.Min;


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

    @PostMapping("/profiles")
    public String saveProfile(
            @RequestParam(name = "profileName", defaultValue = "")
            String profileName,
            @RequestParam(name = "locationId")
            @Min(1) int locationId,
            @RequestParam(name = "activity")
            Activity activity,
            @RequestParam(name = "minDurationMinutes")
            @Min(1) int minDurationMinutes,
            @RequestParam(name = "temperaturePreference")
            TemperaturePreference temperaturePreference,
            @RequestParam(name = "windPreference")
            WindPreference windPreference,
            @RequestParam(name = "precipitationPreference")
            PrecipitationPreference precipitationPreference,
            @RequestParam(name = "searchDate", required = false)
            String searchDate,
            RedirectAttributes redirectAttributes) {

        String name = profileName.strip();

        if (name.isBlank() || name.length() > 100) {
            redirectAttributes.addFlashAttribute(
                    "profileError",
                    "Bitte gib einen Profilnamen mit 1 bis 100 Zeichen ein.");
            redirectAttributes.addFlashAttribute("profileName", profileName);

            redirectAttributes.addAttribute("locationId", locationId);
            redirectAttributes.addAttribute("activity", activity);
            redirectAttributes.addAttribute(
                    "minDurationMinutes", minDurationMinutes);
            redirectAttributes.addAttribute(
                    "temperaturePreference", temperaturePreference);
            redirectAttributes.addAttribute("windPreference", windPreference);
            redirectAttributes.addAttribute(
                    "precipitationPreference", precipitationPreference);

            if (searchDate != null && !searchDate.isBlank()) {
                redirectAttributes.addAttribute("searchDate", searchDate);
            }

            return "redirect:/";
        }

        SearchProfile profile = new SearchProfile(
                name,
                locationId,
                activity,
                minDurationMinutes,
                temperaturePreference,
                windPreference,
                precipitationPreference);

        repository.save(profile);

        redirectAttributes.addFlashAttribute(
                "profileSuccess",
                "Das Suchprofil wurde gespeichert.");

        return "redirect:/profiles";
    }
}
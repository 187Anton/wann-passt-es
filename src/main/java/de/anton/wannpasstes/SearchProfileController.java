package de.anton.wannpasstes;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.constraints.Min;

import java.util.Optional;


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
                repository.findAll(Sort.by(
                        Sort.Order.asc("name").ignoreCase(),
                        Sort.Order.asc("id"))));

        return "profiles";
    }

    /**
     * Saves a profile without location name and date range (legacy single-day form).
     */
    public String saveProfile(
            String profileName,
            int locationId,
            Activity activity,
            int minDurationMinutes,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference,
            String searchDate,
            RedirectAttributes redirectAttributes) {

        return saveProfile(profileName, locationId, null, activity, minDurationMinutes,
                temperaturePreference, windPreference, precipitationPreference,
                searchDate, null, null, redirectAttributes);
    }

    @PostMapping("/profiles")
    public String saveProfile(
            @RequestParam(name = "profileName", defaultValue = "")
            String profileName,
            @RequestParam(name = "locationId")
            @Min(1) int locationId,
            @RequestParam(name = "locationName", required = false)
            String locationName,
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
            @RequestParam(name = "startDate", required = false)
            String startDate,
            @RequestParam(name = "endDate", required = false)
            String endDate,
            RedirectAttributes redirectAttributes) {

        Optional<String> nameError = ProfileNames.validate(profileName, repository, null);

        if (nameError.isPresent()) {
            redirectAttributes.addFlashAttribute("profileError", nameError.get());
            redirectAttributes.addFlashAttribute("profileName", profileName);

            // Keep the current selection so nothing has to be entered again.
            redirectAttributes.addAttribute("locationId", locationId);
            redirectAttributes.addAttribute("activity", activity);
            redirectAttributes.addAttribute(
                    "minDurationMinutes", minDurationMinutes);
            redirectAttributes.addAttribute(
                    "temperaturePreference", temperaturePreference);
            redirectAttributes.addAttribute("windPreference", windPreference);
            redirectAttributes.addAttribute(
                    "precipitationPreference", precipitationPreference);

            addIfPresent(redirectAttributes, "searchDate", searchDate);
            addIfPresent(redirectAttributes, "startDate", startDate);
            addIfPresent(redirectAttributes, "endDate", endDate);

            return "redirect:/";
        }

        String name = ProfileNames.normalize(profileName);

        SearchProfile profile = new SearchProfile(
                name,
                locationId,
                locationName,
                activity,
                minDurationMinutes,
                temperaturePreference,
                windPreference,
                precipitationPreference);

        repository.save(profile);

        redirectAttributes.addFlashAttribute(
                "profileSuccess",
                "Das Suchprofil „" + name + "“ wurde gespeichert.");

        return "redirect:/profiles";
    }

    @PostMapping("/profiles/delete")
    public String deleteProfile(
            @RequestParam(name = "id") @Min(1) long id,
            RedirectAttributes redirectAttributes) {

        boolean existed = repository.existsById(id);
        // deleteById is a no-op for unknown IDs, so a concurrent deletion is harmless.
        repository.deleteById(id);

        if (existed) {
            redirectAttributes.addFlashAttribute(
                    "profileSuccess",
                    "Das Suchprofil wurde entfernt.");
        } else {
            redirectAttributes.addFlashAttribute(
                    "profileError",
                    "Dieses Suchprofil gibt es nicht (mehr). Vielleicht wurde es bereits gelöscht.");
        }

        return "redirect:/profiles";
    }

    private static void addIfPresent(RedirectAttributes redirectAttributes, String name, String value) {
        if (value != null && !value.isBlank()) {
            redirectAttributes.addAttribute(name, value);
        }
    }
}

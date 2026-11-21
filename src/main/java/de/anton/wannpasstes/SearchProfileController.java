package de.anton.wannpasstes;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.constraints.Min;


@Controller
public class SearchProfileController {

    static final int MAX_NAME_LENGTH = 100;

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
            @RequestParam(name = "locationName", required = false, defaultValue = "")
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
            @RequestParam(name = "startDate", required = false)
            String startDate,
            @RequestParam(name = "endDate", required = false)
            String endDate,
            @RequestParam(name = "searchDate", required = false)
            String searchDate,
            RedirectAttributes redirectAttributes) {

        String name = profileName.strip();

        if (!isValidName(name)) {
            redirectAttributes.addFlashAttribute(
                    "profileError",
                    "Bitte gib einen Profilnamen mit 1 bis " + MAX_NAME_LENGTH + " Zeichen ein.");
            return backToPlanning(redirectAttributes, profileName, locationId,
                    locationName, activity, minDurationMinutes,
                    temperaturePreference, windPreference,
                    precipitationPreference, startDate, endDate, searchDate);
        }

        if (repository.existsByNameIgnoreCase(name)) {
            redirectAttributes.addFlashAttribute(
                    "profileError",
                    "Ein Suchprofil mit dem Namen „" + name + "“ gibt es bereits. "
                            + "Bitte wähle einen anderen Namen.");
            return backToPlanning(redirectAttributes, profileName, locationId,
                    locationName, activity, minDurationMinutes,
                    temperaturePreference, windPreference,
                    precipitationPreference, startDate, endDate, searchDate);
        }

        repository.save(new SearchProfile(
                name,
                locationId,
                normalizeLocationName(locationName),
                activity,
                minDurationMinutes,
                temperaturePreference,
                windPreference,
                precipitationPreference));

        redirectAttributes.addFlashAttribute(
                "profileSuccess",
                "Das Suchprofil „" + name + "“ wurde gespeichert.");

        return "redirect:/profiles";
    }

    @GetMapping("/profiles/{id}/edit")
    public String editProfile(
            @PathVariable("id") long id,
            @RequestParam(name = "locationId", required = false) Integer locationId,
            @RequestParam(name = "locationName", required = false) String locationName,
            Model model,
            RedirectAttributes redirectAttributes) {

        SearchProfile profile = repository.findById(id).orElse(null);

        if (profile == null) {
            redirectAttributes.addFlashAttribute(
                    "profileError",
                    "Das Suchprofil wurde nicht gefunden. Vielleicht wurde es bereits gelöscht.");
            return "redirect:/profiles";
        }

        int selectedLocationId = locationId == null
                ? profile.getLocationId() : locationId;
        String selectedLocationName;
        if (locationName != null && !locationName.isBlank()) {
            selectedLocationName = locationName.strip();
        } else if (selectedLocationId == profile.getLocationId()) {
            selectedLocationName = profile.getLocationName();
        } else {
            selectedLocationName = null;
        }

        addEditForm(model, profile, profile.getName(), selectedLocationId,
                selectedLocationName, profile.getActivity(),
                profile.getMinDurationMinutes(),
                profile.getTemperaturePreference(),
                profile.getWindPreference(),
                profile.getPrecipitationPreference());

        return "profile-edit";
    }

    @PostMapping("/profiles/{id}/edit")
    public String updateProfile(
            @PathVariable("id") long id,
            @RequestParam(name = "profileName", defaultValue = "")
            String profileName,
            @RequestParam(name = "locationId")
            @Min(1) int locationId,
            @RequestParam(name = "locationName", required = false, defaultValue = "")
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
            Model model,
            RedirectAttributes redirectAttributes) {

        SearchProfile profile = repository.findById(id).orElse(null);

        if (profile == null) {
            redirectAttributes.addFlashAttribute(
                    "profileError",
                    "Das Suchprofil wurde nicht gefunden. Vielleicht wurde es bereits gelöscht.");
            return "redirect:/profiles";
        }

        String name = profileName.strip();
        String error = null;

        if (!isValidName(name)) {
            error = "Bitte gib einen Profilnamen mit 1 bis " + MAX_NAME_LENGTH + " Zeichen ein.";
        } else if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            error = "Ein anderes Suchprofil heißt bereits „" + name + "“. "
                    + "Bitte wähle einen anderen Namen.";
        }

        if (error != null) {
            model.addAttribute("profileError", error);
            addEditForm(model, profile, profileName, locationId, locationName,
                    activity, minDurationMinutes, temperaturePreference,
                    windPreference, precipitationPreference);
            return "profile-edit";
        }

        profile.update(name, locationId, normalizeLocationName(locationName),
                activity, minDurationMinutes, temperaturePreference,
                windPreference, precipitationPreference);
        repository.save(profile);

        redirectAttributes.addFlashAttribute(
                "profileSuccess",
                "Das Suchprofil „" + name + "“ wurde aktualisiert.");

        return "redirect:/profiles";
    }

    @PostMapping("/profiles/delete")
    public String deleteProfile(
            @RequestParam(name = "id") @Min(1) long id,
            RedirectAttributes redirectAttributes) {

        SearchProfile profile = repository.findById(id).orElse(null);

        if (profile == null) {
            redirectAttributes.addFlashAttribute(
                    "profileError",
                    "Das Suchprofil wurde nicht gefunden. Vielleicht wurde es bereits gelöscht.");
            return "redirect:/profiles";
        }

        repository.delete(profile);

        redirectAttributes.addFlashAttribute(
                "profileSuccess",
                "Das Suchprofil „" + profile.getName() + "“ wurde entfernt.");

        return "redirect:/profiles";
    }

    private String backToPlanning(
            RedirectAttributes redirectAttributes,
            String profileName,
            int locationId,
            String locationName,
            Activity activity,
            int minDurationMinutes,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference,
            String startDate,
            String endDate,
            String searchDate) {

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

        if (locationName != null && !locationName.isBlank()) {
            redirectAttributes.addAttribute("locationName", locationName.strip());
        }
        if (startDate != null && !startDate.isBlank()) {
            redirectAttributes.addAttribute("startDate", startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            redirectAttributes.addAttribute("endDate", endDate);
        }
        if (searchDate != null && !searchDate.isBlank()) {
            redirectAttributes.addAttribute("searchDate", searchDate);
        }

        return "redirect:/";
    }

    private void addEditForm(
            Model model,
            SearchProfile profile,
            String name,
            int locationId,
            String locationName,
            Activity activity,
            int minDurationMinutes,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference) {

        model.addAttribute("profileId", profile.getId());
        model.addAttribute("profileName", name);
        model.addAttribute("locationId", locationId);
        model.addAttribute("locationName", locationName);
        model.addAttribute("activity", activity);
        model.addAttribute("activities", Activity.values());
        model.addAttribute("minDurationMinutes", minDurationMinutes);
        model.addAttribute("temperaturePreference", temperaturePreference);
        model.addAttribute("temperaturePreferences", TemperaturePreference.values());
        model.addAttribute("windPreference", windPreference);
        model.addAttribute("windPreferences", WindPreference.values());
        model.addAttribute("precipitationPreference", precipitationPreference);
        model.addAttribute("precipitationPreferences", PrecipitationPreference.values());
        model.addAttribute("maxProfileNameLength", MAX_NAME_LENGTH);
    }

    private boolean isValidName(String name) {
        return !name.isBlank() && name.length() <= MAX_NAME_LENGTH;
    }

    private String normalizeLocationName(String locationName) {
        if (locationName == null || locationName.isBlank()) {
            return null;
        }
        String stripped = locationName.strip();
        return stripped.length() > 200 ? stripped.substring(0, 200) : stripped;
    }
}

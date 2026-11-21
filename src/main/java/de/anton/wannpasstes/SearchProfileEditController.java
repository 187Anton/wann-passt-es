package de.anton.wannpasstes;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Edits existing search profiles. The location is changed via the location
 * search, which links back here with the new location and all other
 * (possibly unsaved) form values as request parameters.
 */
@Controller
public class SearchProfileEditController {

    static final String VIEW = "profile-edit";

    private final SearchProfileRepository repository;
    private final GeocodingClient geocodingClient;

    public SearchProfileEditController(SearchProfileRepository repository, GeocodingClient geocodingClient) {
        this.repository = repository;
        this.geocodingClient = geocodingClient;
    }

    @GetMapping("/profiles/{id}/edit")
    public String editProfile(
            @PathVariable("id") long id,
            @RequestParam(name = "profileName", required = false) String profileName,
            @RequestParam(name = "locationId", required = false) Integer locationId,
            @RequestParam(name = "locationName", required = false) String locationName,
            @RequestParam(name = "activity", required = false) Activity activity,
            @RequestParam(name = "minDurationMinutes", required = false) Integer minDurationMinutes,
            @RequestParam(name = "temperaturePreference", required = false)
            TemperaturePreference temperaturePreference,
            @RequestParam(name = "windPreference", required = false) WindPreference windPreference,
            @RequestParam(name = "precipitationPreference", required = false)
            PrecipitationPreference precipitationPreference,
            Model model) {

        SearchProfile profile = findProfile(id);
        ProfileForm form = ProfileForm.of(profile);

        // Values passed back from the location search override the stored ones.
        if (profileName != null) form.setProfileName(profileName);
        if (locationId != null && locationId > 0) {
            form.setLocationId(locationId);
            form.setLocationName(locationName);
        }
        if (activity != null) form.setActivity(activity);
        if (minDurationMinutes != null) form.setMinDurationMinutes(minDurationMinutes);
        if (temperaturePreference != null) form.setTemperaturePreference(temperaturePreference);
        if (windPreference != null) form.setWindPreference(windPreference);
        if (precipitationPreference != null) form.setPrecipitationPreference(precipitationPreference);

        if (!form.hasLocationName()) {
            form.setLocationName(lookUpLocationName(form.getLocationId()));
        }

        prepareModel(model, profile, form);
        return VIEW;
    }

    @PostMapping("/profiles/{id}/edit")
    public String updateProfile(
            @PathVariable("id") long id,
            @ModelAttribute("profileForm") ProfileForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response) {

        SearchProfile profile = findProfile(id);

        validate(form, bindingResult, id);

        if (bindingResult.hasErrors()) {
            if (response != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            prepareModel(model, profile, form);
            model.addAttribute("formHasErrors", true);
            if (bindingResult.hasFieldErrors("locationId")) {
                model.addAttribute("locationIdError", "Bitte wähle über die Ortssuche einen Ort aus.");
            }
            return VIEW;
        }

        String name = ProfileNames.normalize(form.getProfileName());
        String locationName = form.getLocationName();
        if ((locationName == null || locationName.isBlank())
                && form.getLocationId() == profile.getLocationId()) {
            locationName = profile.getLocationName();
        }

        profile.update(
                name,
                form.getLocationId(),
                locationName,
                form.getActivity(),
                form.getMinDurationMinutes(),
                form.getTemperaturePreference(),
                form.getWindPreference(),
                form.getPrecipitationPreference());
        repository.save(profile);

        redirectAttributes.addFlashAttribute(
                "profileSuccess",
                "Das Suchprofil „" + name + "“ wurde aktualisiert.");
        return "redirect:/profiles";
    }

    private void validate(ProfileForm form, BindingResult bindingResult, long id) {
        if (!bindingResult.hasFieldErrors("profileName")) {
            Optional<String> nameError = ProfileNames.validate(form.getProfileName(), repository, id);
            nameError.ifPresent(message -> bindingResult.rejectValue("profileName", "profileName.invalid", message));
        }
        if (!bindingResult.hasFieldErrors("locationId")
                && (form.getLocationId() == null || form.getLocationId() < 1)) {
            bindingResult.rejectValue("locationId", "locationId.missing", "Bitte wähle einen Ort aus.");
        }
        if (!bindingResult.hasFieldErrors("minDurationMinutes")
                && (form.getMinDurationMinutes() == null || form.getMinDurationMinutes() < 1)) {
            bindingResult.rejectValue("minDurationMinutes", "minDurationMinutes.invalid",
                    "Bitte gib eine Mindestdauer von mindestens 1 Minute ein.");
        }
        rejectIfMissing(bindingResult, "activity", form.getActivity(), "Bitte wähle eine Aktivität.");
        rejectIfMissing(bindingResult, "temperaturePreference", form.getTemperaturePreference(),
                "Bitte wähle einen Temperaturwunsch.");
        rejectIfMissing(bindingResult, "windPreference", form.getWindPreference(),
                "Bitte wähle einen Windwunsch.");
        rejectIfMissing(bindingResult, "precipitationPreference", form.getPrecipitationPreference(),
                "Bitte wähle einen Niederschlagswunsch.");
    }

    private static void rejectIfMissing(BindingResult bindingResult, String field, Object value, String message) {
        if (value == null && !bindingResult.hasFieldErrors(field)) {
            bindingResult.rejectValue(field, field + ".missing", message);
        }
    }

    private SearchProfile findProfile(long id) {
        return repository.findById(id).orElseThrow(() -> new ProfileNotFoundException(id));
    }

    /** Best-effort lookup for profiles saved without a location name. */
    private String lookUpLocationName(Integer locationId) {
        if (locationId == null || locationId < 1) {
            return null;
        }
        try {
            return geocodingClient.getLocation(locationId).name();
        } catch (RestClientException | IllegalArgumentException exception) {
            return null;
        }
    }

    private static void prepareModel(Model model, SearchProfile profile, ProfileForm form) {
        model.addAttribute("profileId", profile.getId());
        model.addAttribute("storedProfileName", profile.getName());
        model.addAttribute("profileForm", form);
        model.addAttribute("activities", Activity.values());
        model.addAttribute("temperaturePreferences", TemperaturePreference.values());
        model.addAttribute("windPreferences", WindPreference.values());
        model.addAttribute("precipitationPreferences", PrecipitationPreference.values());
    }
}

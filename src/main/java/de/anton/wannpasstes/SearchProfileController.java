package de.anton.wannpasstes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SearchProfileController {

    private final SearchProfileRepository repository;
    private final SearchProfileService profiles;
    private final GeocodingClient geocodingClient;

    public SearchProfileController(SearchProfileRepository repository, SearchProfileService profiles,
                                   GeocodingClient geocodingClient) {
        this.repository = repository;
        this.profiles = profiles;
        this.geocodingClient = geocodingClient;
    }

    @GetMapping("/profiles")
    public String listProfiles(Model model) {
        model.addAttribute("profiles", repository.findAll(Sort.by("name", "id")));
        return "profiles";
    }

    @PostMapping("/profiles")
    public String saveProfile(@Valid @ModelAttribute ProfileForm form,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate searchDate,
                              RedirectAttributes redirect) {
        try {
            profiles.save(null, form);
        } catch (SearchProfileService.InvalidProfile exception) {
            return returnToPlanning(form, startDate, endDate, searchDate, exception.getMessage(), redirect);
        } catch (DataIntegrityViolationException exception) {
            return returnToPlanning(form, startDate, endDate, searchDate, duplicateMessage(exception), redirect);
        }
        redirect.addFlashAttribute("profileSuccess", "Das Suchprofil wurde gespeichert.");
        return "redirect:/profiles";
    }

    @GetMapping("/profiles/{id}/edit")
    public String editProfile(@PathVariable long id, Model model, RedirectAttributes redirect) {
        SearchProfile profile = repository.findById(id).orElse(null);
        if (profile == null) {
            return missingProfile(redirect);
        }
        ProfileForm form = ProfileForm.from(profile);
        model.addAttribute("profileNameValue", form.profileName());
        model.addAttribute("activityValue", form.activity());
        model.addAttribute("durationValue", form.minDurationMinutes());
        model.addAttribute("temperatureValue", form.temperaturePreference());
        model.addAttribute("windValue", form.windPreference());
        model.addAttribute("precipitationValue", form.precipitationPreference());
        return renderEdit(profile, profile.getLocationId(), "", model);
    }

    @PostMapping("/profiles/{id}/edit")
    public String updateProfile(@PathVariable long id,
                                @Valid @ModelAttribute("profileForm") ProfileForm form, BindingResult binding,
                                @RequestParam(defaultValue = "") String locationQuery,
                                @RequestParam(defaultValue = "save") String operation,
                                HttpServletResponse response, Model model, RedirectAttributes redirect) {
        SearchProfile profile = repository.findById(id).orElse(null);
        if (profile == null) {
            return missingProfile(redirect);
        }
        model.addAttribute("profileNameValue", binding.getFieldValue("profileName"));
        model.addAttribute("activityValue", binding.getFieldValue("activity"));
        model.addAttribute("durationValue", binding.getFieldValue("minDurationMinutes"));
        model.addAttribute("temperatureValue", binding.getFieldValue("temperaturePreference"));
        model.addAttribute("windValue", binding.getFieldValue("windPreference"));
        model.addAttribute("precipitationValue", binding.getFieldValue("precipitationPreference"));
        if (binding.hasErrors()) {
            model.addAttribute("invalidFields", binding.getFieldErrors().stream()
                    .map(FieldError::getField).collect(Collectors.toSet()));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("profileError", "Bitte wähle einen Ort, eine Aktivität und gültige Wetterwünsche. "
                    + "Die Mindestdauer muss mindestens eine Minute betragen.");
            return renderEdit(profile, selectedLocation(binding), locationQuery, model);
        }
        if ("search".equals(operation)) {
            if (locationQuery.strip().length() < 2) {
                model.addAttribute("locationError", "Bitte gib mindestens zwei Zeichen ein.");
            }
            return renderEdit(profile, form.locationId(), locationQuery, model);
        }

        try {
            profiles.save(id, form);
        } catch (SearchProfileService.MissingProfile exception) {
            return missingProfile(redirect);
        } catch (SearchProfileService.InvalidProfile exception) {
            model.addAttribute("profileError", exception.getMessage());
            return renderEdit(profile, form.locationId(), locationQuery, model);
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("profileError", duplicateMessage(exception));
            return renderEdit(profile, form.locationId(), locationQuery, model);
        }
        redirect.addFlashAttribute("profileSuccess", "Die Änderungen wurden gespeichert.");
        return "redirect:/profiles";
    }

    @PostMapping("/profiles/delete")
    public String deleteProfile(@RequestParam @Min(1) long id, RedirectAttributes redirect) {
        if (!profiles.delete(id)) {
            return missingProfile(redirect);
        }
        redirect.addFlashAttribute("profileSuccess", "Das Suchprofil wurde entfernt.");
        return "redirect:/profiles";
    }

    private Integer selectedLocation(BindingResult binding) {
        Object value = binding.getFieldValue("locationId");
        if (value instanceof Integer id && id > 0) {
            return id;
        }
        return null;
    }

    private String renderEdit(SearchProfile profile, Integer selectedLocationId, String query, Model model) {
        String searchTerm = query.strip();
        List<GeocodingResponse.Location> locations = List.of();
        if (searchTerm.length() >= 2) {
            try {
                locations = geocodingClient.search(searchTerm).stream()
                        .filter(location -> location.id() != profile.getLocationId()).toList();
            } catch (RestClientException exception) {
                model.addAttribute("locationError", "Die Ortssuche ist gerade nicht verfügbar.");
            }
        }

        model.addAttribute("profile", profile);
        model.addAttribute("locationQuery", searchTerm);
        model.addAttribute("locations", locations);
        model.addAttribute("showSelectedLocation", selectedLocationId != null
                && selectedLocationId != profile.getLocationId()
                && locations.stream().noneMatch(location -> location.id() == selectedLocationId));
        model.addAttribute("selectedLocationId", selectedLocationId);
        return "profile-edit";
    }

    private String missingProfile(RedirectAttributes redirect) {
        redirect.addFlashAttribute("profileError", "Dieses Suchprofil wurde nicht gefunden.");
        return "redirect:/profiles";
    }

    private String returnToPlanning(ProfileForm form, LocalDate startDate, LocalDate endDate, LocalDate searchDate,
                                    String message, RedirectAttributes redirect) {
        redirect.addFlashAttribute("profileError", message);
        redirect.addFlashAttribute("profileName", form.profileName());
        new PlanningSelection(form.locationId(), form.activity(), form.minDurationMinutes(),
                form.temperaturePreference(), form.windPreference(), form.precipitationPreference(),
                startDate, endDate, searchDate).parameters().forEach(redirect::addAttribute);
        return "redirect:/";
    }

    private String duplicateMessage(DataIntegrityViolationException exception) {
        if (exception.getMostSpecificCause() instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
            return "Diesen Profilnamen gibt es schon. Bitte wähle einen anderen.";
        }
        throw exception;
    }
}

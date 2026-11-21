package de.anton.wannpasstes;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class LegacyProfileNames implements SmartInitializingSingleton {

    private final SearchProfileRepository repository;
    private final TransactionTemplate transactions;

    LegacyProfileNames(SearchProfileRepository repository, TransactionTemplate transactions) {
        this.repository = repository;
        this.transactions = transactions;
    }

    @Override
    public void afterSingletonsInstantiated() {
        transactions.executeWithoutResult(status -> updateNames());
    }

    private void updateNames() {
        if (!repository.existsByNameKeyIsNull()) {
            return;
        }
        var profiles = repository.findAll(Sort.by("id"));
        Set<String> reserved = profiles.stream()
                .map(profile -> SearchProfileService.nameKey(profile.getName()))
                .collect(Collectors.toSet());
        Set<String> used = profiles.stream()
                .map(SearchProfile::nameKey)
                .filter(key -> key != null)
                .collect(Collectors.toCollection(HashSet::new));

        for (SearchProfile profile : profiles) {
            if (profile.nameKey() != null) {
                continue;
            }
            String name = profile.getName().strip();
            String key = SearchProfileService.nameKey(name);
            if (!used.add(key)) {
                int number = 2;
                do {
                    name = withSuffix(profile.getName().strip(), number++);
                    key = SearchProfileService.nameKey(name);
                } while (!reserved.add(key));
                used.add(key);
            }
            profile.assignName(name, key);
        }
    }

    private String withSuffix(String name, int number) {
        String suffix = " (" + number + ")";
        int end = Math.min(name.length(), 100 - suffix.length());
        if (Character.isHighSurrogate(name.charAt(end - 1))) {
            end--;
        }
        return name.substring(0, end).stripTrailing() + suffix;
    }
}

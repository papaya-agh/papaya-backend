package pl.edu.agh.papaya.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    SPRINT_ENROLLMENT_OPENED("Podawanie dostępności zostało otwarte"),
    SPRINT_ENROLLMENT_REMINDER("Nie zapomnij złożyć deklaracji dostępności"),
    SPRINT_ENROLLMENT_CLOSED("Deklaracje dostępności zostały zamknięte");

    private final String emailSubject;
}

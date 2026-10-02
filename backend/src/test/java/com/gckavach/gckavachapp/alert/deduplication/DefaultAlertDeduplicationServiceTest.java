package com.gckavach.gckavachapp.alert.deduplication;

import com.gckavach.gckavachapp.alert.service.DefaultAlertDeduplicationService;
import com.gckavach.gckavachapp.common.exception.AlertConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DefaultAlertDeduplicationService.
 *
 * These tests verify:
 *
 * 1. A new fingerprint can be acquired.
 * 2. A duplicate fingerprint produces a conflict.
 * 3. A fingerprint can be released.
 * 4. Active fingerprints can be checked.
 * 5. Invalid input is rejected.
 */
@ExtendWith(MockitoExtension.class)
class DefaultAlertDeduplicationServiceTest {

    @Mock
    private AlertDeduplicationRepository repository;

    private DefaultAlertDeduplicationService service;

    @BeforeEach
    void setUp() {

        service =
                new DefaultAlertDeduplicationService(
                        repository
                );
    }

    /**
     * Verifies that a new fingerprint can be acquired.
     */
    @Test
    void shouldAcquireNewFingerprint() {

        assertDoesNotThrow(() ->
                service.acquire(
                        "fingerprint-001",
                        "alert-001"
                )
        );

        verify(repository).save(any(AlertDeduplication.class));
    }

    /**
     * Verifies that MongoDB duplicate-key errors
     * are converted into AlertConflictException.
     */
    @Test
    void shouldRejectDuplicateFingerprint() {

        doThrow(new DuplicateKeyException(
                "Duplicate fingerprint"
        )).when(repository).save(
                any(AlertDeduplication.class)
        );

        assertThrows(
                AlertConflictException.class,
                () -> service.acquire(
                        "fingerprint-001",
                        "alert-002"
                )
        );
    }

    /**
     * Verifies that a fingerprint can be released.
     */
    @Test
    void shouldReleaseFingerprint() {

        service.release("fingerprint-001");

        verify(repository)
                .deleteByFingerprint("fingerprint-001");
    }

    /**
     * Verifies that an active fingerprint is detected.
     */
    @Test
    void shouldReturnTrueWhenFingerprintIsActive() {

        when(repository.existsByFingerprint(
                "fingerprint-001"
        )).thenReturn(true);

        boolean result =
                service.isActive("fingerprint-001");

        assertTrue(result);

        verify(repository)
                .existsByFingerprint("fingerprint-001");
    }

    /**
     * Verifies that an unknown fingerprint
     * is reported as inactive.
     */
    @Test
    void shouldReturnFalseWhenFingerprintIsNotActive() {

        when(repository.existsByFingerprint(
                "fingerprint-001"
        )).thenReturn(false);

        boolean result =
                service.isActive("fingerprint-001");

        assertFalse(result);
    }

    /**
     * Verifies that a blank fingerprint cannot
     * be acquired.
     */
    @Test
    void shouldRejectBlankFingerprint() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.acquire(
                        "",
                        "alert-001"
                )
        );
    }

    /**
     * Verifies that a null fingerprint cannot
     * be acquired.
     */
    @Test
    void shouldRejectNullFingerprint() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.acquire(
                        null,
                        "alert-001"
                )
        );
    }

    /**
     * Verifies that a blank alert ID cannot
     * be used for deduplication.
     */
    @Test
    void shouldRejectBlankAlertId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.acquire(
                        "fingerprint-001",
                        ""
                )
        );
    }

    /**
     * Verifies that a null alert ID cannot
     * be used for deduplication.
     */
    @Test
    void shouldRejectNullAlertId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.acquire(
                        "fingerprint-001",
                        null
                )
        );
    }

    /**
     * Releasing a null fingerprint should be safe.
     */
    @Test
    void shouldIgnoreNullFingerprintOnRelease() {

        assertDoesNotThrow(() ->
                service.release(null)
        );
    }

    /**
     * Releasing a blank fingerprint should be safe.
     */
    @Test
    void shouldIgnoreBlankFingerprintOnRelease() {

        assertDoesNotThrow(() ->
                service.release("")
        );
    }

    /**
     * A null fingerprint should be considered inactive.
     */
    @Test
    void shouldReturnFalseForNullFingerprint() {

        assertFalse(
                service.isActive(null)
        );
    }

    /**
     * A blank fingerprint should be considered inactive.
     */
    @Test
    void shouldReturnFalseForBlankFingerprint() {

        assertFalse(
                service.isActive("")
        );
    }
}
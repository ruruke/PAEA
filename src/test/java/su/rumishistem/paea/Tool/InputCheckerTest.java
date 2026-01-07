package su.rumishistem.paea.Tool;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InputCheckerTest {
    @Test
    public void testIsHost() {
        // Valid hosts
        assertTrue(InputChecker.is_host("example.com"));
        assertTrue(InputChecker.is_host("a.b.c"));
        assertTrue(InputChecker.is_host("localhost"));
        assertTrue(InputChecker.is_host("my-host.example.com"));
        assertTrue(InputChecker.is_host("127.0.0.1")); // Technically matches the regex as labels
        assertTrue(InputChecker.is_host("a".repeat(63) + ".com"));

        // Invalid hosts
        assertFalse(InputChecker.is_host(null));
        assertFalse(InputChecker.is_host(""));
        assertFalse(InputChecker.is_host("-example.com"));
        assertFalse(InputChecker.is_host("example-.com"));
        assertFalse(InputChecker.is_host("example.com-"));
        assertFalse(InputChecker.is_host("a".repeat(64) + ".com")); // label too long
        assertFalse(InputChecker.is_host("a".repeat(254))); // total length too long
        assertFalse(InputChecker.is_host("exam_ple.com")); // underscore not allowed
    }
}

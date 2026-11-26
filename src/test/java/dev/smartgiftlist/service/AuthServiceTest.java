package dev.smartgiftlist.service;

import dev.smartgiftlist.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @Test
    void registersAuthenticatesAndPreventsDuplicates() {
        InMemoryRepositories repo = new InMemoryRepositories();
        AuthService auth = new AuthService(repo.users());

        ServiceResult<User> mismatch = auth.register("alice", "alice@test.example", "1999-05-14", "secret123", "other123");
        assertFalse(mismatch.isOk());

        ServiceResult<User> created = auth.register("alice", "alice@test.example", "1999-05-14", "secret123", "secret123");
        assertTrue(created.isOk());
        assertEquals("Lumibella", created.getValue().getIcon());

        ServiceResult<User> dupUser = auth.register("alice", "other@test.example", "", "secret123", "secret123");
        assertFalse(dupUser.isOk());
        ServiceResult<User> dupEmail = auth.register("bob", "alice@test.example", "", "secret123", "secret123");
        assertFalse(dupEmail.isOk());

        assertTrue(auth.authenticate("alice", "secret123").isOk());
        assertFalse(auth.authenticate("alice", "wrongpass").isOk());
    }

    @Test
    void updatesProfileWithOptionalPasswordAndDeletesAccount() {
        InMemoryRepositories repo = new InMemoryRepositories();
        AuthService auth = new AuthService(repo.users());

        User alice = auth.register("alice", "alice@test.example", "", "secret123", "secret123").getValue();
        auth.register("bob", "bob@test.example", "", "secret123", "secret123");

        ServiceResult<User> clash = auth.updateProfile(alice.getId(), "bob", "", "", "Novaterra");
        assertFalse(clash.isOk());

        ServiceResult<User> updated = auth.updateProfile(alice.getId(), "alice_new", "", "", "Novaterra");
        assertTrue(updated.isOk());
        assertEquals("alice_new", updated.getValue().getUsername());
        assertEquals("Novaterra", updated.getValue().getIcon());
        assertTrue(auth.authenticate("alice_new", "secret123").isOk());

        ServiceResult<User> rotated = auth.updateProfile(alice.getId(), "alice_new", "newpass99", "newpass99", "Quixart");
        assertTrue(rotated.isOk());
        assertFalse(auth.authenticate("alice_new", "secret123").isOk());
        assertTrue(auth.authenticate("alice_new", "newpass99").isOk());

        assertTrue(auth.deleteAccount(alice.getId()).isOk());
        assertTrue(auth.getUserById(alice.getId()).isEmpty());
    }
}

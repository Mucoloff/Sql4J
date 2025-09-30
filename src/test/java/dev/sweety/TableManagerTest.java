package dev.sweety;

import dev.sweety.sql4j.api.connection.SQLConnection;
import dev.sweety.sql4j.api.field.DataField;
import dev.sweety.sql4j.api.table.Info;
import dev.sweety.sql4j.impl.connection.SQLiteConnection;
import dev.sweety.sql4j.impl.table.Table;
import dev.sweety.sql4j.impl.table.TableManager;
import org.junit.jupiter.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class TableManagerTest {

    @Info(name = "TestUser")
    static class TestUser {
        @DataField(name = "id", notNull = true, unique = true)
        int id;
        @DataField(name = "name")
        String name;
    }

    SQLConnection connection;

    @BeforeEach
    void setUp() {
        connection = new SQLiteConnection("test.db");
    }

    @Test
    void testCreateTableSync() {
        var table = TableManager.create(TestUser.class, connection);
        assertNotNull(table);
        assertEquals("TestUser", table.name());
    }

    @Test
    void testCreateTableAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Table<TestUser>> future = TableManager.createAsync(TestUser.class, connection);
        Table<TestUser> table = future.get();
        assertNotNull(table);
        assertEquals("TestUser", table.name());
    }

    @Test
    void testDropTableSync() {
        TableManager.create(TestUser.class, connection);
        assertDoesNotThrow(() -> TableManager.drop(TestUser.class, connection));
    }

    @Test
    void testDropTableAsync() throws ExecutionException, InterruptedException {
        TableManager.create(TestUser.class, connection);
        CompletableFuture<Void> future = TableManager.dropAsync(TestUser.class, connection);
        assertDoesNotThrow(() -> future.get());
    }

    @Test
    void testConcurrentCreateAndDrop() throws InterruptedException, ExecutionException {
        CompletableFuture<Table<TestUser>> createFuture = TableManager.createAsync(TestUser.class, connection);
        Table<TestUser> table = createFuture.get();
        assertNotNull(table);

        CompletableFuture<Void> dropFuture = TableManager.dropAsync(TestUser.class, connection);
        dropFuture.get();

        // Ricreazione per testare la concorrenza
        CompletableFuture<Table<TestUser>> createFuture2 = TableManager.createAsync(TestUser.class, connection);
        Table<TestUser> table2 = createFuture2.get();
        assertNotNull(table2);
    }
}

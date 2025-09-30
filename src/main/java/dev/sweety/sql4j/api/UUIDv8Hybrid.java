package dev.sweety.sql4j.api;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class UUIDv8Hybrid {

    private static final SecureRandom random = new SecureRandom();
    private static final AtomicInteger counter = new AtomicInteger();

    private static final File datFile = new File("uuidv8.dat");
    private static final int NODE_ID = random.nextInt(); // oppure un ID fisso per il nodo

    static {
        if (!datFile.exists()) {
            try {
                datFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else{

            try (FileInputStream fis = new FileInputStream(datFile)) {
                counter.set(fis.read());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println(counter.get());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (FileOutputStream fos = new FileOutputStream(datFile)) {
                fos.write(counter.get());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

    }

    public static UUID generate() {
        long unixMillis = System.currentTimeMillis();

        // 48 bit timestamp + 16 bit counter
        long mostSig = (unixMillis & 0xFFFFFFFFFFFFL) << 16;
        int count = counter.getAndIncrement() & 0xFFFF;
        mostSig |= (count & 0xFFFF);

        // aggiungo versione v8
        mostSig &= ~(0xF000L);  // pulisco 4 bit versione
        mostSig |= (0x8000L);   // setto "1000" = v8

        // 32 bit node id + 32 bit random
        long leastSig = ((long) NODE_ID << 32) | (random.nextInt() & 0xFFFFFFFFL);

        // setto variant (10xx...)
        leastSig &= ~(0b11L << 62);
        leastSig |= (0b10L << 62);

        return new UUID(mostSig, leastSig);
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID fromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long msb = bb.getLong();
        long lsb = bb.getLong();
        return new UUID(msb, lsb);
    }


    // 🕒 Estrai il timestamp di creazione come Instant
    public static long getCreationTime(UUID uuid) {
        long mostSig = uuid.getMostSignificantBits();
        return (mostSig >> 16) & 0xFFFFFFFFFFFFL;
    }
}

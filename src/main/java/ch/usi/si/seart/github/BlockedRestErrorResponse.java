package ch.usi.si.seart.github;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.net.URL;
import java.util.Date;

@Getter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlockedRestErrorResponse extends RestErrorResponse {

    private static final String TEMPLATE = "%s due to %s at %s (see %s)";

    Block block;

    @Override
    public String toString() {
        return String.format(TEMPLATE, message, block.reason.description, block.createdAt, block.url);
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class Block {

        Reason reason;
        Date createdAt;
        URL url;

        public Block(String reasonName, Date createdAt, URL url) {
            Reason reason;
            try {
                reason = Reason.valueOf(reasonName.toUpperCase());
            } catch (RuntimeException ignored) {
                reason = Reason.UNKNOWN;
            }
            this.reason = reason;
            this.createdAt = createdAt;
            this.url = url;
        }

        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        private enum Reason {

            TOS("GitHub Terms of Service (TOS) violation"),
            DMCA("Digital Millennium Copyright Act (DMCA) violation"),
            UNKNOWN("unknown reason");

            String description;
        }
    }
}

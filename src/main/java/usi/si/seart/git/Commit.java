package usi.si.seart.git;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a single commit in a Git repository.
 * Only contains information pertaining
 * to the commit date and SHA-256 hash.
 * All instances of this class are immutable.
 * An instance without a specified date
 * <em>and</em> hash is called a null commit.
 * Such commits should be returned in place
 * of {@code null} to represent that no
 * commit information is available.
 *
 * @author Ozren Dabić
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("java:S6548")
public class Commit {

    public static final Commit UNKNOWN = new Commit();

    String sha;
    Date date;

    @Override
    public String toString() {
        if (sha == null)
            return "null commit";
        String truncated = sha.substring(0, 7);
        if (date == null)
            return truncated;
        return String.format("%s on %s", truncated, date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commit commit = (Commit) o;
        return Objects.equals(sha, commit.sha) && Objects.equals(date, commit.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha, date);
    }
}
